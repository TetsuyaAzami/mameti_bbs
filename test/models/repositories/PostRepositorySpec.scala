package models.repositories

import models.DatabaseExecutionContext
import play.api.db.DBApi
import play.api.test.Injecting
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Span, Seconds, Millis}
import models.domains.UserWhoPosted
import java.time.LocalDateTime
import models.domains.Post
import models.domains.Comment
import play.api.db.evolutions.Evolutions
import com.fasterxml.jackson.module.scala.deser.overrides

class PostRepositorySpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfter
    with BeforeAndAfterEach {
  val dbApi = inject[DBApi]
  val db = dbApi.database("default")
  val userRepository = inject[UserRepository]
  val commentRepository = inject[CommentRepository]
  val likeRepository = inject[LikeRepository]
  implicit val databaseExecutionContext = inject[DatabaseExecutionContext]

  override protected def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(db)
    Evolutions.applyEvolutions(db)
  }

  val postRepository =
    new PostRepository(dbApi, userRepository, commentRepository, likeRepository)

  "PostRepository#findAll" should {
    "投稿idが1~4までのものが取得できること" in {
      val result = postRepository.findAll()
      whenReady(result, timeout(Span(30, Seconds))) { result =>
        val postIds = result.map(_._1.postId.get)
        val expectedPostIds = 1 to 4
        val size = result.size
        expectedPostIds.foreach(postId => assert(postIds.contains(postId)))
        assert(postIds.contains(Some(5)) == false)
        assert(size == 4)
      }
    }
  }

  "PostRepository#findAllWithFlag" should {
    "部署がフロントエンドの投稿が取得できること" in {
      val result = postRepository.findAllWithFlag("フロントエンド")
      whenReady(result) { result =>
        val departments = result.map(_._1.user.get.department.get)

        assert(departments.forall(_.name == "フロントエンド"))
        assert(result.size == 2)
      }
    }
    "部署がバックエンドの投稿が取得できること" in {
      val result = postRepository.findAllWithFlag("バックエンド")
      whenReady(result) { result =>
        val departments = result.map(_._1.user.get.department.get)

        assert(departments.forall(_.name == "バックエンド"))
        assert(result.size == 2)
      }
    }
    "部署がクラウドの投稿が取得できること" in {
      val result = postRepository.findAllWithFlag("クラウド")
      whenReady(result) { result =>
        val departments = result.map(_._1.user.get.department.get)

        assert(departments.forall(_.name == "クラウド"))
        assert(result.size == 0)
      }
    }
    "不明な部署がフラグに指定された場合、空のリストを返すこと" in {
      val result = postRepository.findAllWithFlag("hogehoge")
      whenReady(result) { result =>
        assert(result.size == 0)
      }
    }
    "フラグに空が指定された場合、空のリストを返すこと" in {
      val result = postRepository.findAllWithFlag("")
      whenReady(result) { result =>
        assert(result.size == 0)
      }
    }
  }

  "PostRepository#findByUserId" should {
    "userId = 1の時" should {
      "userId = 1の投稿が正しく取得できること" in {
        val result = postRepository.findByUserId(1)
        whenReady(result) { result =>
          val contents = result.map(_._1.content)
          assert(result.forall(_._1.userId == 1))
          assert(contents.contains("user1投稿1"))
          assert(contents.contains("user1投稿2"))
          assert(result.size == 2)
        }
      }
    }
    "userId = 2の時" should {
      "userId = 2の投稿が正しく取得できること" in {
        val result = postRepository.findByUserId(2)
        whenReady(result) { result =>
          val contents = result.map(_._1.content)
          assert(result.forall(_._1.userId == 2))
          assert(contents.contains("user2投稿1"))
          assert(contents.contains("user2投稿2"))
          assert(result.size == 2)
        }
      }
    }
    "存在しないuserIdの時" should {
      "空のリストが返ること" in {
        val result = postRepository.findByUserId(3)
        whenReady(result) { result =>
          assert(result.size == 0)
        }
      }
    }
  }

  "PostRepository#findByPostIdAndUserId" should {
    "postId = 1 かつ userId = 1のとき" should {
      "postId = 1 かつ userId = 1のデータが正しく取得できること" in {
        val result = postRepository.findByPostIdAndUserId(1, 1)
        whenReady(result) { result =>
          assert(result.map(_.content).get == "user1投稿1")
        }
      }
    }

    "postId = 3 かつ userId = 2のとき" should {
      "postId = 3 かつ userId = 2のデータが正しく取得できること" in {
        val result = postRepository.findByPostIdAndUserId(3, 2)
        whenReady(result) { result =>
          assert(result.map(_.content).get == "user2投稿1")
        }
      }
    }

    "postIdとuserIdが存在しない組み合わせの時" should {
      "Noneが返ること" in {
        val result = postRepository.findByPostIdAndUserId(1, 2)
        whenReady(result) { result =>
          assert(result == None)
        }
      }
    }
  }

  "PostRepository#findByPostIdWithCommentList" should {
    "postId = 1の時" should {
      "content = user1投稿1 かつ ユーザ2からのコメントが3件、ユーザ3からのコメントが2件取得できること" in {
        val result = postRepository.findByPostIdWithCommentList(1)
        whenReady(result) { result =>
          val post = result._1.get
          val user2Comments = post.commentList.filter(_.userId == 2)
          val user3Comments = post.commentList.filter(_.userId == 3)

          assert(post.content == "user1投稿1")

          // user2のコメント
          assert(user2Comments.map(_.content).contains("post1に対するユーザ2のコメント1"))
          assert(user2Comments.map(_.content).contains("post1に対するユーザ2のコメント2"))
          assert(user2Comments.map(_.content).contains("post1に対するユーザ2のコメント3"))

          // user3のコメント
          assert(user3Comments.map(_.content).contains("post1に対するユーザ3のコメント4"))
          assert(user3Comments.map(_.content).contains("post1に対するユーザ3のコメント5"))
        }
      }
    }
    "postId = 3の時" should {
      "content = user2投稿1 かつ コメントのリストが空で取得できること" in {
        val result = postRepository.findByPostIdWithCommentList(3)
        whenReady(result) { result =>
          val post = result._1.get
          val comments = post.commentList

          assert(post.content == "user2投稿1")
          assert(comments == List())
        }
      }
    }
    "postId = 100の時(存在しないpostIdが指定された時)" should {
      "(None,Nil)が返ること" in {
        val result = postRepository.findByPostIdWithCommentList(100)
        whenReady(result) { result =>
          assert(result == (None, Nil))
        }
      }
    }
  }

  "PostRepository#update" should {
    "postId = 1 かつ userId = 1のcontentと投稿日が更新されること" in {
      val mockUserWhoPosted = mock[UserWhoPosted]
      val mockComments = mock[List[Comment]]
      val post =
        Post(
          Some(1),
          "更新しました",
          1,
          Some(mockUserWhoPosted),
          LocalDateTime.now(),
          mockComments
        )
      val result = postRepository.update(post, 1)
      whenReady(result) { result =>
        assert(result == 1)
        val updatedPost = postRepository.findByPostIdAndUserId(1, 1)
        whenReady(updatedPost) { updatedPost =>
          assert(updatedPost.get.content == "更新しました")
          assert(
            updatedPost.get.postedAt.getYear == LocalDateTime
              .now()
              .getYear()
          )
          assert(
            updatedPost.get.postedAt.getMonth == LocalDateTime
              .now()
              .getMonth()
          )
          assert(
            updatedPost.get.postedAt.getDayOfYear == LocalDateTime
              .now()
              .getDayOfYear()
          )
        }
      }
    }

    "存在しない投稿の更新を行おうとした場合" should {
      "更新件数0件が返ってくること" in {
        val mockUserWhoPosted = mock[UserWhoPosted]
        val mockComments = mock[List[Comment]]
        val post =
          Post(
            Some(100),
            "存在しない投稿の更新",
            1,
            Some(mockUserWhoPosted),
            LocalDateTime.now(),
            mockComments
          )

        val result = postRepository.update(post, 1)
        whenReady(result) { result =>
          assert(result == 0)
        }
      }
    }

    "他人の投稿の更新を行おうとした場合" should {
      "更新件数0件が返ってくること" in {
        val mockUserWhoPosted = mock[UserWhoPosted]
        val mockComments = mock[List[Comment]]
        val post =
          Post(
            Some(1),
            "user1が持っている投稿",
            1,
            Some(mockUserWhoPosted),
            LocalDateTime.now(),
            mockComments
          )

        val result = postRepository.update(post, 2)
        whenReady(result) { result =>
          assert(result == 0)
        }
      }
    }
  }

  "postRepository#insert" should {
    val mockUserWhoPosted = mock[UserWhoPosted]
    val mockComments = mock[List[Comment]]
    val post = Post(
      None,
      "投稿insertテスト",
      1,
      Some(mockUserWhoPosted),
      LocalDateTime.now(),
      mockComments
    )
    "投稿が正常に行われること" in {
      val result = postRepository.insert(post)
      whenReady(result) { result =>
        val insertedPostId = result.get
        val insertedPost =
          postRepository.findByPostIdWithCommentList(insertedPostId)
        whenReady(insertedPost) { insertedPost =>
          val post = insertedPost._1.get
          val comments = insertedPost._1.get.commentList
          val likes = insertedPost._2

          assert(post.content == "投稿insertテスト")
          assert(post.userId == 1)
          assert(post.postedAt.getYear() == LocalDateTime.now().getYear())
          assert(post.postedAt.getMonth() == LocalDateTime.now().getMonth())
          assert(
            post.postedAt.getDayOfMonth() == LocalDateTime.now().getDayOfMonth()
          )
          assert(comments == Nil)
          assert(likes == Nil)
        }
      }
    }
  }

  "postRepository#delete" should {
    "自分の投稿を削除できること" in {
      val deleteCount = postRepository.delete(1, 1)
      whenReady(deleteCount) { deleteCount =>
        assert(deleteCount == 1)
        val deletedPost = postRepository.findByPostIdAndUserId(1, 1)
        whenReady(deletedPost) { deletedPost =>
          assert(deletedPost == None)
        }
      }
    }

    "他人の投稿を削除できないこと" in {
      val deleteCount = postRepository.delete(1, 2)
      whenReady(deleteCount) { deleteCount =>
        assert(deleteCount == 0)

        // 削除されそうになった投稿が存在していること
        val deleteTargetedPost = postRepository.findByPostIdAndUserId(1, 1)
        whenReady(deleteTargetedPost) { deleteTargetedPost =>
          assert(deleteTargetedPost.get.content == "user1投稿1")
        }
      }
    }

    "存在していない投稿を削除しようとした場合、0が返ること" in {
      val deleteCount = postRepository.delete(100, 1)
      whenReady(deleteCount) { deleteCount =>
        assert(deleteCount == 0)
      }
    }
  }

}
