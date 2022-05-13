package controllers.forms

import play.api.data._
import play.api.data.Forms._
import models.domains.PostFormData
import models.domains.PostUpdateFormData
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.validation.ValidationError

object PostForm {

  val postForm = Form {
    mapping(
      "content" -> text.verifying(contentConstraint)
    )(PostFormData.apply)(PostFormData.unapply)
  }

  val postUpdateForm = Form {
    mapping(
      "postId" -> longNumber,
      "content" -> text.verifying(contentConstraint)
    )(PostUpdateFormData.apply)(PostUpdateFormData.unapply)
  }

  lazy val contentConstraint: Constraint[String] = Constraint(
    "constraints.post.content"
  )({ content =>
    // 空もしくは空白のみの投稿を許さない
    if (content.isBlank()) Invalid(ValidationError("error.minLength", 1))
    // 改行のみの投稿を許さない
    else if (content.replaceAll("\r?\n", "").isBlank())
      Invalid(ValidationError("error.minLength"))
    else if (content.length() > 140) Invalid("error.maxLength", 140)
    else Valid
  })
}
