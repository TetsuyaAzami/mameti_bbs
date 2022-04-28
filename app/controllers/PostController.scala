package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.cache.SyncCacheApi
import play.api.data.Form

import models.repositories._
import models.domains.Post
import models.domains.PostForInsert
import models.domains.PostForUpdate
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
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val postForm = PostForm.postForm
  val commentForm = CommentForm.commentForm
  val postUpdateForm = PostForm.postUpdateForm

  def index() = userAction.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, commentForm, allPosts))
    }
  }

  def detail(postId: Long) = userAction.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).map { postWithComments =>
      Ok(views.html.posts.detail(postWithComments, commentForm))
    }
  }

  def insert() = userAction.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostForm.PostFormData] =>
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

    val successFunction = { post: PostForm.PostFormData =>
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
      val postDataFromDB =
        Map(
          "postId" -> post.postId.toString(),
          "content" -> post.content.toString()
        )
      val formWithPostData = postUpdateForm.bind(postDataFromDB)
      Ok(views.html.posts.edit(formWithPostData))
    }
  }

  def update() = userAction.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    val sentPostForm = postUpdateForm.bindFromRequest()

    val errorFunction = { formWithErrors: Form[PostForm.PostUpdateFormData] =>
      Future.successful(BadRequest(views.html.posts.edit(formWithErrors)))
    }
    val successFunction = { post: PostForm.PostUpdateFormData =>
      val savePostData =
        PostForUpdate(post.postId, post.content, LocalDateTime.now())

      postService.update(savePostData)
      postService
        .findByUserId(1)
        .map(post =>
          Redirect(routes.UserController.index(1))
            .flashing("success" -> "編集完了しました")
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
      Redirect(routes.UserController.index(1))
        .flashing("success" -> "削除に成功しました")
    }
  }

}
