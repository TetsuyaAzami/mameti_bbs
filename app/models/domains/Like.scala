package models.domains

case class Like(
    likeId: Option[Long],
    userId: Long,
    postId: Long
)
