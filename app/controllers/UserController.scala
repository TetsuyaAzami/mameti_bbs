package controllers

import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import models.repositories.UserRepository
import scala.concurrent.ExecutionContext
import play.api.mvc.Action
import play.api.mvc.MessagesAbstractController
import scala.concurrent.Future

class UserController @Inject() (
    mcc: MessagesControllerComponents,
    userService: UserRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {

  def index() = Action.async { implicit request =>
    userService.findUserById(1).map {
      case Some(user) =>
        Ok(views.html.users.index(user))
      case other => NotFound
    }
  }
}
