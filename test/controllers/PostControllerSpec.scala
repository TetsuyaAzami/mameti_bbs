package controllers

import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import play.api.mvc.Results
import play.api.BuiltInComponents
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.components.OneAppPerTestWithComponents
import org.mockito.Mockito._

import models.domains.SignInUser
import models.services.PostService
import controllers.forms.PostForm._
import controllers.forms.CommentForm._
import common.UserOptAction
import common.UserNeedLoginAction
import common.errors.ErrorHandler
import common.CacheUtil
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.time.LocalDate
import models.domains.Department
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.BodyParsers
import play.api.i18n.Lang
import play.api.mvc.Flash

class PostControllerSpec
    extends PlaySpec
    with ScalaFutures
    with Results
    with GuiceOneAppPerSuite
    with Injecting
    with MockitoSugar {
  val signInUser =
    SignInUser(
      1,
      "user1",
      "email1@example.com",
      "password1",
      Some(LocalDate.of(2000, 11, 11)),
      Some("user1です"),
      Some("email1@example.com.png"),
      1,
      mock[Department]
    )

  val mockMessagesControllerComponents = mock[MessagesControllerComponents]

  // userOptAction生成
  val mockCacheUtil = mock[CacheUtil]
  when(mockCacheUtil.getSessionUser(Some("testSessionId")))
    .thenReturn(Some(signInUser))
  when(mockCacheUtil.getSessionUser(None))
    .thenReturn(None)
  when(mockCacheUtil.getSessionUser(Some("NoSessionId")))
    .thenReturn(None)

  val userOptAction =
    new UserOptAction(
      inject[BodyParsers.Default],
      mockCacheUtil,
      inject[MessagesApi]
    )
  // userNeedLoginAction生成
  val userNeedLoginAction = inject[UserNeedLoginAction]
  val mockPostService = mock[PostService]
  val mockErrorHandler = mock[ErrorHandler]

  when(mockPostService.findAll())
    .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

  val controller =
    new PostController(
      inject[MessagesControllerComponents],
      userOptAction,
      userNeedLoginAction,
      mockPostService,
      mockErrorHandler
    )

  "PostController#index" should {
    "path = /, method = GETの場合、正常に描画されること" in {
      val index = controller.index().apply(FakeRequest(GET, "/").withCSRFToken)

      status(index) mustBe OK
      contentType(index) mustBe Some("text/html")
      contentAsString(index) must include("みんなの豆知識一覧")
      contentAsString(index) must include("テスト投稿1")
      contentAsString(index) must include("テスト投稿5")
    }
    "path = /posts, method = GETの場合、正常に描画されること" in {
      val index =
        controller.index().apply(FakeRequest(GET, "/posts").withCSRFToken)

      status(index) mustBe OK
      contentType(index) mustBe Some("text/html")
      contentAsString(index) must include("みんなの豆知識一覧")
      contentAsString(index) must include("テスト投稿1")
      contentAsString(index) must include("テスト投稿5")
    }
  }

  "PostController#detail" should {
    "リクエスト情報にログインユーザがない場合" should {
      "ログインページにリダイレクトすること" in {
        val insert = controller
          .insert()
          .apply(FakeRequest().withSession("sessionId" -> "NoSessionId"))
        status(insert) mustBe SEE_OTHER
        redirectLocation(insert) mustBe (Some("/users/sign-in"))
        flash(insert) mustBe Flash(Map("errorNeedSignIn" -> "ログインしてください"))
      }
    }
  }
}
