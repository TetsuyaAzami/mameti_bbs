package models.repositories

import models.domains.Post
import models.domains.User
import models.domains.PostForInsert
import models.DatabaseExecutionContext

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject.Inject
import java.time.LocalDateTime

import scala.concurrent.Future
import models.domains.UserWhoPosted

class PostRepository @Inject() (dbApi: DBApi, userRepository: UserRepository)(
    implicit dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val withUser = {
    get[Option[Long]]("posts.post_id") ~
      get[String]("posts.content") ~
      userRepository.userWhoPostedParser ~
      get[LocalDateTime]("posts.posted_at") map {
        case postId ~ content ~ user ~ postedAt =>
          Post(postId, content, user, postedAt)
      }
  }

  def findAll(): Future[List[Post]] = Future {
    db.withConnection { implicit con =>
      SQL"""
            SELECT
              p.post_id,
              p.content,
              p.user_id,
              p.posted_at,
              user_id,
              u.name,
              u.profile_img
            FROM posts p
            INNER JOIN users u
            USING (user_id)
            ORDER BY p.posted_at DESC;"""
        .as(
          withUser.*
        )
    }
  }

  def findByUserId(userId: Int): Future[List[Post]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      SELECT
      p.post_id,
      p.content,
      p.user_id,
      p.posted_at,
      user_id,
      u.name,
      u.profile_img
      FROM posts p
      INNER JOIN users u
      USING (user_id)
      WHERE user_id = ${userId}
      ORDER BY p.posted_at DESC;
      """.as(withUser.*)
    }
  }

  def insert(postForInsert: PostForInsert): Future[Option[Long]] = Future {
    db.withConnection { implicit connection =>
      SQL("""
      INSERT INTO posts (content, user_id, posted_at) VALUES ({content}, {userId}, {postedAt});
      """).bind(postForInsert).executeInsert()
    }

  }
}
