package models.services

import models.repositories.DepartmentRepository
import javax.inject._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

@Singleton
class DepartmentService @Inject() (departmentRepository: DepartmentRepository)(
    implicit ec: ExecutionContext
) {
  // フォーム表示の都合上(Long, String)を(String, Stringに変換)
  def selectDepartmentList(): Future[List[(String, String)]] =
    departmentRepository
      .selectDepartmentList()
      .map(departmentList => departmentList.map(e => (e._1.toString(), e._2)))
}
