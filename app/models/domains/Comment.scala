package models.domains

import anorm.Macro
import anorm.ToParameterList
import java.time.LocalDateTime
import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes

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

// データベースのカラムがnullになる場合、anormがパースできないためcontentをOption型に変更
final case class OptionComment(
    commentId: Option[Long] = None,
    userId: Option[Long],
    userWhoCommented: Option[UserWhoCommented],
    postId: Option[Long],
    content: Option[String],
    commentedAt: Option[LocalDateTime]
)
