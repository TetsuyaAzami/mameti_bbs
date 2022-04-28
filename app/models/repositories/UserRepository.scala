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
import models.domains.UserWhoCommented
import models.domains.SignInUser

class UserRepository @Inject() (dbApi: DBApi)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple: RowParser[User] =
    Macro.namedParser[User](Macro.ColumnNaming.SnakeCase)

  // 投稿一覧取得の際に使用
  private[repositories] val userWhoPostedParser = {
    get[Long]("u_user_id") ~
      get[String]("u_name") ~
      get[Option[String]]("u_profile_img") map {
        case userId ~ name ~ profileImg =>
          UserWhoPosted(userId, name, profileImg)
      }
  }

  // コメントリストを表示する際に使用
  private[repositories] val userWhoCommentedParser = {
    get[Option[Long]]("cu_user_id") ~
      get[Option[String]]("cu_name") ~
      get[Option[String]]("cu_profile_img") map {
        case userId ~ name ~ profileImg =>
          UserWhoCommented(userId, name, profileImg)
      }
  }

  // サインイン時に使用
  private[repositories] val signInUserParser = {
    get[Long]("u_user_id") ~
      get[String]("u_name") ~
      get[Option[String]]("u_profile_img") map {
        case userId ~ name ~ profileImg =>
          SignInUser(userId, name, profileImg)
      }
  }

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

  def findUserByEmail(email: String): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      val userId =
        SQL"""
        SELECT
        user_id
        FROM users
        WHERE email = ${email};
        """.as(long("user_id").singleOpt)
      userId
    }
  }

  def findUserByEmailAndPassword(
      email: String,
      password: String
  ): Future[Option[SignInUser]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
        SELECT
        u.user_id u_user_id,
        u.name u_name,
        u.profile_img u_profile_img
        FROM users u
        WHERE email = ${email} AND password = ${password}
        ;
        """.as(signInUserParser.singleOpt)
    }
  }

  def insert(user: User): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      val userId = SQL("""
      INSERT INTO users
      (name, email, password, department_id)
      VALUES({name},{email},{password},{departmentId});""")
        .on(
          "name" -> user.name,
          "email" -> user.email,
          "password" -> user.password,
          "departmentId" -> user.departmentId
        )
        .executeInsert()
      userId
    }
  }
}
