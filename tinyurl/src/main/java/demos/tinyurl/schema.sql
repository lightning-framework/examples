DROP TABLE IF EXISTS tinyurls;

CREATE TABLE tinyurls (
  code varchar(255) not null,
  url mediumtext not null,
  user_id int(64) unsigned not null,
  last_updated int(64) not null,
  last_clicked int(64) not null,
  click_count int(64) not null default 0,
  PRIMARY KEY(code),
  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
) CHARACTER SET utf8 COLLATE utf8_general_ci;
