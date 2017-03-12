CREATE TABLE nodetable (
  id INTEGER NOT NULL,
  type int NOT NULL,
  version BIGINT NOT NULL,
  time TIMESTAMP DEFAULT NULL,
  data VARCHAR(255) DEFAULT '' NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE linktable (
  id1 INTEGER NOT NULL,
  id2 INTEGER NOT NULL,
  link_type BIGINT NOT NULL,
  visibility tinyint DEFAULT NULL,
  data varchar(255) DEFAULT '' NOT NULL,
  time TIMESTAMP DEFAULT NULL,
  version int NOT NULL,
  PRIMARY KEY (id1,id2,link_type),
);