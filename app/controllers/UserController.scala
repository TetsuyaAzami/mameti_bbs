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

import models.domains.{User, UpdateUserProfileFormData}
import models.services.{UserService, PostService, DepartmentService}
import controllers.forms.{SignInFormData, SignUpFormData}
import controllers.forms.UpdateUserProfileForm._
import controllers.forms.SignInForm._
import controllers.forms.SignUpForm._

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
    postService: PostService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  // マイページ遷移
  def detail(userId: Long) = userAction.async { implicit request =>
    // ログインユーザのidと一致しているかのチェック あとで実装
    postService.findByUserId(userId).map { posts =>
      Ok(views.html.users.detail(posts))
    }
  }

  // プロフィール編集
  def edit(userId: Long) = userAction.async { implicit request =>
    // ログインユーザのuserIdと送られてきたuserIdが一致することを確認
    // 一致しない場合、403Forbiddenエラーを返す
    userService.findUserById(userId).flatMap { user =>
      user match {
        case None => {
          // ユーザがログインしていない場合はサインイン画面にリダイレクト
          Future.successful(Redirect(routes.SignInController.toSignIn()))
        }
        case Some(user) => {
          val formWithUserData = updateUserProfileForm.fill(user)
          departmentService.selectDepartmentList().map { departmentList =>
            Ok(views.html.users.edit(formWithUserData, departmentList))
          }
        }
      }
    }
  }

  // プロフィール更新
  def update() = userAction.async { implicit request =>
    val sentUserForm = updateUserProfileForm.bindFromRequest()
    // where userId = ログインユーザのid
    // バリデーション
    val errorFunction = { formWithErrors: Form[UpdateUserProfileFormData] =>
      // userDataのuserIdがログインユーザのIdと一致すること
      // 一致しない場合に403Forbiddenエラーを返す
      departmentService.selectDepartmentList().map { departmentList =>
        BadRequest(views.html.users.edit(formWithErrors, departmentList))
      }
    }
    val successFunction = { userData: UpdateUserProfileFormData =>
      userService.update(userData).map { numberOfRowsUpdated =>
        Redirect(routes.UserController.detail(1)) // ログインユーザIdに差し替え
          .flashing("success" -> messagesApi("update.success"))
      }
    }
    sentUserForm.fold(errorFunction, successFunction)
  }
}
