package models.domains

import java.time.LocalDateTime

final case class Post(
    postId: Option[Long] = None,
    content: String,
    user: UserWhoPosted,
    postedAt: Option[LocalDateTime] = None
)
