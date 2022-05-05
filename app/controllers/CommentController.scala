package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.data.Form

import models.DatabaseExecutionContext
import models.domains.{Comment, CommentFormData}
import models.services.{CommentService, PostService}
import controllers.forms.CommentForm._
import controllers.forms.PostForm._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import javax.inject.Inject
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject._
import common._

@Singleton
class CommentController @Inject() (
    mcc: MessagesControllerComponents,
    commentService: CommentService,
    postService: PostService,
    userNeedLoginAsyncAction: UserNeedLoginAsyncAction
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(mcc) {
  def insert() = userNeedLoginAsyncAction(parse.json).async {
    implicit request =>
      val sentCommentForm = commentForm.bindFromRequest()
      val signInUser = request.signInUser

      val errorFunction = { formWithErrors: Form[CommentFormData] =>
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      }
      val successFunction = { commentData: CommentFormData =>
        // formのデータをdomainに詰め直す
        val comment = Comment(
          userId = signInUser.userId,
          postId = commentData.postId,
          content = commentData.content,
          commentedAt = LocalDateTime.now()
        )
        commentService.insert(comment).flatMap { commentIdOpt =>
          commentIdOpt match {
            case None => {
              Future.successful(InternalServerError)
            }
            case Some(commentId) => {
              commentService.findByIdWithUser(commentId).map {
                commentWithUser =>
                  Created(Json.toJson(commentWithUser))
              }
            }
          }
        }
      }
      sentCommentForm.fold(errorFunction, successFunction)
  }
}
