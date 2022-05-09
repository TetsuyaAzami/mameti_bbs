package models.repositories

import play.api.db.DBApi
import anorm._
import anorm.SqlParser._

import models.DatabaseExecutionContext
import models.domains.Like

import javax.inject._
import scala.concurrent.Future

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

  def insert(like: Like): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      INSERT INTO likes (user_id, post_id)
                  VALUES (${like.userId},${like.postId});
      """.executeInsert()
    }
  }

  def count(postId: Long): Future[Long] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      SELECT
      COUNT(*) like_count
      FROM likes
      WHERE post_id = ${postId};""".as(long("like_count").single)
    }
  }
}
