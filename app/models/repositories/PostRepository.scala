package models.repositories

import models.domains._
import models.DatabaseExecutionContext

import anorm._
import anorm.SqlParser._
import play.api.db.DBApi

import scala.concurrent.Future
import scala.language.postfixOps
import javax.inject._
import java.time.LocalDate
import java.time.LocalDateTime

@Singleton
class PostRepository @Inject() (
    dbApi: DBApi,
    userRepository: UserRepository,
    commentRepository: CommentRepository,
    likeRepository: LikeRepository
)(implicit
    dec: DatabaseExecutionContext
) {
  private val db = dbApi.database("default")

  private[repositories] val forUpdate = {
    get[Long]("p_post_id") ~
      get[String]("p_content") map { case postId ~ content =>
        PostUpdateFormData(postId, content)
      }
  }

  private[repositories] val withUser = {
    get[Option[Long]]("p_post_id") ~
      get[String]("p_content") ~
      get[Long]("p_user_id") ~
      userRepository.userWhoPostedParser.? ~
      get[LocalDateTime]("p_posted_at") map {
        case postId ~ content ~ userId ~ user ~ postedAt =>
          Post(postId, content, userId, user, postedAt, List())
      }
  }

  def findAll(): Future[List[(Post, Option[Long], List[Like])]] = Future {
    db.withConnection { implicit con =>
      val sqlResult = SQL"""
        SELECT
        p.post_id p_post_id,
        p.content p_content,
        p.user_id p_user_id,
        p.posted_at p_posted_at,
        c.count c_count, -- コメント数の取得
        u.user_id u_user_id, -- 投稿したユーザの取得
        u.name u_name,
        u.profile_img u_profile_img,
        l.like_id l_like_id,
        l.user_id l_user_id,
        l.post_id l_post_id
        FROM posts p
        LEFT OUTER JOIN (
        SELECT -- コメント数の取得
        post_id,
        count(*) count
        FROM comments
        GROUP BY post_id
        ) c
        ON p.post_id = c.post_id
        INNER JOIN users u
        ON p.user_id = u.user_id
        LEFT OUTER JOIN likes l
        ON p.post_id = l.post_id
        ORDER BY p_posted_at DESC;
        """
        .as(
          (withUser ~ long("c_count").? ~ likeRepository.simple.?).map {
            case post ~ cCount ~ like =>
              (post, cCount, like)
          }.*
        )
      // postIdごとにsqlの取得結果をグループ化
      val groupedPosts = sqlResult.groupBy(_._1)
      val result = groupedPosts.map { e =>
        val postList = e._2
        val post = postList(0)._1
        val commentCount = postList(0)._2
        val likeList = postList.flatMap(_._3)
        (post, commentCount, likeList)
      }
      result.toList
    }
  }

  def findByUserId(
      userId: Long
  ): Future[List[(Post, Option[Long], Option[Long])]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
        SELECT
        p.post_id p_post_id,
        p.content p_content,
        p.user_id p_user_id,
        p.posted_at p_posted_at,
        c.count c_count, -- コメント数の取得
        l.count l_count, -- いいね数の取得
        u.user_id u_user_id, -- 投稿したユーザの取得
        u.name u_name,
        u.profile_img u_profile_img
        FROM posts p
        LEFT OUTER JOIN (
        SELECT
        post_id,
        count(*) count
        FROM comments
        GROUP BY post_id
        ) c
        ON p.post_id = c.post_id
        LEFT OUTER JOIN(
        SELECT
        post_id,
        COUNT(*) count
        FROM likes
        GROUP BY post_id
        ) l
        ON p.post_id = l.post_id
        INNER JOIN users u
        ON p.user_id = u.user_id
        WHERE p.user_id = ${userId}
        ORDER BY p_posted_at DESC;
        """
        .as((withUser ~ long("c_count").? ~ long("l_count").?).map {
          case post ~ cCount ~ lCount =>
            (post, cCount, lCount)
        }.*)
    }
  }

  def findByPostIdAndUserId(
      postId: Long,
      userId: Long
  ): Future[Option[PostUpdateFormData]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      SELECT
      p.post_id p_post_id,
      p.content p_content
      FROM posts p
      WHERE p.post_id = ${postId} AND p.user_id = ${userId};""".as(
        forUpdate.singleOpt
      )
    }
  }

  def findByPostIdWithCommentList(postId: Long): Future[(Option[Post], Long)] =
    Future {
      db.withConnection { implicit conn =>
        val sqlResult =
          SQL"""
          SELECT
          p.post_id p_post_id,
          p.content p_content,
          p.user_id p_user_id,
          p.posted_at p_posted_at,
          u.user_id u_user_id,
          u.name u_name,
          u.profile_img u_profile_img,
          c.comment_id c_comment_id,
          c.user_id c_user_id,
          c.post_id c_post_id,
          c.content c_content,
          c.commented_at c_commented_at,
          cu.user_id cu_user_id,
          cu.name cu_name,
          cu.profile_img cu_profile_img,
          COALESCE(l.count,0) l_count --いいね数の取得
          FROM posts p
          LEFT OUTER JOIN users u
          ON p.user_id = u.user_id
          LEFT OUTER JOIN comments c
          ON p.post_id = c.post_id
          LEFT OUTER JOIN users cu
          ON c.user_id = cu.user_id
          LEFT OUTER JOIN(
          SELECT
          post_id,
          COUNT(*) count
          FROM likes
          GROUP BY post_id
          ) l
          ON p.post_id = l.post_id
          WHERE p.post_id = ${postId}
          ORDER BY c_commented_at DESC; """
            .as(
              (withUser ~ commentRepository.commentWithUserParser.? ~ long(
                "l_count"
              )).*
            )

        sqlResult match {
          case Nil => {
            (None, 0)
          }
          case head :: next => {
            val post = sqlResult(0)._1._1
            val likeCount = sqlResult(0)._2
            val isCommentListNil = sqlResult(0)._1._2
            // コメントが1つもなければ空のリストを返す
            val commentList = isCommentListNil match {
              case None => Nil
              case Some(Comment(_, _, _, _, _, _)) => {
                sqlResult.map(_._1._2.get)
              }
            }
            val postWithCommentList = post.copy(commentList = commentList)
            (Some(postWithCommentList), likeCount)
          }
        }
      }
    }

  def update(post: Post, userId: Long): Future[Long] = Future {
    db.withConnection { implicit conn =>
      SQL(
        """UPDATE posts SET
           content = {content},
           posted_at = {postedAt}
           WHERE post_id = {postId} AND user_id = {userId};"""
      )
        .on(
          "content" -> post.content,
          "postedAt" -> post.postedAt,
          "postId" -> post.postId,
          "userId" -> userId
        )
        .executeUpdate()
    }
  }

  def insert(post: Post): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
          INSERT INTO posts
                     (content, user_id, posted_at)
          VALUES     ({content}, {userId}, {postedAt});
      """)
        .on(
          "content" -> post.content,
          "userId" -> post.userId,
          "postedAt" -> post.postedAt
        )
        .executeInsert()
    }
  }

  def delete(postId: Long, userId: Long): Future[Long] = Future {
    db.withConnection { implicit conn =>
      SQL"""
      DELETE FROM posts
      WHERE post_id = ${postId} AND user_id = ${userId};""".executeUpdate()

    }
  }
}
