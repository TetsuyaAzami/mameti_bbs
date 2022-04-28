package models.repositories

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import models.DatabaseExecutionContext

import scala.concurrent.Future
import javax.inject.Inject

class DepartmentRepository @Inject() (dbApi: DBApi)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

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
