package controllers.forms

import java.time.LocalDate
import play.api.data._
import play.api.data.Forms._

case class UpdateUserProfileFormData(
    userId: Long,
    name: String,
    email: String,
    password: String,
    birthday: Option[LocalDate],
    introduce: Option[String],
    profileImg: Option[String],
    departementId: Long
)

object UpdateUserProfileForm {
  val updateUserProfileForm = Form {
    mapping(
      "userId" -> longNumber,
      "name" -> text(minLength = 1, maxLength = 128),
      "email" -> email,
      "password" -> text(minLength = 8, maxLength = 16),
      "birthday" -> optional(localDate(pattern = "yyyy/MM/dd")),
      "introduce" -> optional(text),
      "profileImg" -> optional(text(maxLength = 128)),
      "departmentId" -> longNumber
    )(UpdateUserProfileFormData.apply)(UpdateUserProfileFormData.unapply)
  }
}
