package models.services

import models.domains._
import models.repositories.PostRepository
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class PostService @Inject() (postRepository: PostRepository)(
    ec: ExecutionContext
) {
  def findAll(): Future[List[(Post, Option[Long], Option[Long])]] =
    postRepository.findAll()

  def findByUserId(userId: Long): Future[List[(Post, Option[Long])]] =
    postRepository.findByUserId(userId)

  def findByPostIdAndUserId(
      postId: Long,
      userId: Long
  ): Future[Option[PostUpdateFormData]] =
    postRepository.findByPostIdAndUserId(postId, userId)

  def findByPostIdWithCommentList(postId: Long): Future[Option[Post]] =
    postRepository.findByPostIdWithCommentList(postId)

  def update(post: Post, userId: Long) = postRepository.update(post, userId)

  def insert(post: Post): Future[Option[Long]] =
    postRepository.insert(post)

  def delete(postId: Long, userId: Long): Future[Long] =
    postRepository.delete(postId, userId)
}
