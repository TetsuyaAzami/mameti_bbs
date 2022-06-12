import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class ConfigurationSpec extends PlaySpec with GuiceOneAppPerTest {
  "configuration" should {
    "データベース設定を上書きできていること" in {
      app.configuration.getOptional[String]("db.default.url") mustBe Some(
        "jdbc:mysql://localhost/mameti_bbs_test"
      )
      app.configuration.getOptional[String]("db.default.username") mustBe Some(
        "mameti_bbs_test"
      )
      app.configuration.getOptional[String]("db.default.password") mustBe Some(
        "mameti_bbs_test"
      )
    }
  }
}
