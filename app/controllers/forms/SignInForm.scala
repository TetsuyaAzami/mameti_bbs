package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

case class SignInData(email: String, password: String)

object SignInForm {
  val signInForm = Form {
    mapping(
      "email" -> email,
      "password" -> text(minLength = 8, maxLength = 16)
    )(SignInData.apply)(SignInData.unapply)
  }
}
