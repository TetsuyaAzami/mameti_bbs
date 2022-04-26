package models.domains

import java.time.LocalDate
import play.api.libs.json.Writes
import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._

final case class User(
    userId: Option[Long] = None,
    name: String,
    email: String,
    password: String,
    birthday: Option[LocalDate],
    introduce: Option[String],
    profileImg: Option[String],
    departmentId: Int
)

final case class SignInUser(
    userId: Long,
    name: String,
    profileImg: String
)

// 投稿したユーザ
final case class UserWhoPosted(
    userId: Long,
    name: String,
    profileImg: Option[String]
)

// コメントしたユーザ
final case class UserWhoCommented(
    userId: Option[Long],
    name: Option[String],
    profileImg: Option[String]
)

object UserWhoCommented {
  implicit val userWhoCommentedWrites: Writes[UserWhoCommented] =
    (JsPath \ "userId")
      .write[Option[Long]]
      .and((JsPath \ "name").write[Option[String]])
      .and((JsPath \ "profileImg").write[Option[String]])(
        unlift(UserWhoCommented.unapply)
      )
}
