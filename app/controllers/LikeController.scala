package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController

import models.services.PostService
import controllers.forms.CommentForm._
import controllers.forms.PostForm._

import scala.concurrent.ExecutionContext
import javax.inject._
import common.UserOptAction

@Singleton
class LikeController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostService,
    userOptAction: UserOptAction
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(mcc) {
  def insert = userOptAction.async { implicit request =>
    // signInUserの有無によるログインチェック
    // 独自アクションビルダーでOptionのユーザを返すがログインが必要なものを定義すべき？
    println()
    println()
    println()
    println("いいねされました")
    postService.findAll().map { result =>
      Ok(views.html.posts.index(postForm, commentForm, result))
    }
  }
}
