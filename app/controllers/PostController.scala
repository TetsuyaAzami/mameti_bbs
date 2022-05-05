package controllers

import play.api.mvc.{MessagesControllerComponents, MessagesAbstractController}
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.i18n.Lang

import models.domains._
import models.services.PostService
import controllers.forms.PostForm._
import controllers.forms.CommentForm._

import java.time.LocalDateTime
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import common._
import errors._

@Singleton
class PostController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userOptAction: UserOptAction,
    userNeedLoginAction: UserNeedLoginAction,
    userNeedAuthorityAction: UserNeedAuthorityAction,
    postService: PostService,
    errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  def index() = userOptAction.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, commentForm, allPosts))
    }
  }

  def detail(postId: Long) = userOptAction.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).flatMap {
      postWithCommentList =>
        postWithCommentList match {
          case None => {
            errorHandler.onClientError(request, 404, "")
          }
          case Some(postWithCommentList) => {
            Future.successful(
              Ok(views.html.posts.detail(postWithCommentList, commentForm))
            )
          }
        }
    }
  }

  def insert() = userOptAction.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostFormData] =>
      postService.findAll().map { allPosts =>
        BadRequest(
          views.html.posts
            .index(formWithErrors, commentForm, allPosts)
        )
      }
    }

    val successFunction = { post: PostFormData =>
      val postForInsert =
        Post(
          None,
          post.content,
          1, // ログインユーザIdに変更
          None,
          LocalDateTime.now(),
          List()
        )
      postService.insert(postForInsert).flatMap { _ =>
        postService.findAll().map { allPosts =>
          Redirect(routes.PostController.index())
            .flashing("successInsert" -> messagesApi("success.insert"))
        }
      }
    }
    postForm.bindFromRequest().fold(errorFunction, successFunction)
  }

  def edit(postId: Long) = userNeedLoginAction.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    postService.findByPostId(postId).map { post =>
      // DBから取得したデータをformに詰めてviewに渡す
      val formWithPostData = postUpdateForm.fill(post)
      Ok(views.html.posts.edit(formWithPostData))
    }
  }

  def update() = userNeedAuthorityAction.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    val sentPostForm = postUpdateForm.bindFromRequest()

    val errorFunction = { formWithErrors: Form[PostUpdateFormData] =>
      Future.successful(BadRequest(views.html.posts.edit(formWithErrors)))
    }
    val successFunction = { post: PostUpdateFormData =>
      val savePostData =
        Post(
          Option(post.postId),
          post.content,
          1, // ログインユーザのIdに変更
          None,
          LocalDateTime.now(),
          List()
        )
      postService.update(savePostData)
      postService
        .findByUserId(1)
        .map(post =>
          Redirect(routes.UserController.detail(1))
            .flashing("successUpdate" -> messagesApi("success.update"))
        )
    }

    sentPostForm.fold(errorFunction, successFunction)
  }

  def delete(postId: Long) = Action.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    postService.delete(postId).map { deletedPostId =>
      Redirect(routes.UserController.detail(1))
        .flashing("successDelete" -> messagesApi("success.delete"))
    }
  }
}
