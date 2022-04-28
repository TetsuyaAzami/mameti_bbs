package controllers.forms

import play.api.data._
import play.api.data.Forms._

object PostForm {
  case class PostFormData(content: String)
  case class PostUpdateFormData(postId: Long, content: String)

  val postForm = Form {
    mapping(
      "content" -> text(minLength = 1, maxLength = 140)
    )(PostFormData.apply)(PostFormData.unapply)
  }

  val postUpdateForm = Form {
    mapping(
      "postId" -> longNumber,
      "content" -> text(minLength = 1, maxLength = 140)
    )(PostUpdateFormData.apply)(PostUpdateFormData.unapply)
  }
}
