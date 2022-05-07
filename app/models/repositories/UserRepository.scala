package models.repositories

import models.DatabaseExecutionContext
import models.domains._

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import javax.inject._
import java.time.LocalDate
import scala.concurrent.Future

@Singleton
class UserRepository @Inject() (
    dbApi: DBApi,
    departmentRepository: DepartmentRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val userForUpdateProfileParser = {
    get[Long]("u_user_id") ~
      get[String]("u_name") ~
      get[String]("u_email") ~
      get[Option[LocalDate]]("u_birthday") ~
      get[Option[String]]("u_introduce") ~
      get[Option[String]]("u_profile_img") ~
      get[Long]("u_department_id") map {
        case userId ~ name ~ email ~ birthday ~ introduce ~ profileImg ~ departmentId =>
          UpdateUserProfileFormData(
            userId,
            name,
            email,
            birthday,
            introduce,
            profileImg,
            departmentId
          )
      }
  }

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
    get[Long]("cu_user_id") ~
      get[String]("cu_name") ~
      get[Option[String]]("cu_profile_img") map {
        case userId ~ name ~ profileImg =>
          UserWhoCommented(userId, name, profileImg)
      }
  }

  // サインイン時に使用
  private[repositories] val signInUserParser = {
    get[Long]("u_user_id") ~
      get[String]("u_name") ~
      get[String]("u_email") ~
      get[String]("u_password") ~
      get[Option[LocalDate]]("u_birthday") ~
      get[Option[String]]("u_introduce") ~
      get[Option[String]]("u_profile_img") ~
      get[Long]("u_department_id") ~
      departmentRepository.simple map {
        case userId ~ name ~ email ~ password ~ birthdayOpt ~ introduceOpt ~ profileImgOpt ~ departmentId ~ department =>
          SignInUser(
            userId,
            name,
            email,
            password,
            birthdayOpt,
            introduceOpt,
            profileImgOpt,
            departmentId,
            department
          )
      }
  }

  def findUserById(userId: Long): Future[Option[UpdateUserProfileFormData]] =
    Future {
      db.withConnection { implicit con =>
        SQL"""
        SELECT
        u.user_id u_user_id,
        u.name u_name,
        u.email u_email,
        u.birthday u_birthday,
        u.introduce u_introduce,
        u.profile_img u_profile_img,
        u.department_id u_department_id
        FROM users u
        WHERE u.user_id = ${userId}
        ;"""
          .as(userForUpdateProfileParser.singleOpt)
      }
    }

  def findSignInUserById(
      userId: Long
  ): Future[Option[SignInUser]] =
    Future {
      db.withConnection { implicit con =>
        SQL"""
        SELECT
        u.user_id u_user_id,
        u.name u_name,
        u.email u_email,
        u.password u_password,
        u.birthday u_birthday,
        u.introduce u_introduce,
        u.profile_img u_profile_img,
        u.department_id u_department_id,
        d.department_id d_department_id,
        d.name d_name
        FROM users u
        INNER JOIN departments d
        ON u.department_id = d.department_id
        WHERE u.user_id = ${userId}
        ;"""
          .as(signInUserParser.singleOpt)
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
        u.email u_email,
        u.password u_password,
        u.birthday u_birthday,
        u.introduce u_introduce,
        u.profile_img u_profile_img,
        u.department_id u_department_id,
        d.department_id d_department_id,
        d.name d_name
        FROM users u
        INNER JOIN departments d
        ON u.department_id = d.department_id
        WHERE email = ${email} AND password = ${password}
        ;
        """.as(signInUserParser.singleOpt)
    }
  }

  def insert(user: User): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
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
    }
  }

  def update(user: UpdateUserProfileFormData): Future[Long] = Future {
    db.withConnection { implicit conn =>
      SQL("""
          UPDATE users SET
          name = {name},
          email = {email},
          birthday = {birthday},
          introduce = {introduce},
          profile_img = {profileImg},
          department_id = {departmentId}
          WHERE user_id = {userId};
      """)
        .on(
          "name" -> user.name,
          "email" -> user.email,
          "birthday" -> user.birthday,
          "introduce" -> user.introduce,
          "profileImg" -> user.profileImg,
          "departmentId" -> user.departementId,
          "userId" -> user.userId
        )
        .executeUpdate()
    }
  }
  def updateExceptProfileImg(user: UpdateUserProfileFormData): Future[Long] =
    Future {
      db.withConnection { implicit conn =>
        SQL("""
          UPDATE users SET
          name = {name},
          email = {email},
          birthday = {birthday},
          introduce = {introduce},
          department_id = {departmentId}
          WHERE user_id = {userId};
      """)
          .on(
            "name" -> user.name,
            "email" -> user.email,
            "birthday" -> user.birthday,
            "introduce" -> user.introduce,
            "departmentId" -> user.departementId,
            "userId" -> user.userId
          )
          .executeUpdate()
      }
    }
}
