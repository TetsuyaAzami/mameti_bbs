package models.domains

import java.time.LocalDateTime

final case class Comment(
    commentId: Option[Long] = None,
    userId: Option[Long],
    postId: Option[Long],
    content: String,
    commentedAt: Option[LocalDateTime]
)

// データベースのカラムがnullになる場合、anormがパースできないためcontentをOption型に変更
final case class OptionComment(
    commentId: Option[Long] = None,
    userId: Option[Long],
    postId: Option[Long],
    content: Option[String],
    commentedAt: Option[LocalDateTime]
)
