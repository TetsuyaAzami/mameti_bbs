package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.data.Form

import models.DatabaseExecutionContext
import models.domains.{Comment, CommentFormData}
import models.services.{CommentService, PostService}
import views.html.defaultpages.error
import controllers.forms.{CommentForm, PostForm}

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import javax.inject.Inject
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class CommentController @Inject() (
    mcc: MessagesControllerComponents,
    commentService: CommentService,
    postService: PostService
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(mcc) {
  val commentForm = CommentForm.commentForm
  val postForm = PostForm.postForm

  def insert() = Action(parse.json).async { implicit request =>
    val sentCommentForm = commentForm.bindFromRequest()

    val errorFunction = { formWithErrors: Form[CommentFormData] =>
      Future.successful(BadRequest(formWithErrors.errorsAsJson))
    }
    val successFunction = { commentData: CommentFormData =>
      // formのデータをdomainに詰め直す
      val comment = Comment(
        userId = 1,
        postId = commentData.postId,
        content = commentData.content,
        commentedAt = LocalDateTime.now()
      )
      commentService.insert(comment).flatMap { commentId =>
        commentService.findByIdWithUser(commentId.get).map { commentWithUser =>
          Created(Json.toJson(commentWithUser))
        }
      }
    }
    sentCommentForm.fold(errorFunction, successFunction)
  }
}
