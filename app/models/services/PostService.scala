package models.services

import models.domains._
import models.repositories.PostRepository
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class PostService @Inject() (postRepository: PostRepository)(implicit
    ec: ExecutionContext
) {
  def findAll(): Future[List[(Post, Option[Long], List[Like])]] = {
    // 投稿順でsort
    postRepository.findAll().map { result =>
      result.sortWith { (e1, e2) => e1._1.postedAt isAfter e2._1.postedAt }
    }
  }

  def findAllWithFlag(
      department: Option[String],
      sortBy: Option[String]
  ): Future[List[(Post, Option[Long], List[Like])]] = {
    // いいね順でsort
    postRepository.findAllWithFlag(department, sortBy).map { result =>
      result.sortWith { (e1, e2) => e1._3.size > e2._3.size }
    }
  }

  def findByUserId(
      userId: Long
  ): Future[List[(Post, Option[Long], List[Like])]] =
    postRepository.findByUserId(userId)

  def findByPostIdAndUserId(
      postId: Long,
      userId: Long
  ): Future[Option[PostUpdateFormData]] =
    postRepository.findByPostIdAndUserId(postId, userId)

  def findByPostIdWithCommentList(
      postId: Long
  ): Future[(Option[Post], List[Like])] =
    postRepository.findByPostIdWithCommentList(postId)

  def update(post: Post, userId: Long) = postRepository.update(post, userId)

  def insert(post: Post): Future[Option[Long]] =
    postRepository.insert(post)

  def delete(postId: Long, userId: Long): Future[Long] =
    postRepository.delete(postId, userId)
}
