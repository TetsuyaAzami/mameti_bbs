package models.repositories

import javax.inject._
import play.api.db.DBApi
import models.DatabaseExecutionContext
import anorm._

class LikeRepository @Inject() (
    dbApi: DBApi
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")
}
