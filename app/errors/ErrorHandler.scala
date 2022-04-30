package errors

import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
  override protected def onNotFound(
      request: RequestHeader,
      message: String
  ): Future[Result] = {
    Future.successful(
      NotFound(
        views.html.errors
          .client_error(404, "Not Found", "ページが見つかりませんでした。")
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
