package controllers

import models.repositories._
import models.domains.Post
import models.domains.PostForInsert
import views.html.helper.form
import views.html.defaultpages.error
import controllers.forms.PostForm

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.data.Form

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PostController @Inject() (
    mcc: MessagesControllerComponents,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val postForm = PostForm.postForm
  val postUpdateForm = PostForm.postUpdateForm

  def index() = Action.async { implicit request =>
    postService.findAll().map { allPosts =>
      Ok(views.html.posts.index(postForm, allPosts))
    }
  }

  def detail(postId: Long) = Action.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).map { postWithComments =>
      Ok(views.html.posts.detail(postWithComments))
    }
  }

  def edit(postId: Long) = Action.async { implicit request =>
    // ここでeditするpostのuserIdとログインユーザのuserIdが一致するか確認
    // あとで実装

    postService.findByPostId(postId).map { post =>
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

    val sentPostForm = postForm.bindFromRequest()
    println()
    println()
    println()
    println()
    println(sentPostForm)
    // フォームバリデーション
    // val errorFunction = { formWithErrors: Form[PostForm.PostUpdateFormData] =>
    //   postService.findByPostId(sentPostForm.get.postId)
    // }

    // 登録処理

    // ユーザマイページへ遷移
    postService
      .findByUserId(1)
      .map(post =>
        Redirect(routes.UserController.index(1))
          .flashing("success" -> "投稿完了しました")
      )
  }

  def insert() = Action.async { implicit request =>
    val errorFunction = { formWithErrors: Form[PostForm.PostFormData] =>
      postService.findAll().map { allPosts =>
        BadRequest(views.html.posts.index(formWithErrors, allPosts))
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

}
