package common

import org.scalatest._
import org.scalatestplus.play.PlaySpec
import org.openqa.selenium._
import org.openqa.selenium.WebElement;
import org.scalatestplus.selenium.Chrome

class SeleniumTestSpec extends PlaySpec with Chrome {
  val host = "http://localhost/"

  "一覧ページのリンク" should {
    "正常に一覧ページに遷移すること" in {
      go to (host + "posts")
      pageTitle mustBe ("投稿一覧")

      click on (linkText("豆知識一覧"))
      pageTitle mustBe ("投稿一覧")

      // コンテンツのフォームが存在すること
      assert(find(id("content")).isDefined)
      val contentForm = textArea("content")
      assert(contentForm.value == "")

      // 投稿ボタンがdisabledになっていること
      val buttons = findAll(tagName("button"))
      val postSubmitButton = buttons.filter(e => e.text == "投稿する").toList(0)
      assert(postSubmitButton.attribute("disabled") == Some("true"))
    }

    "正常にサインインページに遷移すること" in {
      go to (host + "posts")
      pageTitle mustBe ("投稿一覧")

      click on (linkText("ログイン"))
      pageTitle mustBe ("サインイン")

      // フォームの要素が存在すること
      assert(find(id("email")).isDefined)
      assert(find(id("password")).isDefined)
    }

    "正常にユーザ登録ページに遷移すること" in {
      go to (host + "posts")
      pageTitle mustBe ("投稿一覧")

      click on (linkText("会員登録"))
      pageTitle mustBe ("ユーザ登録")

      // フォームの要素が存在すること
      assert(find(id("name")).isDefined)
      assert(find(id("email")).isDefined)
      assert(find(id("password")).isDefined)
      assert(find(id("departmentId")).isDefined)

    }
  }

  "ログインしていない場合" should {
    "投稿しようとすると、ログインページにリダイレクトする" in {
      go to (host + "posts")
      pageTitle mustBe ("投稿一覧")
      val contents = findAll(name("content")).toList
      val postTextArea = contents(1)

      postTextArea.underlying.sendKeys("テスト投稿")
      submit()

      pageTitle mustBe ("サインイン")
      assert(
        xpath("/html/body/div/section/div/span[2]").element.text == "ログインしてください"
      )
    }
  }

  "ログインしている場合" should {
    "空の投稿が失敗し、投稿一覧ページに遷移すること" in {
      go to (host + "users/sign-in")
      pageTitle mustBe ("サインイン")
      textField("email").value = "email1@example.com"
      find(id("password")).get.underlying.sendKeys("password1")
      submit()

      pageTitle mustBe ("投稿一覧")
      val contents = findAll(name("content")).toList
      val postTextArea = contents(1)
      postTextArea.underlying.sendKeys("")
      submit()

      assert(
        xpath(
          "/html/body/div/section[2]/section[1]/form/div[1]/span"
        ).element.text == "1文字以上にしてください"
      )
    }

    "141文字の投稿が失敗し、投稿一覧ページに遷移すること" in {
      go to (host + "users/sign-in")
      pageTitle mustBe ("サインイン")
      textField("email").value = "email1@example.com"
      find(id("password")).get.underlying.sendKeys("password1")
      submit()

      pageTitle mustBe ("投稿一覧")
      val contents = findAll(name("content")).toList
      val postTextArea = contents(1)
      postTextArea.underlying.sendKeys("a" * 141)
      submit()

      assert(
        xpath(
          "/html/body/div/section[2]/section[1]/form/div[1]/span"
        ).element.text == "140文字以内にしてください"
      )
    }

    "140文字の投稿が成功し、投稿一覧ページに遷移すること" in {
      go to (host + "users/sign-in")
      pageTitle mustBe ("サインイン")
      textField("email").value = "email1@example.com"
      find(id("password")).get.underlying.sendKeys("password1")
      submit()

      pageTitle mustBe ("投稿一覧")
      val contents = findAll(name("content")).toList
      val postTextArea = contents(1)
      postTextArea.underlying.sendKeys("a" * 140)
      submit()

      assert(
        xpath("//*[@id='flashSuccessMessage']/span").element.text == "投稿完了しました"
      )
    }
  }

}
