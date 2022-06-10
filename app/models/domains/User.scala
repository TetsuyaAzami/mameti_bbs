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
    birthday: Option[LocalDate] = None,
    introduce: Option[String] = None,
    profileImg: Option[String] = None,
    departmentId: Long,
    department: Option[Department] = None
)

//updateの際に使用。パスワードリマインダーをつけるため、passwordがない。
final case class UpdateUserProfileFormData(
    userId: Long,
    name: String,
    email: String,
    birthday: Option[LocalDate],
    introduce: Option[String],
    profileImg: Option[String],
    departementId: Long
)

final case class SignInUser(
    userId: Long,
    name: String,
    email: String,
    password: String,
    birthday: Option[LocalDate] = None,
    introduce: Option[String] = None,
    profileImg: Option[String] = None,
    departmentId: Long,
    department: Department
) extends Serializable

// 投稿したユーザ
final case class UserWhoPosted(
    userId: Long,
    name: String,
    profileImg: Option[String],
    department: Option[Department]
)

// コメントしたユーザ
final case class UserWhoCommented(
    userId: Long,
    name: String,
    profileImg: Option[String]
)

object UserWhoCommented {
  implicit val userWhoCommentedWrites: Writes[UserWhoCommented] =
    (JsPath \ "userId")
      .write[Long]
      .and((JsPath \ "name").write[String])
      .and((JsPath \ "profileImg").write[Option[String]])(
        unlift(UserWhoCommented.unapply)
      )
}
