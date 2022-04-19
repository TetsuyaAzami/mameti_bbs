package controllers

import models.repositories._
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import scala.concurrent.ExecutionContext

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  def index() = Action.async { implicit request =>
    postService.selectAllPosts().map { allPosts =>
      Ok(views.html.posts.index(allPosts))
    }
  }
}
