package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController

import models.services.PostService
import models.services.LikeService
import controllers.forms.CommentForm._
import controllers.forms.PostForm._
import controllers.forms.LikeForm._

import scala.concurrent.ExecutionContext
import javax.inject._
import common.UserOptAction
import views.html.users.sign_in
import scala.concurrent.Future
import play.api.i18n.Lang
import play.api.data.Form
import models.domains.LikeForInsert
import models.domains.Like
import play.api.libs.json.Json
import views.html.defaultpages.error

@Singleton
class LikeController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostService,
    userOptAction: UserOptAction,
    likeService: LikeService
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(mcc) {
  def insert = userOptAction.async(parse.json) { implicit request =>
    implicit val lang = Lang.defaultLang
    // 独自アクションビルダーでOptionのユーザを返すがログインが必要なものを定義すべき？
    // signInUserの有無によるログインチェック(認証)
    val signInUserOpt = request.signInUserOpt
    signInUserOpt match {
      case None => {
        Future.successful(Unauthorized)
      }
      case Some(signInUser) => {
        // バリデーション
        val errorFunction = { formWithErrors: Form[LikeForInsert] =>
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        }
        val successFunction = { data: LikeForInsert =>
          val postId = data.postId
          val userId = signInUser.userId
          val like = Like(None, userId, postId)
          likeService.insert(like).flatMap { likeId =>
            likeService.count(postId).map { likeCount =>
              Created(Json.toJson(likeCount))
            }
          }
        }
        likeForm.bindFromRequest().fold(errorFunction, successFunction)
      }
    }
  }
}
