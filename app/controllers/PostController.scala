package controllers
import models.repositories._
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import scala.concurrent.ExecutionContext
import views.html.helper.form
import play.api.data.Form
import controllers.forms.PostForm
import models.domains.Post
import views.html.defaultpages.error
import models.domains.PostForInsert
import java.time.LocalDateTime

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val postForm = PostForm.postForm

  def index() = Action.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, allPosts))
    }
  }

  def detail(id: Long) = Action.async { implicit request =>
    postService.findByPostId(id).map { postWithComments =>
      println(postWithComments)
      Ok(views.html.posts.detail(postWithComments))
    }
  }

  def insert() = Action.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostForm.PostFormData] =>
      println("エラーがありました")
      postService.findAll().map { allPosts =>
        BadRequest(views.html.posts.index(formWithErrors, allPosts))
      }
    }

    val successFunction = { post: PostForm.PostFormData =>
      val postForInsert = PostForInsert(post.content, 1, LocalDateTime.now())
      postService.insert(postForInsert).flatMap { _ =>
        postService.findAll().map { allPosts =>
          Redirect(routes.PostController.index())
            .flashing("success" -> "投稿完了しました")
        }
      }
    }
    postForm.bindFromRequest().fold(errorFunction, successFunction)
  }
}
