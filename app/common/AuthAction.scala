package common

import play.api.mvc._
import play.api.i18n._

import models.domains.SignInUser
import views.html.users.sign_in
import controllers.routes

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import errors.ErrorHandler

trait UserOptRequestHeader
    extends PreferredMessagesProvider
    with MessagesRequestHeader {
  def signInUserOpt: Option[SignInUser]
}

trait UserRequestHeader
    extends PreferredMessagesProvider
    with MessagesRequestHeader {
  def signInUser: SignInUser
}

class UserOptRequest[A](
    request: Request[A],
    val signInUserOpt: Option[SignInUser],
    val messagesApi: MessagesApi
) extends WrappedRequest[A](request)
    with UserOptRequestHeader

class UserRequest[A](
    request: Request[A],
    val signInUser: SignInUser,
    val messagesApi: MessagesApi
) extends WrappedRequest[A](request)
    with UserRequestHeader

class UserOptAction @Inject() (
    val parser: BodyParsers.Default,
    cache: CacheUtil,
    messagesApi: MessagesApi
)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[UserOptRequest, AnyContent]
    with Results {

  override def invokeBlock[A](
      request: Request[A],
      block: UserOptRequest[A] => Future[Result]
  ): Future[Result] = {
    val sessionIdOpt = request.session.get("sessionId")
    val signInUserOpt = cache.getSessionUser(sessionIdOpt)
    val userRequest = new UserOptRequest(request, signInUserOpt, messagesApi)
    block(userRequest)
  }
}

class UserNeedLoginAction @Inject() (
    val parser: BodyParsers.Default,
    cache: CacheUtil,
    messagesApi: MessagesApi
)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with Results {
  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val lang = Lang.defaultLang
    val sessionIdOpt = request.session.get("sessionId")
    val signInUserOpt = cache.getSessionUser(sessionIdOpt)
    signInUserOpt match {
      case None =>
        Future.successful(
          Redirect(routes.SignInController.signIn())
            .flashing("errorNeedSignIn" -> messagesApi("error.needSignIn"))
        )
      case Some(signInUser) => {
        val userRequest = new UserRequest(request, signInUser, messagesApi)
        block(userRequest)
      }
    }
  }
}

class UserNeedLoginAsyncAction @Inject() (
    val parser: BodyParsers.Default,
    cache: CacheUtil,
    messagesApi: MessagesApi
)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with Results {
  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val lang = Lang.defaultLang
    val sessionIdOpt = request.session.get("sessionId")
    val signInUserOpt = cache.getSessionUser(sessionIdOpt)
    signInUserOpt match {
      case None => {
        Future.successful(
          Unauthorized
        )
      }
      case Some(signInUser) => {
        val userRequest = new UserRequest(request, signInUser, messagesApi)
        block(userRequest)
      }
    }
  }
}
