package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import models.DatabaseExecutionContext
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import models.repositories.PostRepository
import controllers.forms.CommentForm
import controllers.forms.PostForm
import scala.concurrent.Future

class CommentController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(mcc) {
  def insert() = Action.async { implicit request =>
    val commentForm = CommentForm.commentForm
    val postForm = PostForm.postForm

    println()
    println()
    println()
    println("insertを通りました")
    Future { // とりあえずFutureにした。あとで直す。
      Redirect(routes.PostController.index())
        .flashing("success" -> "コメントを投稿しました")
    }
  }
}
