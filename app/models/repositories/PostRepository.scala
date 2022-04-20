package models.repositories

import models.domains.Post
import models.DatabaseExecutionContext

import javax.inject.Inject
import java.time.LocalDateTime

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import scala.concurrent.Future
import models.domains.User

class PostRepository @Inject() (dbApi: DBApi, userRepository: UserRepository)(
    implicit dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple = {
    get[Option[Long]]("posts.post_id") ~
      get[String]("posts.content") ~
      userRepository.postUserParser ~
      get[Option[LocalDateTime]]("posts.posted_at") map {
        case postId ~ content ~ user ~ postedAt =>
          Post(postId, content, user, postedAt)
      }
  }

  def selectAllPosts(): Future[List[Post]] = Future {
    db.withConnection { implicit con =>
      val allPosts =
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
            simple.*
          )
      allPosts
    }
  }
}
