package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
import models.domains.CommentFormData
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.ValidationError
import play.api.data.validation.Valid

object CommentForm {
  val commentForm = Form {
    mapping(
      "postId" -> longNumber,
      "content" -> text.verifying(contentConstraint)
    )(CommentFormData.apply)(CommentFormData.unapply)
  }

  lazy val contentConstraint: Constraint[String] = Constraint(
    "constraint.post.content"
  )(content => {
    if (content.isBlank) Invalid(ValidationError("error.minLength", 1))
    else if (content.replaceAll("\r?\n", "").isBlank)
      Invalid(ValidationError("error.minLength", 1))
    else if (content.length > 140) Invalid("error.maxLength", 140)
    else Valid
  })
}
