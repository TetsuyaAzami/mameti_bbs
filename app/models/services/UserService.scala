package models.services

import models.domains.{User, SignInUser, UpdateUserProfileFormData}
import models.repositories.UserRepository
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit
    ec: ExecutionContext
) {

  def findUserById(userId: Long): Future[Option[UpdateUserProfileFormData]] =
    userRepository.findUserById(userId)

  def findSignInUserById(
      userId: Long
  ): Future[Option[SignInUser]] = userRepository.findSignInUserById(userId)

  def findUserByEmail(email: String): Future[Option[Long]] =
    userRepository.findUserByEmail(email)

  def findUserByEmailAndPassword(
      email: String,
      password: String
  ): Future[Option[SignInUser]] =
    userRepository.findUserByEmailAndPassword(email, password)

  def insert(user: User): Future[Option[Long]] = userRepository.insert(user)

  def update(user: UpdateUserProfileFormData): Future[Long] = {
    user.profileImg match {
      case None => {
        userRepository.updateExceptProfileImg(user)
      }
      case Some(profileImg) => {
        userRepository.update(user)
      }
    }
  }
}
