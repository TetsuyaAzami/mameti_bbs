package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
import models.domains.CommentFormData

object CommentForm {
  val commentForm = Form {
    mapping(
      "postId" -> longNumber,
      "content" -> text(minLength = 1, maxLength = 140)
    )(CommentFormData.apply)(CommentFormData.unapply)
  }
}
