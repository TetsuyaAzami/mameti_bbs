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
import scala.util.Failure
import scala.util.Success
import common._
import akka.actor

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
    // ページ表示権限確認
    if (userId != request.signInUser.userId) {
      Future.successful(
        Forbidden(
          views.html.errors
            .client_error(403, "Forbidden", messagesApi("error.http.forbidden"))
        )
      )
    } else {
      postService.findByUserId(userId).map { posts =>
        Ok(views.html.users.detail(posts))
      }
    }
  }

  // プロフィール編集
  def edit(userId: Long) = userNeedLoginAction.async { implicit request =>
    // ページ表示権限確認
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
        val sentUserForm = updateUserProfileForm.bindFromRequest()
        val signInUser = request.signInUser
        val uploadedProfileImgOpt = request.body.file("profileImg")

        // update権限確認
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
          // 拡張子チェック&エラーがあればリストとして取得
          val uploadedFileErrorList =
            FileUploadUtil.extractErrorsFromUploadedFile(uploadedProfileImgOpt)
          // uploadedFileのエラーを注入
          val formErrors = sentUserForm.errors.foldLeft(uploadedFileErrorList) {
            (acc, error) => acc :+ error
          }
          val formWithFileErrors =
            sentUserForm.copy(errors = formErrors)

          val errorFunction = {
            formWithErrors: Form[UpdateUserProfileFormData] =>
              departmentService.selectDepartmentList().map { departmentList =>
                BadRequest(
                  views.html.users.edit(formWithErrors, departmentList)
                )
              }
          }
          val successFunction = { userData: UpdateUserProfileFormData =>
            // アプリケーションサーバーに画像を保存
            val uploadedFilenameOpt = FileUploadUtil.saveToApplicationServer(
              uploadedProfileImgOpt,
              signInUser.email
            )
            val userDataWithImg =
              userData.copy(profileImg = uploadedFilenameOpt)

            userService.update(userDataWithImg).map { numberOfRowsUpdated =>
              Redirect(
                routes.UserController.detail(signInUser.userId)
              )
                .flashing("successUpdate" -> messagesApi("success.update"))
            }
          }
          formWithFileErrors.fold(errorFunction, successFunction)
        }
      }
}
