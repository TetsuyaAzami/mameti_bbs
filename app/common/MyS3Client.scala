package common

import play.api.Configuration
import scala.concurrent.ExecutionContext
import javax.inject.Inject
import java.nio.file.Path
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

class MyS3Client @Inject() (configuration: Configuration) {
  val conf = configuration
  val bucketName = conf.get[String]("aws.s3.bucketName")
  val directory = conf.get[String]("aws.s3.directory")
  val accessKey = conf.get[String]("aws.s3.accessKey")
  val secretKey =
    conf.get[String]("aws.s3.secretKey")
  val credentials = StaticCredentialsProvider.create(
    AwsSessionCredentials.create(accessKey, secretKey, "")
  )

  val s3 = S3Client
    .builder()
    .region(Region.AP_NORTHEAST_1)
    .credentialsProvider(credentials)
    .build()

  def get(fileName: String) = {
    val getObjectRequest: GetObjectRequest =
      GetObjectRequest
        .builder()
        .bucket(bucketName)
        .key(directory + fileName)
        .build()
    s3.getObject(getObjectRequest)
  }

  def put(fileName: String, path: Path) = {
    val putObjectRequest: PutObjectRequest =
      PutObjectRequest
        .builder()
        .bucket(bucketName)
        .key(directory + fileName)
        .build()
    s3.putObject(putObjectRequest, RequestBody.fromFile(path))
  }

  def delete(fileName: String) = {
    val deleteObjectRequest: DeleteObjectRequest =
      DeleteObjectRequest
        .builder()
        .bucket(bucketName)
        .key(directory + fileName)
        .build()
    s3.deleteObject(deleteObjectRequest)
  }

}
