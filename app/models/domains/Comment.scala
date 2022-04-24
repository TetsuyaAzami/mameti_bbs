package models.domains

import anorm.Macro
import anorm.ToParameterList
import java.time.LocalDateTime

final case class Comment(
    commentId: Option[Long] = None,
    userId: Long,
    postId: Long,
    content: String,
    commentedAt: LocalDateTime
)

object Comment {
  implicit def toParameters: ToParameterList[Comment] =
    Macro.toParameters[Comment]
}

// データベースのカラムがnullになる場合、anormがパースできないためcontentをOption型に変更
final case class OptionComment(
    commentId: Option[Long] = None,
    userId: Option[Long],
    userWhoCommented: Option[UserWhoCommented],
    postId: Option[Long],
    content: Option[String],
    commentedAt: Option[LocalDateTime]
)
