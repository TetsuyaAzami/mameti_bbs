package models.repositories

import models.DatabaseExecutionContext
import models.domains.Comment

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime
import models.domains.OptionComment
import scala.concurrent.Future

class CommentRepository @Inject() (
    dbApi: DBApi,
    userRepository: UserRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val optionCommentWithUserParser = {
    get[Option[Long]]("c_comment_id") ~
      get[Option[Long]]("c_user_id") ~
      userRepository.userWhoCommentedParser.? ~
      get[Option[Long]]("c_post_id") ~
      get[Option[String]]("c_content") ~
      get[Option[LocalDateTime]]("c_commented_at") map {
        case commentId ~ userId ~ userWhoCommented ~ postId ~ content ~ commentedAt =>
          OptionComment(
            commentId,
            userId,
            userWhoCommented,
            postId,
            content,
            commentedAt
          )
      }
  }

  def insert(): Future[Long] = Future {
    db.withConnection { implicit conn =>
      {
        1
      }
    }
  }
}
