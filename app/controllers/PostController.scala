package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.i18n.Lang

import models.domains._
import models.services.PostService
import views.html.defaultpages.error
import views.html.helper.form
import controllers.forms.PostForm
import controllers.forms.CommentForm

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import common._

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userAction: UserAction,
    postService: PostService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang
  val postForm = PostForm.postForm
  val commentForm = CommentForm.commentForm
  val postUpdateForm = PostForm.postUpdateForm

  def index() = userAction.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, commentForm, allPosts))
    }
  }

  def detail(postId: Long) = userAction.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).flatMap {
      postWithCommentList =>
        postWithCommentList match {
          case None => {
            // 404 NotFoundを返す。あとで変更
            postService.findAll().map { allPosts =>
              Ok(views.html.posts.index(postForm, commentForm, allPosts))
            }
          }
          case Some(postWithCommentList) => {
            Future.successful(
              Ok(views.html.posts.detail(postWithCommentList, commentForm))
            )
          }
        }
    }
  }

  def insert() = userAction.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostFormData] =>
      postService.findAll().map { allPosts =>
        // ログインユーザ情報の取得
        val sessionId = request.session.get("sessionId")
        val signInUser = CacheUtil.getSessionUser(cache, sessionId)
        BadRequest(
          views.html.posts
            .index(formWithErrors, commentForm, allPosts)
        )
      }
    }

    val successFunction = { post: PostFormData =>
      val postForInsert = PostForInsert(post.content, 1, LocalDateTime.now())
      postService.insert(postForInsert).flatMap { _ =>
        postService.findAll().map { allPosts =>
          Redirect(routes.PostController.index())
            .flashing("success" -> "投稿完了しました")
        }
      }
    }
    postForm.bindFromRequest().fold(errorFunction, successFunction)
  }

  def edit(postId: Long) = userAction.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    postService.findByPostId(postId).map { post =>
      // DBから取得したデータをformに詰めてviewに渡す
      val formWithPostData = postUpdateForm.fill(post)
      Ok(views.html.posts.edit(formWithPostData))
    }
  }

  def update() = userAction.async { implicit request =>
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
            .flashing("success" -> messagesApi("update.success"))
        )
    }

    sentPostForm.fold(errorFunction, successFunction)
  }

  def delete() = Action.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    val deletePostId =
      request.body.asFormUrlEncoded.get("deletePostId")(0).toLong
    postService.delete(deletePostId).map { deletedPostId =>
      Redirect(routes.UserController.detail(1))
        .flashing("success" -> "delete.success")
    }
  }

}
