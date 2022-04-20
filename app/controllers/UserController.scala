package controllers

import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import models.repositories.UserRepository
import scala.concurrent.ExecutionContext
import play.api.mvc.Action
import play.api.mvc.MessagesAbstractController
import scala.concurrent.Future
import models.repositories.PostRepository

class UserController @Inject() (
    mcc: MessagesControllerComponents,
    userService: UserRepository,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {

  def index() = Action.async { implicit request =>
    postService.findByUserId(1).map { posts =>
      Ok(views.html.users.index(posts))
    }
  }
}
