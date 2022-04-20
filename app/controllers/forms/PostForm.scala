package controllers.forms

import play.api.data._
import play.api.data.Forms._

object PostForm {
  case class PostFormData(content: String)
  val postForm = Form {
    mapping {
      "content" -> nonEmptyText(minLength = 1, maxLength = 140)
    }(PostFormData.apply)(PostFormData.unapply)
  }
}
