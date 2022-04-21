package models.repositories

import models.domains.Post
import models.domains.PostForInsert
import models.domains.PostWithComments
import models.domains.User
import models.domains.Comment
import models.DatabaseExecutionContext

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime

import scala.concurrent.Future
import models.domains.UserWhoPosted

class PostRepository @Inject() (
    dbApi: DBApi,
    userRepository: UserRepository,
    commentRepository: CommentRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val withUser = {
    get[Option[Long]]("posts.post_id") ~
      get[String]("posts.content") ~
      userRepository.userWhoPostedParser ~
      get[LocalDateTime]("posts.posted_at") map {
        case postId ~ content ~ user ~ postedAt =>
          Post(postId, content, user, postedAt)
      }
  }

  // private[repositories] val withComments = {
  //   get[Option[Long]]("posts.post_id") ~
  //     get[String]("posts.content") ~
  //     userRepository.userWhoPostedParser ~
  //     get[LocalDateTime]("posts.posted_at") ~
  //     commentRepository.simple map {
  //       case postId ~ content ~ user ~ postedAt ~ comments =>
  //         PostWithComments(postId, content, user, postedAt, comments)
  //     }
  // }

  def findAll(): Future[List[Post]] = Future {
    db.withConnection { implicit con =>
      SQL"""
            SELECT
              p.post_id,
              p.content,
              p.user_id,
              p.posted_at,
              user_id,
              u.name,
              u.profile_img
            FROM posts p
            INNER JOIN users u
            USING (user_id)
            ORDER BY p.posted_at DESC;"""
        .as(
          withUser.*
        )
    }
  }

  def findByUserId(userId: Long): Future[List[Post]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      SELECT
      p.post_id,
      p.content,
      p.user_id,
      p.posted_at,
      user_id,
      u.name,
      u.profile_img
      FROM posts p
      INNER JOIN users u
      USING (user_id)
      WHERE user_id = ${userId}
      ORDER BY p.posted_at DESC;
      """.as(withUser.*)
    }
  }

  def findByPostId(postId: Long): Future[(PostWithComments)] = Future {
    db.withConnection { implicit conn =>
      // postの1件取得
      val post =
        SQL"""
            SELECT
            p.post_id,
            p.content,
            p.user_id,
            p.posted_at,
            user_id,
            u.name,
            u.profile_img
            FROM posts p
            INNER JOIN users u
            USING (user_id)
            WHERE post_id = ${postId};
            """.as(withUser.single)

      // postに紐づくcommentsの取得
      val comments =
        SQL"""
            SELECT
            comment_id,
            user_id,
            post_id,
            content,
            commented_at
            FROM comments
            WHERE post_id = ${postId};""".as(
          commentRepository.simple.*
        )
      PostWithComments(
        post.postId,
        post.content,
        post.user,
        post.postedAt,
        comments
      )
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
