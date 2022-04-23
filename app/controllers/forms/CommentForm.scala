package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object CommentForm {
  case class CommentFormData(postId: Long, content: String)

  val commentForm = Form {
    mapping(
      "postId" -> longNumber,
      "content" -> text(minLength = 1, maxLength = 140)
    )(CommentFormData.apply)(CommentFormData.unapply)
  }
}
