package models.services

import models.domains._
import models.repositories.PostRepository
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PostService @Inject() (postRepository: PostRepository)(
    ec: ExecutionContext
) {
  def findAll(): Future[List[(Post, Option[Long])]] = postRepository.findAll()

  def findByUserId(userId: Long): Future[List[(Post, Option[Long])]] =
    postRepository.findByUserId(userId)

  def findByPostId(postId: Long): Future[PostUpdateFormData] =
    postRepository.findByPostId(postId)

  def findByPostIdWithCommentList(postId: Long): Future[Option[Post]] =
    postRepository.findByPostIdWithCommentList(postId)

  def update(post: Post) = postRepository.update(post)

  def insert(post: Post): Future[Option[Long]] =
    postRepository.insert(post)

  def delete(postId: Long): Future[Long] = postRepository.delete(postId)
}
