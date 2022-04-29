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

// Postをinsertする際に使用
final case class PostForInsert(
    content: String,
    userId: Int,
    postedAt: LocalDateTime
)

object PostForInsert {
  implicit def toParameters: ToParameterList[PostForInsert] =
    Macro.toParameters[PostForInsert]
}

case class PostFormData(content: String)
case class PostUpdateFormData(
    postId: Long,
    content: String
)
