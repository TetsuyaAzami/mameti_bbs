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
  def findAllWithFlag(
      department: Option[String],
      sortByOpt: Option[String]
  ): Future[List[(Post, Option[Long], List[Like])]] = {
    sortByOpt match {
      case None => {
        // 投稿日の降順でsort
        postRepository.findAllWithFlag(department).map { result =>
          result.sortWith { (e1, e2) => e1._1.postedAt isAfter e2._1.postedAt }
        }
      }
      case Some("like") => {
        // いいねの降順でsort
        postRepository.findAllWithFlag(department).map { result =>
          result.sortWith { (e1, e2) => e1._3.size > e2._3.size }
        }
      }
      case Some(value) => {
        // 予期せぬ値が渡されてきたら空のリストを返す
        Future.successful(Nil)
      }
    }
  }

  def findByUserId(
      userId: Long
  ): Future[List[(Post, Option[Long], List[Like])]] =
    // 投稿日の降順でsort
    postRepository.findByUserId(userId).map { result =>
      result.sortWith((e1, e2) => e1._1.postedAt isAfter e2._1.postedAt)
    }

  def findByPostIdAndUserId(
      postId: Long,
      userId: Long
  ): Future[Option[PostUpdateFormData]] =
    postRepository.findByPostIdAndUserId(postId, userId)

  def findByPostIdWithCommentList(
      postId: Long
  ): Future[(Option[Post], List[Like])] =
    postRepository.findByPostIdWithCommentList(postId).map { result =>
      val postOpt = result._1
      postOpt match {
        case None => {
          (None, Nil)
        }
        // コメントリストの投稿日の降順でsort
        case Some(post) => {
          val sortedCommentList =
            post.commentList.sortWith((comment1, comment2) =>
              comment1.commentedAt isAfter comment2.commentedAt
            )
          val postWithSortedCommentList =
            post.copy(commentList = sortedCommentList)
          (Some(postWithSortedCommentList), result._2)
        }
      }
    }

  def update(post: Post, userId: Long) = postRepository.update(post, userId)

  def insert(post: Post): Future[Option[Long]] =
    postRepository.insert(post)

  def delete(postId: Long, userId: Long): Future[Long] =
    postRepository.delete(postId, userId)
}
