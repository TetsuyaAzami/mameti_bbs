--!Ups
CREATE TABLE departments(
department_id serial PRIMARY KEY,
name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users(
user_id serial PRIMARY KEY,
name varchar(128) NOT NULL,
email varchar(254) NOT NULL UNIQUE ,
password varchar(128) CHECK (LENGTH(password) >= 8),
birthday date CHECK(birthday < CURRENT_DATE),
introduce varchar(200),
profile_img text,
department_id integer NOT NULL REFERENCES departments(department_id)
);

CREATE TABLE posts(
post_id serial PRIMARY KEY,
content varchar(140) NOT NULL,
user_id integer NOT NULL REFERENCES users(user_id),
posted_at timestamp NOT NULL
);

CREATE TABLE comments(
comment_id serial PRIMARY KEY,
user_id integer REFERENCES users(user_id),
post_id integer REFERENCES posts(post_id)
);

CREATE TABLE likes(
like_id serial PRIMARY KEY,
user_id integer REFERENCES users(user_id),
post_id integer REFERENCES posts(post_id),
	UNIQUE (user_id, post_id)
);

--!Downs
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS likes;
