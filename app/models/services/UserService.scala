package models.services

import models.domains.{User, SignInUser, UpdateUserProfileFormData}
import models.repositories.UserRepository
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Inject

class UserService @Inject() (userRepository: UserRepository)(implicit
    ec: ExecutionContext
) {

  def findUserById(userId: Long): Future[Option[UpdateUserProfileFormData]] =
    userRepository.findUserById(userId)

  def findUserByEmail(email: String): Future[Option[Long]] =
    userRepository.findUserByEmail(email)

  def findUserByEmailAndPassword(
      email: String,
      password: String
  ): Future[Option[SignInUser]] =
    userRepository.findUserByEmailAndPassword(email, password)

  def insert(user: User): Future[Option[Long]] = userRepository.insert(user)

  def update(user: UpdateUserProfileFormData): Future[Long] =
    userRepository.update(user)
}
