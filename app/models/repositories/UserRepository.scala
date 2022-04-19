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

  // private[repositories] val simple = {
  //   get[Option[Long]]("users.user_id") ~
  //     get[String]("users.name") ~
  //     get[String]("users.email") ~
  //     get[String]("users.password") ~
  //     get[Option[LocalDate]]("users.birthday") ~
  //     get[Option[String]]("users.introduce") ~
  //     get[Option[String]]("users.profile_img") ~
  //     get[Int]("users.department_id") map {
  //       case userId ~ name ~ email ~ password ~ birthday ~ introduce ~ profileImg ~ departmentId =>
  //         User(
  //           userId,
  //           name,
  //           email,
  //           password,
  //           birthday,
  //           introduce,
  //           profileImg,
  //           departmentId
  //         )
  //     }
  // }

  private[repositories] val simple: RowParser[User] =
    Macro.namedParser[User](Macro.ColumnNaming.SnakeCase)

  // 投稿一覧取得の際に使用する
  private[repositories] val postUserParser: RowParser[UserWhoPosted] =
    Macro.namedParser[UserWhoPosted](Macro.ColumnNaming.SnakeCase)

  def findUserById(id: Long): Future[Option[User]] = Future {
    db.withConnection { implicit con =>
      SQL"SELECT name, email, password, birthday, introduce, profile_img, department_id FROM users WHERE user_id = $id;"
        .as(simple.singleOpt)
    }
  }
}
