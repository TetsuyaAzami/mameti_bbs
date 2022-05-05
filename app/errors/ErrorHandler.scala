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

  override def onClientError(
      request: RequestHeader,
      statusCode: Int,
      message: String
  ): Future[Result] = {
    if (statusCode == 413) {
      Future.successful(
        EntityTooLarge(
          views.html.errors
            .client_error(
              413,
              "Request Entity Too large",
              messagesApi("error.http.RequestEntityTooLarge")
            )
        )
      )
    } else if (statusCode == 403) {
      Future.successful(
        Forbidden(
          views.html.errors
            .client_error(403, "Forbidden", messagesApi("error.http.forbidden"))
        )
      )
    } else if (statusCode == 404) {
      Future.successful(
        NotFound(
          views.html.errors
            .client_error(
              404,
              "Not Found",
              messagesApi("error.http.notFound")
            )
        )
      )
    } else super.onClientError(request, statusCode, message)
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
