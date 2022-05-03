package controllers

import play.api.mvc.{
  MessagesControllerComponents,
  MessagesAbstractController,
  Action,
  Result
}
import play.api.data.Form
import play.api.i18n.Lang
import play.api.cache._

import models.domains.{User, UpdateUserProfileFormData, SignInUser}
import models.services.{UserService, PostService, DepartmentService}
import controllers.forms.{SignInFormData, SignUpFormData}
import controllers.forms.UpdateUserProfileForm._
import controllers.forms.SignInForm._
import controllers.forms.SignUpForm._

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import common._

@Singleton
class UserController @Inject() (
    mcc: MessagesControllerComponents,
    cache: SyncCacheApi,
    userOptAction: UserOptAction,
    userNeedLoginAction: UserNeedLoginAction,
    userNeedAuthorityAction: UserNeedAuthorityAction,
    userService: UserService,
    departmentService: DepartmentService,
    postService: PostService,
    fileUploadUtil: FileUploadUtil
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  // マイページ遷移
  def detail(userId: Long) = userNeedLoginAction.async { implicit request =>
    // ログインユーザのidと一致しているかのチェック あとで実装
    postService.findByUserId(userId).map { posts =>
      Ok(views.html.users.detail(posts))
    }
  }

  // プロフィール編集
  def edit(userId: Long) = userNeedLoginAction.async { implicit request =>
    // ログインユーザのuserIdと送られてきたuserIdが一致することを確認
    // 一致しない場合、403Forbiddenエラーを返す
    if (userId != request.signInUser.userId) {
      Future.successful(
        Forbidden(
          views.html.errors
            .client_error(403, "Forbidden", messagesApi("error.http.forbidden"))
        )
      )
    } else {
      userService.findUserById(userId).flatMap { user =>
        user match {
          case None => {
            // ユーザが存在していない場合はNotFound
            Future.successful(
              NotFound(
                views.html.errors
                  .client_error(
                    404,
                    "Not Found",
                    messagesApi("error.http.notFound")
                  )
              )
            )
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
  }

  // プロフィール更新
  def update() =
    userNeedAuthorityAction(
      parse.multipartFormData(fileUploadUtil.handleFilePartAsFile)
    )
      .async { implicit request =>
        // userDataのuserIdがログインユーザのIdと一致すること
        // 一致しない場合に403Forbiddenエラーを返す
        val sentUserForm = updateUserProfileForm.bindFromRequest()
        val signInUser = request.signInUser
        val uploadedProfileImg = request.body.file("profileImg")
        if (sentUserForm.data("userId").toLong != signInUser.userId) {
          Future.successful(
            Forbidden(
              views.html.errors
                .client_error(
                  403,
                  "Forbidden",
                  messagesApi("error.http.forbidden")
                )
            )
          )
        } else {
          // プロフィール画像がアップロードされたらアプリケーションサーバーに保存する
          uploadedProfileImg match {
            case None => {}
            case Some(uploadedProfileImg) =>
              fileUploadUtil.saveToApplicationServer(
                uploadedProfileImg,
                signInUser.email
              )
          }

          val errorFunction = {
            formWithErrors: Form[UpdateUserProfileFormData] =>
              departmentService.selectDepartmentList().map { departmentList =>
                BadRequest(
                  views.html.users.edit(formWithErrors, departmentList)
                )
              }
          }
          val successFunction = { userData: UpdateUserProfileFormData =>
            val userDataWithImg =
              userData.copy(profileImg = Option(signInUser.email))
            userService.update(userDataWithImg).map { numberOfRowsUpdated =>
              Redirect(
                routes.UserController.detail(signInUser.userId)
              )
                .flashing("successUpdate" -> messagesApi("success.update"))
            }
          }
          sentUserForm.fold(errorFunction, successFunction)
        }
      }
}
