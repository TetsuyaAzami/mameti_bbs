package errors

import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._
import play.api.i18n.MessagesApi
import play.api.i18n.Lang

@Singleton
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    messagesApi: MessagesApi,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
  implicit val lang = Lang.defaultLang
  override protected def onNotFound(
      request: RequestHeader,
      message: String
  ): Future[Result] = {
    Future.successful(
      NotFound(
        views.html.errors
          .client_error(404, "Not Found", messagesApi("error.http.notFound"))
      )
    )
  }

  override def onProdServerError(
      request: RequestHeader,
      exception: UsefulException
  ) = {
    Future.successful(
      InternalServerError(
        views.html.errors.server_error(500, "サーバーエラー", "サーバ内で問題が発生しました。")
      )
    )
  }

}
