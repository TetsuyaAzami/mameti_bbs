package models.services

import models.repositories.CommentRepository
import models.domains.Comment
import javax.inject._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

@Singleton
class CommentService @Inject() (commentRepository: CommentRepository)(
    ec: ExecutionContext
) {
  def insert(comment: Comment): Future[Option[Long]] =
    commentRepository.insert(comment)

  def findByIdWithUser(commentId: Long): Future[Comment] =
    commentRepository.findByIdWithUser(commentId)
}
