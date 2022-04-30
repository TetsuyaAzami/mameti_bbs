package controllers

import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.cache.SyncCacheApi
import play.api.i18n.Lang
import play.api.data.Form

import models.domains.User
import models.services.UserService
import models.services.DepartmentService
import controllers.forms.SignInForm._
import controllers.forms.SignUpForm._
import controllers.forms.SignInFormData
import controllers.forms.SignUpFormData

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import common.UserAction
import common.CacheUtil

class SignInController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userAction: UserAction,
    userService: UserService,
    departmentService: DepartmentService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  // ログイン遷移
  def toSignIn() = userAction { implicit request =>
    Ok(views.html.users.sign_in(signInForm))
  }

  // ログイン処理
  def signIn() = userAction.async { implicit request =>
    val sentSignInForm = signInForm.bindFromRequest()
    val errorFunction = { formWithErrors: Form[SignInFormData] =>
      Future.successful(BadRequest(views.html.users.sign_in(formWithErrors)))
    }
    val successFunction = { userData: SignInFormData =>
      // email, パスワードのチェック
      userService
        .findUserByEmailAndPassword(userData.email, userData.password)
        .map { user =>
          user match {
            case None => {
              val formToReturn = signInForm.fill(userData)
              // エラー情報を注入
              val formWithErrors = formToReturn.withError(
                "userNotFound",
                "user.notFound"
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
  // サインアウト処理
  def signOut() = Action {
    Redirect(routes.SignInController.signIn()).withNewSession
  }

  // サインアップ遷移
  def toSignUp() = userAction.async { implicit request =>
    departmentService.selectDepartmentList().map { departmentList =>
      Ok(views.html.users.register_user(signUpForm, departmentList))
    }
  }

  // サインアップ処理
  def signUp() = userAction.async { implicit request =>
    val sentSignUpForm = signUpForm.bindFromRequest()
    val errorFunction = { formWithErrors: Form[SignUpFormData] =>
      departmentService.selectDepartmentList().map { departmentList =>
        BadRequest(
          views.html.users.register_user(formWithErrors, departmentList)
        )
      }
    }
    val successFunction = { signUpData: SignUpFormData =>
      userService.findUserByEmail(signUpData.email).flatMap { userId =>
        userId match {
          // Email重複エラー
          case Some(email) => {
            val formToReturn = signUpForm.fill(signUpData)
            val formWithErrors =
              formToReturn
                .withError(
                  "emailDupulicate",
                  messagesApi("email.dupulicate")
                )
            departmentService.selectDepartmentList().map { departmentList =>
              BadRequest(
                views.html.users.register_user(formWithErrors, departmentList)
              )
            }

          }
          // 成功ケース
          case None => {
            val signUpUser = User(
              name = signUpData.name,
              email = signUpData.email,
              password = signUpData.password,
              departmentId = signUpData.departmentId
            )
            userService.insert(signUpUser).map { userId =>
              Redirect(routes.SignInController.toSignIn())
                .flashing("success" -> "ユーザ登録成功しました")
            }
          }
        }
      }
    }

    sentSignUpForm.fold(errorFunction, successFunction)
  }
}
