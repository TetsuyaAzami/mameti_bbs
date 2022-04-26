package common

import play.api.cache.SyncCacheApi
import models.domains.SignInUser

object CacheUtil {

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
      cache: SyncCacheApi,
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
      cache: SyncCacheApi,
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
      cache: SyncCacheApi,
      sessionId: Option[String]
  ): Option[SignInUser] = {
    sessionId match {
      case None            => None
      case Some(sessionId) => cache.get(sessionId)
    }
  }

  /** キャッシュからsessionIdに紐づくユーザ情報を削除
    *
    * @param cache
    *   キャッシュ
    * @param sessionId
    *   ログインsessionId
    */
  def deleteSessionUser(cache: SyncCacheApi, sessionId: String) = {
    cache.remove(sessionId)
  }

}
