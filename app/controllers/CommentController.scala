package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.data.Form

import models.DatabaseExecutionContext
import models.domains.Comment
import models.repositories.CommentRepository
import models.repositories.PostRepository
import views.html.defaultpages.error
import controllers.forms.CommentForm
import controllers.forms.CommentForm.CommentFormData
import controllers.forms.PostForm

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.LocalDateTime

class CommentController @Inject() (
    mcc: MessagesControllerComponents,
    commentService: CommentRepository,
    postService: PostRepository
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
    val successFunction = { commentData: CommentForm.CommentFormData =>
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
