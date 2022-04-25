package models.repositories

import models.DatabaseExecutionContext
import models.domains.Comment

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime
import models.domains.OptionComment
import scala.concurrent.Future

class CommentRepository @Inject() (
    dbApi: DBApi,
    userService: UserRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val optionCommentWithUserParser = {
    get[Option[Long]]("c_comment_id") ~
      get[Option[Long]]("c_user_id") ~
      userService.userWhoCommentedParser.? ~
      get[Option[Long]]("c_post_id") ~
      get[Option[String]]("c_content") ~
      get[Option[LocalDateTime]]("c_commented_at") map {
        case commentId ~ userId ~ userWhoCommented ~ postId ~ content ~ commentedAt =>
          OptionComment(
            commentId,
            userId,
            userWhoCommented,
            postId,
            content,
            commentedAt
          )
      }
  }

  private[repositories] val commentParserWithUser = {
    get[Option[Long]]("c_comment_id") ~
      get[Long]("c_user_id") ~
      userService.userWhoCommentedParser.? ~
      get[Long]("c_post_id") ~
      get[String]("c_content") ~
      get[LocalDateTime]("c_commented_at") map {
        case commentId ~ userId ~ userWhoCommented ~ postId ~ content ~ commentedAt =>
          Comment(
            commentId,
            userId,
            userWhoCommented,
            postId,
            content,
            commentedAt
          )
      }

  }

  def insert(comment: Comment): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      {
        SQL(
          "INSERT INTO comments (user_id, post_id, content, commented_at) VALUES ({userId},{postId},{content},{commentedAt});"
        )
          .on(
            "userId" -> comment.userId,
            "postId" -> comment.postId,
            "content" -> comment.content,
            "commentedAt" -> comment.commentedAt
          )
          .executeInsert()
      }
    }
  }

  def findByIdWithUser(commentId: Long): Future[Comment] = Future {
    db.withConnection { implicit conn =>
      {
        SQL"""
          SELECT
          c.comment_id c_comment_id,
          c.user_id c_user_id,
          c.post_id c_post_id,
          c.content c_content,
          c.commented_at c_commented_at,
          cu.user_id cu_user_id,
          cu.name cu_name,
          cu.profile_img cu_profile_img
          FROM comments c
          INNER JOIN users cu
          ON c.user_id = cu.user_id
          WHERE c.comment_id = ${commentId}
          ;""".as(commentParserWithUser.single)
      }
    }
  }
}
