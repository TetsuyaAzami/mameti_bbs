package controllers.forms

import play.api.data._
import play.api.data.Forms._

import models.domains.UpdateUserProfileFormData

import java.time.LocalDate
import common._
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.ValidationError
import play.api.data.validation.Invalid

object UpdateUserProfileForm {
  val updateUserProfileForm = Form {
    mapping(
      "userId" -> longNumber,
      "name" -> text(minLength = 1, maxLength = 128),
      "email" -> email,
      "birthday" -> optional(
        localDate(pattern = "yyyy-MM-dd").verifying(birthdayConstraint)
      ),
      "introduce" -> optional(text(maxLength = 200)),
      "profileImg" -> optional(
        text(maxLength = 200)
      ),
      "departmentId" -> longNumber(min = 1, max = 3)
    )(UpdateUserProfileFormData.apply)(UpdateUserProfileFormData.unapply)
  }

  lazy val birthdayConstraint: Constraint[LocalDate] = Constraint(
    "constraints.user.birthday"
  )(birthday => {

    val isBirthdayIsBeforeNow: Boolean = birthday.isBefore(LocalDate.now())
    if (isBirthdayIsBeforeNow) {
      Valid
    } else {
      Invalid(ValidationError("error.user.birthday.mustBeBeforeNow"))
    }
  })
}
