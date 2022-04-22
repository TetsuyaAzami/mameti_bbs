package models.repositories

import models.DatabaseExecutionContext
import models.domains.Comment

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime

class CommentRepository @Inject() (
    dbApi: DBApi
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

//   private[repositories] val simple: RowParser[Comment] =
//     Macro.namedParser[Comment](Macro.ColumnNaming.SnakeCase)

  private[repositories] val simple = {
    get[Option[Long]]("c_comment_id") ~
      get[Option[Long]]("c_user_id") ~
      get[Option[Long]]("c_post_id") ~
      get[String]("c_content") ~
      get[Option[LocalDateTime]]("c_commented_at") map {
        case commentId ~ userId ~ postId ~ content ~ commentedAt =>
          Comment(commentId, userId, postId, content, commentedAt)
      }
  }
}
