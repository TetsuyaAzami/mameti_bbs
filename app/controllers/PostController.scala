package controllers
import models.repositories._
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import scala.concurrent.ExecutionContext
import views.html.helper.form

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val postForm = forms.PostForm.postForm
  def index() = Action.async { implicit request =>
    postService.selectAllPosts().map { allPosts =>
      Ok(views.html.posts.index(postForm, allPosts))
    }
  }

  def create() = Action { implicit request =>
    // DB処理あとで記述
    Redirect("/posts").flashing("success" -> "投稿完了しました")
  }
}
