package models.domains

import play.api.libs.json.JsPath
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._

case class Like(
    likeId: Option[Long],
    userId: Long,
    postId: Long
)

case class LikeForInsert(
    likeId: Option[Long],
    postId: Long
)

object Like {
  implicit val likeWrites: Writes[Like] = (
    (JsPath \ "likeId")
      .write[Option[Long]]
      .and((JsPath \ "userId").write[Long])
      .and((JsPath \ "postId").write[Long])(unlift(Like.unapply))
  )
}
