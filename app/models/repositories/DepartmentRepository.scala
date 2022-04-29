package models.repositories

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import models.DatabaseExecutionContext
import models.domains.Department

import scala.concurrent.Future
import javax.inject.Inject

class DepartmentRepository @Inject() (dbApi: DBApi)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple = {
    get[Long]("d_department_id") ~
      get[String]("d_name") map { case departmentId ~ name =>
        Department(departmentId, name)
      }
  }

  def selectDepartmentList(): Future[List[(String, String)]] = Future {
    db.withConnection { implicit conn =>
      val departmentList =
        SQL"""
          SELECT
          department_id,
          name
          FROM departments;
          """.as((long("department_id") ~ str("name")).map {
          case departmentId ~ name => (departmentId, name)
        }.*)

      val castedDepartmentList = departmentList.map { e =>
        (e._1.toString(), e._2)
      }
      castedDepartmentList
    }
  }
}
