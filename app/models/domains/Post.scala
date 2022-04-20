package models.domains

import java.time.LocalDateTime
import java.time.LocalDate

import anorm._

final case class Post(
    postId: Option[Long] = None,
    content: String,
    user: UserWhoPosted,
    postedAt: LocalDateTime
)

final case class PostForInsert(
    content: String,
    userId: Int,
    postedAt: LocalDateTime
)

object PostForInsert {
  implicit def toParameters: ToParameterList[PostForInsert] =
    Macro.toParameters[PostForInsert]
}
