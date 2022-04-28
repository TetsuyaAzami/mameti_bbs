package controllers.forms

import play.api.data._
import play.api.data.Forms._
import models.domains.UpdateUserProfileFormData
import java.time.LocalDate

object UpdateUserProfileForm {
  val updateUserProfileForm = Form {
    mapping(
      "userId" -> longNumber,
      "name" -> text(minLength = 1, maxLength = 128),
      "email" -> email,
      "birthday" -> optional(localDate(pattern = "yyyy-MM-dd")),
      "introduce" -> optional(text),
      "profileImg" -> optional(text(maxLength = 128)),
      "departmentId" -> longNumber
    )(UpdateUserProfileFormData.apply)(UpdateUserProfileFormData.unapply)
  }
}
