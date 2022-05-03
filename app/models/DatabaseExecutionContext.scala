package models

import play.api.libs.concurrent.CustomExecutionContext
import akka.actor.ActorSystem
import javax.inject._

@Singleton
class DatabaseExecutionContext @Inject() (system: ActorSystem)
    extends CustomExecutionContext(system, "database.dispatcher")
