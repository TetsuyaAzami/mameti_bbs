package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.mvc.Action
import play.api.data.Form
import play.api.cache._
import play.mvc

import models.repositories.UserRepository
import models.repositories.PostRepository
import controllers.forms.SignInForm
import controllers.forms.SignInData

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.util.UUID
import javax.inject.Inject
import common._

class UserController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userAction: UserAction,
    userService: UserRepository,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  val signInForm = SignInForm.signInForm

  /** ユーザマイページ
    *
    * @param userId
    *   ユーザId
    * @return
    *   マイページ
    */
  def index(userId: Long) = userAction.async { implicit request =>
    // ログインユーザのidと一致しているかのチェック あとで実装
    postService.findByUserId(userId).map { posts =>
      Ok(views.html.users.index(posts))
    }
  }

  /** ログインページ遷移
    *
    * @return
    *   ログインページ
    */
  def toSignIn() = userAction { implicit request =>
    Ok(views.html.users.sign_in(signInForm))
  }

  /** ログイン処理
    *
    * @return
    *   成功時: 豆知識一覧ページ 失敗時: ログインページ
    */
  def signIn() = userAction.async { implicit request =>
    val sentSignInForm = signInForm.bindFromRequest()
    val errorFunction = { formWithErrors: Form[SignInData] =>
      Future.successful(BadRequest(views.html.users.sign_in(formWithErrors)))
    }
    val successFunction = { userData: SignInData =>
      // email, パスワードのチェック
      userService
        .findUserByEmailAndPassword(userData.email, userData.password)
        .map { user =>
          user match {
            case None => {
              val formFilledWithUserData = signInForm.fill(userData)
              // エラー情報を注入
              val formWithErrors = formFilledWithUserData.withError(
                "userNotFound",
                "メールアドレスかパスワードが間違っています"
              )
              BadRequest(views.html.users.sign_in(formWithErrors))
            }
            case Some(signInUser) => {
              // sessionIdの生成とキャッシュへのログインユーザ情報格納
              val sessionId = UUID.randomUUID().toString()
              CacheUtil.setSessionUser(cache, sessionId, signInUser)

              Redirect(routes.PostController.index())
                .withSession(
                  "sessionId" -> sessionId
                )
            }
          }
        }
    }
    sentSignInForm.fold(errorFunction, successFunction)
  }

  def signOut() = Action {
    Redirect(routes.UserController.signIn()).withNewSession
  }

  /** ユーザ登録ページ遷移
    *
    * @return
    *   ユーザ登録ページ
    */
  def toSignUp() = userAction { implicit request =>
    Ok(views.html.users.register_user())
  }

  /** ユーザ登録処理
    *
    * @return
    *   成功時: ログインページ 失敗時: ユーザ登録処理
    */
  def signUp() = userAction { implicit request =>
    Ok(views.html.users.register_user())
  }
}
