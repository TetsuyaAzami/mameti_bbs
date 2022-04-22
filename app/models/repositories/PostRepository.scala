package models.repositories

import models.domains.Post
import models.domains.PostForInsert
import models.domains.User
import models.domains.UserWhoPosted
import models.domains.Comment
import models.DatabaseExecutionContext

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime

import scala.concurrent.Future
import scala.language.postfixOps

class PostRepository @Inject() (
    dbApi: DBApi,
    userRepository: UserRepository,
    commentRepository: CommentRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val withUser = {
    get[Option[Long]]("p_post_id") ~
      get[String]("p_content") ~
      userRepository.userWhoPostedParser ~
      get[LocalDateTime]("p_posted_at") map {
        case postId ~ content ~ user ~ postedAt =>
          Post(postId, content, user, postedAt, Nil)
      }
  }

  def findAll(): Future[List[Post]] = Future {
    db.withConnection { implicit con =>
      SQL"""
            SELECT
              p.post_id p_post_id,
              p.content p_content,
              p.user_id p_user_id,
              p.posted_at p_posted_at,
              u.user_id u_user_id,
              u.name u_name,
              u.profile_img u_profile_img
            FROM posts p
            INNER JOIN users u
            ON p.user_id = u.user_id
            ORDER BY p_posted_at DESC;"""
        .as(
          withUser.*
        )
    }
  }

  def findByUserId(userId: Long): Future[List[Post]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
        SELECT
        p.post_id p_post_id,
        p.content p_content,
        p.user_id p_user_id,
        p.posted_at p_posted_at,
        u.user_id u_user_id,
        u.name u_name,
        u.profile_img u_profile_img
        FROM posts p
        INNER JOIN users u
        ON p.user_id = u.user_id
        WHERE p.user_id = ${userId}
        ORDER BY p_posted_at DESC;
        """.as(withUser.*)
    }
  }

  def findByPostId(postId: Long) = Future {
    db.withConnection { implicit conn =>
      val sqlResult =
        SQL"""
          SELECT
          p.post_id p_post_id,
          p.content p_content,
          p.posted_at p_posted_at,
          u.user_id u_user_id,
          u.name u_name,
          u.profile_img u_profile_img,
          c.comment_id c_comment_id,
          c.user_id c_user_id,
          c.post_id c_post_id,
          c.content c_content,
          c.commented_at c_commented_at
          FROM posts p
          LEFT OUTER JOIN users u ON p.user_id = u.user_id
          LEFT OUTER JOIN comments c ON p.post_id = c.post_id
          WHERE p.post_id = ${postId}
          ORDER BY c_commented_at ASC; """
          .as((withUser ~ commentRepository.optionCommentParser).*)

      val post = sqlResult(0)._1

      // 投稿に紐づくコメントがない場合、空のコメントリストを返します
      val firstCommentId = sqlResult.map(_._2)(0).commentId
      val commentList = firstCommentId match {
        case None    => List()
        case Some(i) => sqlResult.map(_._2)
      }

      val postWithCommentList = post.copy(commentList = commentList)
      postWithCommentList
    }
  }

  def insert(postForInsert: PostForInsert): Future[Option[Long]] = Future {
    db.withConnection { implicit connection =>
      SQL("""
      INSERT INTO posts (content, user_id, posted_at) VALUES ({content}, {userId}, {postedAt});
      """).bind(postForInsert).executeInsert()
    }

  }
}
