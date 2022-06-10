package common

import models.domains.Post
import models.domains.UserWhoPosted
import java.time.LocalDateTime
import models.domains.Like
import models.domains.Department
import models.domains.Comment
import models.domains.UserWhoCommented

object PostRepositoryTestData {
  // findAllWithFlagメソッドのテストデータ
  val selectTestData: List[(Post, Option[Long], List[Like])] = List(
    (
      Post(
        Some(1),
        "テスト投稿1",
        1,
        Some(
          UserWhoPosted(
            1,
            "user1",
            Some("email1@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 11, 11, 11, 11),
        List()
      ),
      Some(1.toLong),
      List(
        Like(Some(1), 2, 1),
        Like(Some(2), 3, 1),
        Like(Some(3), 4, 1),
        Like(Some(4), 5, 1)
      )
    ),
    (
      Post(
        Some(2),
        "テスト投稿2",
        1,
        Some(
          UserWhoPosted(
            1,
            "user1",
            Some("email1@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 2, 22, 22, 22),
        List()
      ),
      Some(1.toLong),
      List(
        Like(Some(5), 2, 2),
        Like(Some(6), 3, 2),
        Like(Some(7), 4, 2)
      )
    ),
    (
      Post(
        Some(3),
        "テスト投稿3",
        2,
        Some(
          UserWhoPosted(
            2,
            "user2",
            Some("email2@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 3, 3, 3, 3, 3),
        List()
      ),
      Some(3.toLong),
      List(
        Like(Some(8), 1, 3),
        Like(Some(9), 3, 3)
      )
    ),
    (
      Post(
        Some(4),
        "テスト投稿4",
        2,
        Some(
          UserWhoPosted(
            2,
            "user2",
            Some("email2@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 4, 4, 4, 4, 4),
        List()
      ),
      Some(4.toLong),
      List(
        Like(Some(10), 1, 4)
      )
    ),
    (
      Post(
        Some(5),
        "テスト投稿5",
        2,
        Some(
          UserWhoPosted(
            2,
            "user2",
            Some("email2@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 5, 5, 5, 5, 5),
        List()
      ),
      Some(5.toLong),
      List()
    )
  )

  val findByPostIdWithCommentList: (Option[Post], List[Like]) = (
    Some(
      Post(
        Some(1),
        "テスト投稿1",
        1,
        Some(
          UserWhoPosted(
            1,
            "user1",
            Some("email1@example.com.jpg"),
            Some(Department(1, "フロントエンド"))
          )
        ),
        LocalDateTime.of(2022, 11, 11, 11, 11),
        List(
          Comment(
            Some(1),
            2,
            Some(UserWhoCommented(2, "user1", Some("email2@example.com.png"))),
            1,
            "コメント1",
            LocalDateTime.of(2022, 11, 11, 11, 11)
          ),
          Comment(
            Some(2),
            3,
            Some(UserWhoCommented(3, "user1", Some("email3@example.com.png"))),
            1,
            "コメント2",
            LocalDateTime.of(2022, 2, 22, 22, 22)
          ),
          Comment(
            Some(3),
            4,
            Some(UserWhoCommented(4, "user4", Some("email4@example.com.png"))),
            1,
            "コメント3",
            LocalDateTime.of(2022, 3, 3, 3, 3)
          )
        )
      )
    ),
    List()
  )
}
