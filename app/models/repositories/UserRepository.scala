package models.repositories

import models.domains.User
import models.domains.UserWhoPosted
import models.DatabaseExecutionContext

import java.time.LocalDate
import javax.inject.Inject

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import scala.concurrent.Future

class UserRepository @Inject() (dbApi: DBApi)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple: RowParser[User] =
    Macro.namedParser[User](Macro.ColumnNaming.SnakeCase)

  // 投稿一覧取得の際に使用する
  private[repositories] val userWhoPostedParser: RowParser[UserWhoPosted] =
    Macro.namedParser[UserWhoPosted](Macro.ColumnNaming.SnakeCase)

  def findUserById(id: Long): Future[Option[User]] = Future {
    db.withConnection { implicit con =>
      SQL"""
      SELECT
      user_id,
      name,
      email,
      password,
      birthday,
      introduce,
      profile_img,
      department_id
      FROM users
      WHERE user_id = $id;"""
        .as(simple.singleOpt)
    }
  }
}
