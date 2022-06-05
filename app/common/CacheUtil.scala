package common

import play.api.cache.redis.CacheApi
import models.domains.SignInUser
import javax.inject.Inject
import software.amazon.awssdk.core.internal.http.pipeline.stages.SigningStage

class CacheUtil @Inject() (cache: CacheApi) {

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
      case None            => None
      case Some(sessionId) => cache.get[SignInUser](sessionId)
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
