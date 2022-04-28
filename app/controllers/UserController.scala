package controllers

import play.mvc
import play.api.mvc.{
  MessagesControllerComponents,
  MessagesAbstractController,
  Action
}
import play.api.data.Form
import play.api.i18n.Lang
import play.api.cache._

import models.domains.User
import models.services.{UserService, DepartmentService}
import models.repositories.{PostRepository}
import views.html.defaultpages.error
import controllers.forms.UpdateUserProfileForm
import controllers.forms.{
  SignInForm,
  SignInFormData,
  SignUpForm,
  SignUpFormData,
  UpdateUserProfileForm,
  UpdateUserProfileFormData
}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import common._

class UserController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userAction: UserAction,
    userService: UserService,
    departmentService: DepartmentService,
    postService: PostRepository
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang
  val signInForm = SignInForm.signInForm
  val signUpForm = SignUpForm.signUpForm
  val updateUserProfileForm = UpdateUserProfileForm.updateUserProfileForm

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

  def edit(id: Long) = userAction.async { implicit request =>
    departmentService.selectDepartmentList().map { departmentList =>
      Ok(views.html.users.edit(updateUserProfileForm, departmentList))
    }
  }
  def update(id: Long) = userAction.async { implicit request =>
    Future.successful(
      Redirect(routes.UserController.index(1))
        .flashing("success" -> "プロフィールを更新しました。")
    )
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

  def signOut() = Action {
    Redirect(routes.UserController.signIn()).withNewSession
  }

  /** ユーザ登録ページ遷移
    *
    * @return
    *   ユーザ登録ページ
    */
  def toSignUp() = userAction.async { implicit request =>
    departmentService.selectDepartmentList().map { departmentList =>
      Ok(views.html.users.register_user(signUpForm, departmentList))
    }
  }

  /** ユーザ登録処理
    *
    * @return
    *   成功時: ログインページ 失敗時: ユーザ登録処理
    */
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
              Redirect(routes.UserController.toSignIn())
                .flashing("success" -> "ユーザ登録成功しました")
            }
          }
        }
      }
    }

    sentSignUpForm.fold(errorFunction, successFunction)
  }
}
