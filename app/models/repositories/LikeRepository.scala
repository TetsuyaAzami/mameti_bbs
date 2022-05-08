package models.repositories

import play.api.db.DBApi
import anorm._
import anorm.SqlParser._

import models.DatabaseExecutionContext
import models.domains.Like

import javax.inject._

class LikeRepository @Inject() (
    dbApi: DBApi
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple = {
    get[Option[Long]]("l_like_id") ~
      get[Long]("l_user_id") ~
      get[Long]("l_post_id") map { case likeId ~ userId ~ postId =>
        Like(likeId, userId, postId)
      }
  }
}
