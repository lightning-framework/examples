DROP TABLE IF EXISTS files;

CREATE TABLE files (
  id int(64) unsigned not null auto_increment,
  name varchar(255) not null,
  content LONGBLOB not null,
  uploaded int(64) unsigned not null,
  downloads int(64) unsigned not null,
  last_downloaded int(64) unsigned not null,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci;

