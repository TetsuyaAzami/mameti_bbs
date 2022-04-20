--!Ups
INSERT INTO departments (name)VALUES('frontend'),('backend'),('cloud'),('devOps');

INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user1','email1@exapmle.com', 'password1', '2000-11-11','user1です', 'img1',1);
INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user2','email2@exapmle.com', 'password2', '2000-11-12','user2です', 'img2',2);
INSERT INTO users (name, email, password, birthday, introduce, profile_img, department_id) VALUES('user3','email3@exapmle.com', 'password3', '2000-11-13','user3です', 'img3',3);

INSERT INTO posts (content,user_id,posted_at) VALUES ('user1投稿1', 1, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user1投稿2', 1, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user2投稿1', 2, NOW());
INSERT INTO posts (content,user_id,posted_at) VALUES ('user2投稿2', 2, NOW());

INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (1,1,'user1のpost1に対するコメント1',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (1,1,'user1のpost1に対するコメント2',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (1,1,'user1のpost1に対するコメント3',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (1,1,'user1のpost1に対するコメント4',NOW());
INSERT INTO comments (user_id, post_id, content, commented_at) VALUES (1,1,'user1のpost1に対するコメント5',NOW());

INSERT INTO likes (user_id, post_id) VALUES(1,1);
INSERT INTO likes (user_id, post_id) VALUES(1,2);
INSERT INTO likes (user_id, post_id) VALUES(2,1);

--!Downs
DELETE FROM likes;
DELETE FROM comments;
DELETE FROM posts;
DELETE FROM users;
DELETE FROM departments;
