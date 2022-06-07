package models.services

import org.scalatestplus.play.PlaySpec
import models.domains._
import models.repositories.PostRepository
import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures

class PostServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {
  "PostService#findAllWithFlag" should {
    "いいね数順で並び替わる" in {
      // レポジトリのMockを用意
      val mockPostRepository = mock[PostRepository]
      when(mockPostRepository.findAllWithFlag(Some("フロントエンド")))
        .thenReturn(Future { common.PostRepositoryTestData.findAllWithFlag })

      // フロントエンドのいいね順で取得
      val postService = new PostService(mockPostRepository)
      val frontendLikeOrder =
        postService.findAllWithFlag(Some("フロントエンド"), Some("like"))

      // メソッドが1度だけ呼ばれていること
      verify(mockPostRepository, times(1)).findAllWithFlag(Some("フロントエンド"))

      // いいね数のリスト
      val likeCountList = frontendLikeOrder.map { result =>
        val likeLists = result.map(_._3)
        likeLists.map(likeList => likeList.size)
      }

      // いいね順になっていること
      assert(likeCountList.futureValue == List(4, 3, 2, 1, 0))
    }
  }
}
