package common

import play.api.cache.redis.CacheAsyncApi
import models.domains.SignInUser
import software.amazon.awssdk.core.internal.http.pipeline.stages.SigningStage

import javax.inject.Inject
import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import scala.concurrent.Await

class CacheUtil @Inject() (cache: CacheAsyncApi)(implicit
    ec: ExecutionContext
) {

  /** キャッシュにユーザをセット
    *
    * @param cache
    *   キャッシュ
    * @param sessionId
    *   sessionId
    * @param signInUser
    *   ログインユーザ情報
    */
  def setSessionUser(
      sessionId: String,
      signInUser: SignInUser
  ) = {
    cache.set(sessionId, signInUser)
  }

  /** キャッシュにユーザをセット (sessionIdがOption型の時)
    *
    * @param cache
    *   キャッシュ
    * @param sessionId
    *   sessionId
    * @param signInUser
    *   ログインユーザ情報
    */
  def setSessionUser(
      sessionId: Option[String],
      signInUser: SignInUser
  ) = {
    sessionId match {
      case None            =>
      case Some(sessionId) => cache.set(sessionId, signInUser)
    }
  }

  /** cacheに格納されているユーザを取得
    *
    * @param cache
    *   キャッシュ
    * @param sessionId
    *   sessionId
    * @return
    *   成功時: ログインユーザ情報 失敗時: None
    */
  def getSessionUser(
      sessionId: Option[String]
  ): Option[SignInUser] = {
    sessionId match {
      case None => None
      case Some(sessionId) => {
        val signInUserResult = cache.get[SignInUser](sessionId)
        Await.result(signInUserResult, 5 seconds)
      }
    }
  }

  /** キャッシュからsessionIdに紐づくユーザ情報を削除
    *
    * @param cache
    *   キャッシュ
    * @param sessionId
    *   ログインsessionId
    */
  def deleteSessionUser(sessionId: String) = {
    cache.remove(sessionId)
  }

}
