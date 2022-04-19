package models.domains

import java.time.LocalDate

final case class User(
    userId: Option[Long] = None,
    name: String,
    email: String,
    password: String,
    birthday: Option[LocalDate],
    introduce: Option[String],
    profileImg: Option[String],
    departmentId: Int
)

// 投稿したユーザ
final case class UserWhoPosted(
    name: String,
    profileImg: Option[String]
)
