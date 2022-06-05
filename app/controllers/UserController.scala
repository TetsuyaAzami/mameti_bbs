package controllers

import play.api.mvc.{
  MessagesControllerComponents,
  MessagesAbstractController,
  Action,
  Result
}
import play.api.data.Form
import play.api.i18n.Lang
import akka.actor

import models.domains.{User, UpdateUserProfileFormData}
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
import common.errors._

@Singleton
class UserController @Inject() (
    mcc: MessagesControllerComponents,
    cache: CacheUtil,
    userOptAction: UserOptAction,
    userNeedLoginAction: UserNeedLoginAction,
    userService: UserService,
    departmentService: DepartmentService,
    postService: PostService,
    fileUploadUtil: FileUploadUtil,
    errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) {
  implicit val lang = Lang.defaultLang

  // マイページ遷移
  def detail(userId: Long) = userNeedLoginAction.async { implicit request =>
    // ページ表示権限確認(認可)
    if (userId != request.signInUser.userId) {
      errorHandler.onClientError(request, 403, "")
    } else {
      postService.findByUserId(userId).map { result =>
        Ok(views.html.users.detail(result))
      }
    }
  }

  // プロフィール編集
  def edit(userId: Long) = userNeedLoginAction.async { implicit request =>
    val signInUser = request.signInUser
    val signInUserId = signInUser.userId
    // ページ表示権限確認(認可)
    if (userId != signInUserId) {
      errorHandler.onClientError(request, 403, "")
    } else {
      userService.findUserById(signInUserId).flatMap { user =>
        user match {
          case None => {
            // ユーザが存在していない場合はNotFound
            errorHandler.onClientError(request, 404, "")
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
    userNeedLoginAction(
      parse.multipartFormData(fileUploadUtil.handleFilePartAsFile)
    )
      .async { implicit request =>
        val sentUserForm = updateUserProfileForm.bindFromRequest()
        val signInUser = request.signInUser
        val sessionId = request.session.get("sessionId")
        // update権限確認(認可)
        if (sentUserForm.data("userId").toLong != signInUser.userId) {
          errorHandler.onClientError(request, 403, "")
        } else {
          val uploadedProfileImgOpt = request.body.file("profileImg")
          // 拡張子チェック&エラーがあればリストとして取得
          val uploadedFileErrorList =
            fileUploadUtil.extractErrorsFromUploadedFile(
              uploadedProfileImgOpt
            )
          // uploadedFileのエラーを注入
          val formErrors =
            sentUserForm.errors.foldLeft(uploadedFileErrorList) {
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
            val uploadedFilenameOpt = fileUploadUtil.save(
              uploadedProfileImgOpt,
              signInUser.email
            )
            val userDataWithImg =
              userData.copy(profileImg = uploadedFilenameOpt)

            userService.update(userDataWithImg).flatMap { numberOfRowsUpdated =>
              userService
                .findSignInUserById(signInUser.userId)
                .map { updatedUserOpt =>
                  val updatedUser = updatedUserOpt.get
                  cache.setSessionUser(sessionId, updatedUser)
                  // 画像の更新を待つ
                  Thread.sleep(800)
                  Redirect(
                    routes.UserController.detail(signInUser.userId)
                  )
                    .flashing("successUpdate" -> messagesApi("success.update"))
                }
            }
          }
          formWithFileErrors.fold(errorFunction, successFunction)
        }
      }
}
