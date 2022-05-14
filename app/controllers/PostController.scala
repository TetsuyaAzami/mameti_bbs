package controllers

import play.api.mvc.{MessagesControllerComponents, MessagesAbstractController}
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.i18n.Lang

import models.domains._
import models.services.PostService
import controllers.forms.PostForm._
import controllers.forms.CommentForm._
import controllers.forms.SignInForm._

import java.time.LocalDateTime
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import common._
import common.errors._

@Singleton
class PostController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userOptAction: UserOptAction,
    userNeedLoginAction: UserNeedLoginAction,
    postService: PostService,
    errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  def index() = userOptAction.async { implicit request =>
    postService.findAllWithFlag(None, None).map { result =>
      Ok(views.html.posts.index(postForm, commentForm, result))
    }
  }

  def detail(postId: Long) = userOptAction.async { implicit request =>
    postService.findByPostIdWithCommentList(postId).flatMap { result =>
      result._1 match {
        case None => {
          errorHandler.onClientError(request, 404, "")
        }
        case (Some(postWithCommentList)) => {
          val likeList = result._2
          Future.successful(
            Ok(
              views.html.posts
                .detail(postWithCommentList, likeList, commentForm)
            )
          )
        }
      }
    }
  }

  def insert() = userOptAction.async { implicit request =>
    val signInUserOpt = request.signInUserOpt
    // ログイン確認
    signInUserOpt match {
      case None => {
        Future.successful(
          Redirect(routes.SignInController.toSignIn())
            .flashing("errorNeedSignIn" -> messagesApi("error.needSignIn"))
        )
      }
      case Some(signInUser) => {
        val errorFunction = { formWithErrors: Form[PostFormData] =>
          postService.findAllWithFlag(None, None).map { result =>
            BadRequest(
              views.html.posts
                .index(formWithErrors, commentForm, result)
            )
          }
        }

        val successFunction = { post: PostFormData =>
          val trimmedContent = post.content.replace("\r", "").replace("\n", "")
          val postForInsert =
            Post(
              None,
              trimmedContent,
              signInUser.userId,
              None,
              LocalDateTime.now(),
              List()
            )
          postService.insert(postForInsert).flatMap { _ =>
            postService.findAllWithFlag(None, None).map { allPosts =>
              Redirect(routes.PostController.index())
                .flashing("successInsert" -> messagesApi("success.insert"))
            }
          }
        }
        postForm.bindFromRequest().fold(errorFunction, successFunction)
      }
    }
  }

  def edit(postId: Long) = userNeedLoginAction.async { implicit request =>
    println()
    println()
    println()
    println("editを通りました")
    val signInUser = request.signInUser
    val userId = signInUser.userId
    // ログインユーザが送られてきたpostIdの投稿を所有していなかったら、403エラー
    postService.findByPostIdAndUserId(postId, userId).map { postOpt =>
      postOpt match {
        case None => {
          Forbidden(
            views.html.errors.client_error(
              403,
              "Forbidden",
              messagesApi("error.http.forbidden")
            )
          )
        }
        case Some(post) => {
          // DBから取得したデータをformに詰めてviewに渡す
          val formWithPostData = postUpdateForm.fill(post)
          Ok(views.html.posts.edit(formWithPostData))
        }
      }
    }
  }

  def update() = userNeedLoginAction.async { implicit request =>
    val sentPostForm = postUpdateForm.bindFromRequest()
    val signInUser = request.signInUser
    val userId = signInUser.userId

    val errorFunction = { formWithErrors: Form[PostUpdateFormData] =>
      Future.successful(BadRequest(views.html.posts.edit(formWithErrors)))
    }
    val successFunction = { post: PostUpdateFormData =>
      val savePostData =
        Post(
          Option(post.postId),
          post.content,
          userId,
          None,
          LocalDateTime.now(),
          List()
        )
      postService.update(savePostData, userId).flatMap {
        // ログインユーザの所有している投稿以外を更新しようとしたら403エラー
        numberOfRowsUpdated: Long =>
          numberOfRowsUpdated match {
            case 0 => {
              errorHandler.onClientError(request, 403, "")
            }
            case _ => {
              postService
                .findByUserId(userId)
                .map(post =>
                  Redirect(routes.UserController.detail(userId))
                    .flashing("successUpdate" -> messagesApi("success.update"))
                )
            }
          }
      }
    }
    sentPostForm.fold(errorFunction, successFunction)
  }

  def delete(postId: Long) = userNeedLoginAction.async { implicit request =>
    val signInUser = request.signInUser
    val userId = signInUser.userId
    postService.delete(postId, userId).map { numberOfRowsUpdated =>
      numberOfRowsUpdated match {
        case 0 => {
          Forbidden(
            views.html.errors.client_error(
              403,
              "Forbidden",
              messagesApi("error.http.forbidden")
            )
          )
        }
        case _ => {
          Redirect(routes.UserController.detail(userId))
            .flashing("successDelete" -> messagesApi("success.delete"))
        }
      }
    }
  }
}
