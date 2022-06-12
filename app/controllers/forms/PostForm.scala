package controllers.forms

import models.domains.PostFormData
import models.domains.PostUpdateFormData

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.time.LocalDateTime

object PostForm {
  val postForm = Form {
    mapping(
      "content" -> text.verifying(contentConstraint)
    )(PostFormData.apply)(PostFormData.unapply)
  }

  implicit val postUpdateFormWrites = Json.writes[PostUpdateFormData]

  lazy val contentConstraint: Constraint[String] = Constraint(
    "constraints.post.content"
  )(content =>
    // 空もしくは空白のみの投稿を許さない
    if (content.isBlank) Invalid(ValidationError("error.minLength", 1))
    // 改行のみの投稿を許さない
    else if (content.replaceAll("\r?\n", "").isBlank)
      Invalid(ValidationError("error.minLength", 1))
    else if (content.length > 140) Invalid("error.maxLength", 140)
    else Valid
  )

  implicit val postUpdateFormDataReads: Reads[PostUpdateFormData] =
    new Reads[PostUpdateFormData] {
      override def reads(json: JsValue): JsResult[PostUpdateFormData] = {
        val postIdResult: JsResult[Long] = (json \ "postId").validate[Long]
        val contentResult: JsResult[String] =
          (json \ "content").validate[String]
        val postedAtResult: JsResult[LocalDateTime] =
          (json \ "postedAt").validate[LocalDateTime]
        for {
          postId <- postIdResult
          content <- contentResult
          postedAt <- postedAtResult
          result <-
            if (content.isBlank) {
              JsError(
                (JsPath \ "content"),
                JsonValidationError("error.minLength", 1)
              )
            } else if (content.replaceAll("\r|\n", "").isBlank) {
              JsError(
                (JsPath \ "content"),
                JsonValidationError("error.minLength", 1)
              )
            } else if (content.length > 140) {
              JsError(
                (JsPath \ "content"),
                JsonValidationError("error.maxLength", 140)
              )
            } else {
              JsSuccess(PostUpdateFormData(postId, content, postedAt))
            }
        } yield result
      }
    }

  implicit val postUpdateFormDataWrites: Writes[PostUpdateFormData] =
    Json.writes[PostUpdateFormData]

}
