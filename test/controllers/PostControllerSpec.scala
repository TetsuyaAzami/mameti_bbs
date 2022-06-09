// package controllers

// import play.api.test._
// import play.api.test.Helpers._
// import org.scalatestplus.play._
// import org.scalatestplus.play.guice._
// import org.scalatestplus.mockito.MockitoSugar
// import org.mockito.Mockito._

// import models.services.PostService
// import controllers.forms.PostForm._
// import controllers.forms.CommentForm._
// import common.UserOptAction
// import common.UserNeedLoginAction
// import common.errors.ErrorHandler
// import scala.concurrent.ExecutionContext.Implicits.global
// import scala.concurrent.Future
// import org.scalatest.concurrent.ScalaFutures
// import play.api.mvc.Results
// import org.scalatestplus.play.components.OneAppPerTestWithComponents
// import play.api.BuiltInComponents
// import common.CacheUtil
// import play.api.i18n.MessagesApi
// import models.domains.SignInUser
// import play.api.mvc.Action

// class PostControllerSpec
//     extends PlaySpec
//     with ScalaFutures
//     with Results
//     with MockitoSugar {

//   "PostController#index" should {
//     "正常に描画されること" in {
//       // モックユーザの作成
//       val mockSignInUser = mock[SignInUser]
//       when(mockSignInUser.userId).thenReturn(1)
//       when(mockSignInUser.name).thenReturn("testユーザ")
//       when(mockSignInUser.email).thenReturn("testUser@example.com")
//       when(mockSignInUser.birthday).thenReturn(None)
//       when(mockSignInUser.introduce).thenReturn(None)
//       when(mockSignInUser.profileImg).thenReturn(None)
//       when(mockSignInUser.departmentId).thenReturn(1)

//       val messagesControllerComponents =
//         Helpers.stubMessagesControllerComponents()
//       val mockCache = mock[CacheUtil]
//       when(mockCache.getSessionUser(Some("testSessionId")))
//         .thenReturn(Some(mockSignInUser))
//       val mockMessagesApi = mock[MessagesApi]

//       val userOptAction =
//         new UserOptAction(
//           new play.api.mvc.BodyParsers.Default(
//             messagesControllerComponents.parsers
//           ),
//           mockCache,
//           mockMessagesApi
//         )(
//           messagesControllerComponents.executionContext
//         )

//       val mockUserNeedLoginAction = mock[UserNeedLoginAction]
//       val mockPostService = mock[PostService]
//       val mockErrorHandler = mock[ErrorHandler]

//       when(mockPostService.findAll())
//         .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

//       val controller =
//         new PostController(
//           messagesControllerComponents,
//           userOptAction,
//           mockUserNeedLoginAction,
//           mockPostService,
//           mockErrorHandler
//         )

//       val action: UserOptAction = Action { request =>
//         val value = (request.body.asJson.get \ "field").as[String]
//         Ok(value)
//       }

//       val index =
//         call(userOptAction { request =>
//           Results.Ok("Yikes")
//         })

//       status(index) mustBe OK
//       contentType(index) mustBe "text/html"
//       contentAsString(index) must include("みんなの豆知識一覧")
//     }
//   }
// }
