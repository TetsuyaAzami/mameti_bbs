package models.domains

import anorm._

import java.time.LocalDateTime
import java.time.LocalDate

final case class Post(
    postId: Option[Long] = None,
    content: String,
    user: UserWhoPosted,
    postedAt: LocalDateTime,
    commentList: List[OptionComment]
)

// Postの編集のためのSELECT, UPDATEの際に使用
final case class PostForUpdate(
    postId: Long,
    content: String,
    postedAt: LocalDateTime
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
