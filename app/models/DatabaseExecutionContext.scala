package models

import javax.inject._
import play.api.libs.concurrent.CustomExecutionContext
import akka.actor.ActorSystem

@Singleton
class DatabaseExecutionContext @Inject() (system: ActorSystem)
    extends CustomExecutionContext(system, "database.dispatcher")
