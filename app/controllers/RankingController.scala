package controllers

import play.api.mvc.MessagesControllerComponents
import javax.inject._
import common.errors.ErrorHandler
import scala.concurrent.ExecutionContext
import play.api.mvc.MessagesAbstractController
import play.api.i18n.Lang
import common.UserNeedLoginAction
import models.services.PostService

@Singleton
class RankingController @Inject() (
    mcc: MessagesControllerComponents,
    userNeedLoginAction: UserNeedLoginAction,
    postService: PostService,
    errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang
  def index(department: Option[String]) = userNeedLoginAction.async {
    implicit request =>
      postService.findAllWithFlag(department).map { result =>
        result.foreach { res =>
          println()
          print(res)
          println(res._3.size)
        }
        Ok(views.html.ranking.index(result))
      }
  }
}
