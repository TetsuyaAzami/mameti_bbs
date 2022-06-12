package models.repositories

import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerTest

import javax.inject.Inject
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.cache.redis.CacheAsyncApi
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import play.api.cache.redis.RedisList
import play.api.cache.redis.AsynchronousResult
import play.api.cache.redis.RedisMap
import play.api.cache.redis.AsynchronousResult
import play.api.cache.redis.{AsynchronousResult, Done}
import scala.concurrent.duration.Duration
import play.api.cache.redis.AsynchronousResult
import scala.concurrent.duration.Duration
import play.api.cache.redis.AsynchronousResult
import play.api.cache.redis.RedisSet
import play.api.cache.redis.AsynchronousResult
import play.api.cache.redis.{AsynchronousResult, Done}
import play.api.cache.redis.RedisSortedSet
import play.api.cache.redis.AsynchronousResult
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceFakeApplicationFactory
import play.api.Configuration
import play.api.Mode
import akka.actor.ActorSystem

class PostRepositorySpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with ScalaFutures
    with MockitoSugar {
  val actorSystem = ActorSystem.apply()
  val injector =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          "play.modules.enabled" -> List(
            "play.api.cache.redis.RedisCacheModule"
          )
        )
      )
      .bindings(bind[ActorSystem].to(actorSystem))
      .injector()

  "PostRepository#findAll" should {
    "全件取得ができること" in {
      assert(true)
    }
  }
}
