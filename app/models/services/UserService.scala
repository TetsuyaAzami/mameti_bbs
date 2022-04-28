package models.services

import models.domains.User
import models.domains.SignInUser
import models.repositories.UserRepository
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Inject

class UserService @Inject() (userRepository: UserRepository)(implicit
    ec: ExecutionContext
) {

  def findUserById(id: Long): Future[Option[User]] =
    userRepository.findUserById(id)

  def findUserByEmail(email: String): Future[Option[Long]] =
    userRepository.findUserByEmail(email)

  def findUserByEmailAndPassword(
      email: String,
      password: String
  ): Future[Option[SignInUser]] =
    userRepository.findUserByEmailAndPassword(email, password)

  // フォーム表示の都合上(Long, String)を(String, Stringに変換)
  def selectDepartmentList(): Future[List[(String, String)]] =
    userRepository
      .selectDepartmentList()
      .map(departmentList => departmentList.map(e => (e._1.toString(), e._2)))

  def insert(user: User): Future[Option[Long]] = userRepository.insert(user)
}
