--!Ups
CREATE TABLE departments (
	department_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(30) NOT NULL UNIQUE
);
CREATE INDEX department_name_idx ON departments(name);

CREATE TABLE users (
	user_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(128) NOT NULL,
	email varchar(254) NOT NULL UNIQUE,
	password varchar(128) CONSTRAINT `password_check` CHECK ((CHAR_LENGTH(`password`) >= 8)),
	birthday date,
	introduce varchar(200),
	profile_img text,
	department_id int NOT NULL REFERENCES departments (department_id)
);


CREATE TABLE posts (
	post_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
	content varchar(140) NOT NULL,
	user_id int NOT NULL REFERENCES users (user_id),
	posted_at timestamp NOT NULL
);

CREATE TABLE comments (
	comment_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
	user_id int REFERENCES users (user_id),
	post_id int REFERENCES posts (post_id) ON DELETE CASCADE,
	content varchar(140) NOT NULL,
	commented_at timestamp NOT NULL
);

CREATE TABLE likes (
	like_id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
	user_id int REFERENCES users (user_id),
	post_id int REFERENCES posts (post_id) ON DELETE CASCADE,
	UNIQUE (user_id, post_id)
);

--!Downs
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS likes;
