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
      "introduce" -> optional(text(maxLength = 200)),
      "profileImg" -> optional(text(maxLength = 200)),
      "departmentId" -> longNumber(min = 1, max = 3)
    )(UpdateUserProfileFormData.apply)(UpdateUserProfileFormData.unapply)
  }
}
