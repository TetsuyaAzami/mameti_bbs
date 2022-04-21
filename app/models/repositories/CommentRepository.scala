package models.repositories

import models.DatabaseExecutionContext
import models.domains.Comment

import play.api.db.DBApi
import anorm._

import javax.inject.Inject

class CommentRepository @Inject() (
    dbApi: DBApi
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val simple: RowParser[Comment] =
    Macro.namedParser[Comment](Macro.ColumnNaming.SnakeCase)
}
