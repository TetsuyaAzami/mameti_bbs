package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.data.Form

import models.repositories._
import models.domains.Post
import models.domains.PostForInsert
import models.domains.PostForUpdate
import views.html.helper.form
import views.html.defaultpages.error
import controllers.forms.PostForm
import controllers.forms.CommentForm

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val postForm = PostForm.postForm
  val commentForm = CommentForm.commentForm
  val postUpdateForm = PostForm.postUpdateForm

  def index() = Action.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, commentForm, allPosts))
    }
  }

  def detail(postId: Long) = Action.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).map { postWithComments =>
      Ok(views.html.posts.detail(postWithComments, commentForm))
    }
  }

  def insert() = Action.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostForm.PostFormData] =>
      postService.findAll().map { allPosts =>
        BadRequest(
          views.html.posts.index(formWithErrors, commentForm, allPosts)
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

  def edit(postId: Long) = Action.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    postService.findByPostId(postId).map { post =>
      // DBから取得したデータをformに詰めてviewに渡す
      val postUpdateData =
        Map(
          "postId" -> post.postId.toString(),
          "content" -> post.content.toString()
        )
      val updateDataFromDB = postUpdateForm.bind(postUpdateData)
      Ok(views.html.posts.edit(updateDataFromDB))
    }
  }

  def update() = Action.async { implicit request =>
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
