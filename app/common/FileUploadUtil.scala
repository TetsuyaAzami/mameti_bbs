package common

import play.core.parsers.Multipart
import play.core.parsers.Multipart.FileInfo
import play.api.Configuration
import play.api.mvc.MultipartFormData._
import play.api.data.FormError
import play.api.libs.streams._
import play.api.i18n.Lang
import play.api.Logger

import akka.util.ByteString
import akka.stream.scaladsl._
import akka.stream.IOResult

import javax.inject._
import java.io.File
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.collection.mutable.ListBuffer

@Singleton
class FileUploadUtil @Inject() (configuration: Configuration)(implicit
    ec: ExecutionContext
) {
  private val logger = Logger(this.getClass())
  val myS3Client = new MyS3Client(configuration)
  val validExtensions = List("jpg", "jpeg", "png", "image/jpeg", "image/png")
  private implicit val lang = Lang.defaultLang

  type FilePartHandler[A] =
    FileInfo => Accumulator[ByteString, FilePart[A]]

  def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)

      accumulator.map { case IOResult(count, status) =>
        logger.info(s"count = $count, status = $status")
        FilePart(partName, filename, contentType, path.toFile)
      }
  }

  // ファイル名から拡張子を取り出す
  private def extractExtension(filename: String): String = {
    val extensionIndex = filename.lastIndexOf(".")
    val extension = filename.substring(extensionIndex + 1)
    extension
  }

  // 拡張子チェック
  private def isExtensionValid(filename: String): Boolean = {
    val extension = extractExtension(filename)
    if (validExtensions.contains(extension)) {
      true
    } else {
      false
    }
  }

  // 画像が正しい形式かチェックしてエラーリストを返す
  def extractErrorsFromUploadedFile(
      uploadedFile: Option[FilePart[File]]
  ): Seq[FormError] = {
    val errorList = ListBuffer.empty[FormError]
    uploadedFile match {
      case None => {}
      case Some(uploadedFile) => {
        if (!isExtensionValid(uploadedFile.filename)) {
          errorList += FormError("profileImg", "error.extension.invalid")
        }
      }
    }
    errorList.toList
  }

  // 画像処理のための一時ファイルを削除
  private def deleteTempFile(file: File) = {
    val size = Files.size(file.toPath)
    Files.deleteIfExists(file.toPath)
  }

  // すでにサーバーにアップロードされているファイルを削除
  private def deleteExistingFile(userEmail: String) = {
    validExtensions.map(extension => {
      val deleteFileName = userEmail + "." + extension
      // S3のバケット上からオブジェクトを削除
      myS3Client.delete(deleteFileName)
    })
  }

  // 画像保存をして、一時ファイルを削除
  def save(
      uploadedFileOpt: Option[FilePart[File]],
      userEmail: String
  ): Option[String] = {
    uploadedFileOpt match {
      case None => {
        None
      }
      case Some(uploadedFile) => {
        deleteExistingFile(userEmail)
        val filename = Paths.get(uploadedFile.filename).getFileName()
        val extension = extractExtension(filename.toString())
        val uploadFileName = userEmail + "." + extension
        // S3のバケット上にオブジェクトを作成
        myS3Client.put(uploadFileName, uploadedFile.ref.toPath())
        deleteTempFile(uploadedFile.ref)
        Some(userEmail + "." + extension)
      }
    }
  }

}
