package models.repositories

import models.domains._
import models.DatabaseExecutionContext

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import scala.concurrent.Future
import scala.language.postfixOps
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime

class PostRepository @Inject() (
    dbApi: DBApi,
    userRepository: UserRepository,
    commentRepository: CommentRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val forUpdate = {
    get[Long]("p_post_id") ~
      get[String]("p_content") map { case postId ~ content =>
        PostUpdateFormData(postId, content)
      }
  }

  private[repositories] val withUser = {
    get[Option[Long]]("p_post_id") ~
      get[String]("p_content") ~
      get[Long]("p_user_id") ~
      userRepository.userWhoPostedParser.? ~
      get[LocalDateTime]("p_posted_at") map {
        case postId ~ content ~ userId ~ user ~ postedAt =>
          Post(postId, content, userId, user, postedAt, List())
      }
  }

  def findAll(): Future[List[(Post, Option[Long])]] = Future {
    db.withConnection { implicit con =>
      SQL"""
        SELECT
        p.post_id p_post_id,
        p.content p_content,
        p.user_id p_user_id,
        p.posted_at p_posted_at,
        c.count c_count, -- コメント数の取得
        u.user_id u_user_id, -- 投稿したユーザの取得
        u.name u_name,
        u.profile_img u_profile_img
        FROM posts p
        LEFT OUTER JOIN (
        SELECT
        post_id,
        count(*) count
        FROM comments
        GROUP BY post_id
        ) c
        ON p.post_id = c.post_id
        INNER JOIN users u
        ON p.user_id = u.user_id
        ORDER BY p_posted_at DESC;"""
        .as(
          (withUser ~ long("c_count").?).map { case post ~ count =>
            (post.copy(), count)
          }.*
        )
    }
  }

  def findByUserId(userId: Long): Future[List[(Post, Option[Long])]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
        SELECT
        p.post_id p_post_id,
        p.content p_content,
        p.user_id p_user_id,
        p.posted_at p_posted_at,
        c.count c_count, -- コメント数の取得
        u.user_id u_user_id, -- 投稿したユーザの取得
        u.name u_name,
        u.profile_img u_profile_img
        FROM posts p
        LEFT OUTER JOIN (
        SELECT
        post_id,
        count(*) count
        FROM comments
        GROUP BY post_id
        ) c
        ON p.post_id = c.post_id
        INNER JOIN users u
        ON p.user_id = u.user_id
        WHERE p.user_id = ${userId}
        ORDER BY p_posted_at DESC;
        """
        .as((withUser ~ long("c_count").?).map { case post ~ count =>
          (post.copy(), count)
        }.*)
    }
  }

  def findByPostId(postId: Long): Future[PostUpdateFormData] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      SELECT
      p.post_id p_post_id,
      p.content p_content
      FROM posts p
      WHERE p.post_id = ${postId};""".as(forUpdate.single)
    }
  }

  def findByPostIdWithCommentList(postId: Long): Future[Option[Post]] = Future {
    db.withConnection { implicit conn =>
      val sqlResult =
        SQL"""
          SELECT
          p.post_id p_post_id,
          p.content p_content,
          p.user_id p_user_id,
          p.posted_at p_posted_at,
          u.user_id u_user_id,
          u.name u_name,
          u.profile_img u_profile_img,
          c.comment_id c_comment_id,
          c.user_id c_user_id,
          c.post_id c_post_id,
          c.content c_content,
          c.commented_at c_commented_at,
          cu.user_id cu_user_id,
          cu.name cu_name,
          cu.profile_img cu_profile_img
          FROM posts p
          LEFT OUTER JOIN users u
          ON p.user_id = u.user_id
          LEFT OUTER JOIN comments c
          ON p.post_id = c.post_id
          LEFT OUTER JOIN users cu
          ON c.user_id = cu.user_id
          WHERE p.post_id = ${postId}
          ORDER BY c_commented_at DESC; """
          .as(
            (withUser ~ commentRepository.commentWithUserParser.?).*
          )

      sqlResult match {
        case Nil => None
        case head :: next => {
          val post = sqlResult(0)._1
          val isCommentListNil = sqlResult(0)._2
          val commentList = isCommentListNil match {
            case None              => Nil
            case Some(commentList) => sqlResult.map(e => e._2.get)
          }

          val postWithCommentList = post.copy(commentList = commentList)
          Some(postWithCommentList)
        }
      }
    }
  }

  def update(post: Post) = Future {
    db.withConnection { implicit conn =>
      SQL(
        """UPDATE posts SET
           content = {content},
           posted_at = {postedAt}
           WHERE post_id = {postId};"""
      )
        .on(
          "content" -> post.content,
          "postedAt" -> post.postedAt,
          "postId" -> post.postId
        )
        .executeUpdate()
    }
  }

  def insert(post: Post): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
          INSERT INTO posts
                     (content, user_id, posted_at)
          VALUES     ({content}, {userId}, {postedAt});
      """)
        .on(
          "content" -> post.content,
          "userId" -> post.userId,
          "postedAt" -> post.postedAt
        )
        .executeInsert()
    }
  }

  def delete(postId: Long): Future[Long] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      WITH
      delete_comment AS( -- 投稿に紐づいているコメントも削除
      DELETE FROM comments
      WHERE post_id = ${postId}
      ),
      delete_likes AS( -- 投稿に紐づいているいいねも削除
      DELETE FROM likes
      WHERE post_id = ${postId}
      )
      DELETE FROM posts
      WHERE post_id = ${postId};""".executeUpdate()
    }
  }
}
