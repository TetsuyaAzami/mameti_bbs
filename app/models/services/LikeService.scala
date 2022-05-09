package models.services

import javax.inject._
import models.repositories.LikeRepository
import scala.concurrent.ExecutionContext
import models.domains.Like
import akka.compat.Future
import scala.concurrent.Future

@Singleton
class LikeService @Inject() (likeRepository: LikeRepository)(
    ec: ExecutionContext
) {
  def insert(like: Like): Future[Option[Long]] =
    likeRepository.insert(like)

  def count(postId: Long): Future[Long] = likeRepository.count(postId)
}
