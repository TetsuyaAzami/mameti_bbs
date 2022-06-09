package models.repositories

import javax.inject.Inject
import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.typesafe.config.ConfigFactory
import java.io.File
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Configuration

class PostRepositorySpec extends PlaySpec with ScalaFutures {
  "PostRepository#findAll" should {
    "全件取得ができること" in {
      assert(true)
    }
  }
}
