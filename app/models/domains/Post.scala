package models.domains

import anorm._

import java.time.{LocalDateTime, LocalDate}

final case class Post(
    postId: Option[Long] = None,
    content: String,
    userId: Long,
    user: Option[UserWhoPosted],
    postedAt: LocalDateTime,
    commentList: List[Comment]
)

// insertの際に使用
case class PostFormData(content: String)
// updateの際に使用
case class PostUpdateFormData(
    postId: Long,
    content: String
)
