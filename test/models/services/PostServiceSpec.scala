package models.services

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures

import models.domains._
import models.repositories.PostRepository
import java.time.LocalDateTime
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {
  "PostService#findAll" should {
    "投稿日の降順で並び替わる" in {
      // レポジトリのMockを用意
      val mockPostRepository = mock[PostRepository]
      when(mockPostRepository.findAll())
        .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

      // 全件取得
      val postService = new PostService(mockPostRepository)
      val allPostsSortedWithPostedAt = postService.findAll()

      // メソッドが1度だけ呼ばれていること
      verify(mockPostRepository, times(1)).findAll()

      // 日付のリスト
      val postedAtList = allPostsSortedWithPostedAt.futureValue.map { tapple =>
        val post = tapple._1
        post.postedAt
      }

      // 日付の降順になっていること
      assert(postedAtList.sorted.reverse == postedAtList)
    }
  }

  "PostService#findAllWithFlag" should {
    "sortByOptがlikeの場合" should {
      "部署名がNoneの場合、findAllが呼ばれる" in {
        val mockPostRepository = mock[PostRepository]
        when(mockPostRepository.findAll())
          .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

        val postService = new PostService(mockPostRepository)
        postService.findAllWithFlag(None, Some("like"))

        verify(mockPostRepository, times(1)).findAll()
      }

      "部署名がSomeの場合、いいね数順で並び替わる" in {
        // レポジトリのMockを用意
        val mockPostRepository = mock[PostRepository]
        when(mockPostRepository.findAllWithFlag("フロントエンド"))
          .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

        // フロントエンドのいいね順で取得
        val postService = new PostService(mockPostRepository)
        val frontendLikeOrder =
          postService.findAllWithFlag(Some("フロントエンド"), Some("like"))

        // メソッドが1度だけ呼ばれていること
        verify(mockPostRepository, times(1)).findAllWithFlag("フロントエンド")

        // いいね数のリスト
        val likeCountList = frontendLikeOrder.map { result =>
          val likeLists = result.map(_._3)
          likeLists.map(likeList => likeList.size)
        }.futureValue

        // いいね順になっていること
        assert(
          likeCountList.sorted.reverse == likeCountList
        )
      }
    }

    "sortByOptが予期せぬ値の場合" should {
      "空のリストが返る" in {
        // レポジトリのMockを用意
        val mockPostRepository = mock[PostRepository]
        val postService = new PostService(mockPostRepository)

        val result =
          postService
            .findAllWithFlag(Some("フロントエンド"), Some("hogehoge"))
            .futureValue

        // PostRepositoryが呼ばれないこと
        verify(mockPostRepository, times(0)).findAllWithFlag("フロントエンド")
        verify(mockPostRepository, times(0)).findAll()

        // 空のリストが返ること
        assert(result == Nil)
      }
    }
  }

  "PostService#findByUserId" should {
    "投稿日の降順で並び替わる" in {
      // レポジトリのMockを用意
      val mockPostRepository = mock[PostRepository]
      when(mockPostRepository.findByUserId(1))
        .thenReturn(Future { common.PostRepositoryTestData.selectTestData })

      val postService = new PostService(mockPostRepository)
      val postedAtList = postService.findByUserId(1).futureValue.map { tapple =>
        val post = tapple._1
        post.postedAt
      }

      assert(postedAtList.sorted.reverse == postedAtList)
    }
  }

  "PostService#findByPostIdWithCommentList" should {
    "postIdに一致する投稿が見つからなかったとき" should {
      "(None,Nil)が返る" in {
        val mockPostRepository = mock[PostRepository]
        when(mockPostRepository.findByPostIdWithCommentList(1))
          .thenReturn((Future { (None, Nil) }))

        val postService = new PostService(mockPostRepository)
        val result = postService.findByPostIdWithCommentList(1).futureValue
        assert((None, Nil) == result)
      }
    }
  }

  "postIdに一致する投稿が見つかったとき" should {
    "コメントリストの投稿日の降順で並び替わる" in {
      val mockPostRepository = mock[PostRepository]
      when(mockPostRepository.findByPostIdWithCommentList(1))
        .thenReturn((Future {
          common.PostRepositoryTestData.findByPostIdWithCommentList
        }))

      // 投稿に紐づくコメントリストを取得
      val postService = new PostService(mockPostRepository)
      val result = postService.findByPostIdWithCommentList(1).futureValue
      val commentList = result._1.get.commentList
      // コメントリストの日付をリストで取得
      val commentedAtList = commentList.map(comment => comment.commentedAt)

      // 日付が降順に並んでいること
      assert(commentedAtList.sorted.reverse == commentedAtList)
    }
  }
}
