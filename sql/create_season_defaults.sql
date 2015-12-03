
CREATE TABLE seasondefaults (
  homeGameId int NOT NULL,
  note varchar(1024) DEFAULT NULL,
  potGameDebit int DEFAULT '0',
  playerGameDebit int DEFAULT '0',
  PRIMARY KEY (`id`)
);
alter table seasondefaults add foreign key (homeGameId) references homegame(id);

