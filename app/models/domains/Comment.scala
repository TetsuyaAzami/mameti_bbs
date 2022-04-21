package models.domains

import java.time.LocalDateTime

final case class Comment(
    commentId: Option[Long] = None,
    userId: Option[Long],
    postId: Option[Long],
    content: String,
    commentedAt: Option[LocalDateTime]
)
