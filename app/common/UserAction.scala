package common

import play.api.mvc._
import play.api.i18n.DefaultMessagesApi
import play.api.i18n.MessagesApi
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import models.domains.SignInUser
import play.api.cache.SyncCacheApi

trait UserRequestHeader
    extends PreferredMessagesProvider
    with MessagesRequestHeader {
  def signInUserOpt: Option[SignInUser]
}

class UserRequest[A](
    request: Request[A],
    val signInUserOpt: Option[SignInUser],
    val messagesApi: MessagesApi
) extends WrappedRequest[A](request)
    with UserRequestHeader

class UserAction @Inject() (
    val parser: BodyParsers.Default,
    cache: SyncCacheApi,
    messagesApi: MessagesApi
)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with Results {

  override def invokeBlock[A](
      request: Request[A],
      block: UserRequest[A] => Future[Result]
  ): Future[Result] = {
    val sessionIdOpt = request.session.get("sessionId")
    val signInUserOpt = CacheUtil.getSessionUser(cache, sessionIdOpt)
    val userRequest = new UserRequest(request, signInUserOpt, messagesApi)
    block(userRequest)
  }
}
