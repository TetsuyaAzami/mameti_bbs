package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
import models.domains.LikeForInsert

object LikeForm {
  val likeForm = Form {
    mapping(
      "likeId" -> optional(longNumber),
      "postId" -> longNumber
    )(LikeForInsert.apply)(LikeForInsert.unapply)
  }
}
