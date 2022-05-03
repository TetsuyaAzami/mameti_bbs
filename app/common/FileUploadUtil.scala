package common

import play.core.parsers.Multipart
import play.core.parsers.Multipart.FileInfo
import play.api.libs.streams._
import play.api.mvc.MultipartFormData._
import play.api.Logger

import akka.util.ByteString
import akka.stream.scaladsl._
import akka.stream.IOResult

import java.io.File
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import javax.inject._
import java.nio.file.StandardCopyOption

@Singleton
class FileUploadUtil @Inject() (implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass())

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

  private def deleteTempFile(file: File) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
    Files.deleteIfExists(file.toPath)
  }

  def saveToApplicationServer(
      uploadedFile: FilePart[File],
      userEmail: String
  ) = {
    val saveFilePath =
      Paths.get(s"./public/images/profileImages/$userEmail")
    Files.copy(
      uploadedFile.ref.toPath(),
      saveFilePath,
      StandardCopyOption.REPLACE_EXISTING
    )
    deleteTempFile(uploadedFile.ref)
  }
}
