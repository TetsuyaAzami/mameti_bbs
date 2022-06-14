--!Ups
INSERT INTO departments (name)VALUES('フロントエンド'),('バックエンド'),('クラウド');

INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user1','email1@example.com', 'password1', '2000-11-11','user1です', 'email1@example.com.png',1);
INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user2','email2@example.com', 'password2', '2000-11-12','user2です', 'email2@example.com.jpg',2);
INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user3','email3@example.com', 'password3', '2000-11-13','user3です', null,3);
INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user4','email4@example.com', 'password4', null, null, null, 3);

INSERT INTO posts (content,user_id,posted_at) VALUES ('user1投稿1', 1, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user1投稿2', 1, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user2投稿1', 2, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user2投稿2', 2, NOW());

INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (2,1,'post1に対するユーザ2のコメント1',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (2,1,'post1に対するユーザ2のコメント2',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (2,1,'post1に対するユーザ2のコメント3',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (3,1,'post1に対するユーザ3のコメント4',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (3,1,'post1に対するユーザ3のコメント5',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (3,2,'post2に対するユーザ3のコメント1',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (3,2,'post2に対するユーザ3のコメント2',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (3,2,'post2に対するユーザ3のコメント3',NOW());

INSERT INTO likes (user_id, post_id) VALUES(2,1);
INSERT INTO likes (user_id, post_id) VALUES(3,1);
INSERT INTO likes (user_id, post_id) VALUES(4,1);
INSERT INTO likes (user_id, post_id) VALUES(3,2);
INSERT INTO likes (user_id, post_id) VALUES(4,2);
INSERT INTO likes (user_id, post_id) VALUES(1,3);
INSERT INTO likes (user_id, post_id) VALUES(3,3);

--!Downs
DELETE FROM likes;
DELETE FROM comments;
DELETE FROM posts;
DELETE FROM users;
DELETE FROM departments;
