package models.domains

import anorm.{Macro, ToParameterList}
import java.time.LocalDateTime
import play.api.libs.json.JsPath
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._

final case class Comment(
    commentId: Option[Long] = None,
    userId: Long,
    userWhoCommented: Option[UserWhoCommented] = None,
    postId: Long,
    content: String,
    commentedAt: LocalDateTime
)

object Comment {
  implicit val commentWrites: Writes[Comment] = (
    (JsPath \ "commentId")
      .write[Option[Long]]
      .and((JsPath \ "userId").write[Long])
      .and((JsPath \ "userWhoCommented").write[Option[UserWhoCommented]])
      .and((JsPath \ "postId").write[Long])
      .and((JsPath \ "content").write[String])
      .and((JsPath \ "commentedAt").write[LocalDateTime])(
        unlift(Comment.unapply)
      )
  )
}

// insertの際に使用
case class CommentFormData(postId: Long, content: String)
