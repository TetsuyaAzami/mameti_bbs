package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
import java.time.LocalDate
import org.checkerframework.checker.units.qual.min

case class SignUpFormData(
    name: String,
    email: String,
    password: String,
    departmentId: Long
)

object SignUpForm {
  val signUpForm = Form {
    mapping(
      "name" -> text(minLength = 1, maxLength = 128),
      "email" -> email,
      "password" -> text(minLength = 8, maxLength = 16),
      "departmentId" -> longNumber(min = 1, max = 3)
    )(SignUpFormData.apply)(SignUpFormData.unapply)
  }
}
