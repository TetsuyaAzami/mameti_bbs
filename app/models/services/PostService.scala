package models.services

import models.domains.Post
import models.domains.PostForInsert
import models.domains.PostForUpdate
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

  def findByPostId(postId: Long): Future[PostForUpdate] =
    postRepository.findByPostId(postId)

  def findByPostIdWithCommentList(postId: Long): Future[Post] =
    postRepository.findByPostIdWithCommentList(postId)

  def update(post: PostForUpdate) = postRepository.update(post)

  def insert(postForInsert: PostForInsert): Future[Option[Long]] =
    postRepository.insert(postForInsert)

  def delete(postId: Long): Future[Long] = postRepository.delete(postId)
}
