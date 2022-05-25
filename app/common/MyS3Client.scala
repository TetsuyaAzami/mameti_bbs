package common

import scala.concurrent.ExecutionContext
import play.api.Configuration
import javax.inject.Inject
class MyS3Client @Inject() (configuration: Configuration) {
  val conf = configuration
  val bucketName = conf.get[String]("aws.s3.bucketName")
  val directory = conf.get[String]("aws.s3.directory")
  val accessKey = conf.get[String]("aws.s3.accessKey")
  val secretKey =
    conf.get[String]("aws.s3.secretKey")
}
