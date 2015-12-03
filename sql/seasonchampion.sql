create table seasonchampion 
(
seasonId int not null,
playerId int not null,
points int not null,
primary key(seasonId, playerId)
); 

alter table seasonchampion add foreign key (seasonId) references season(id);
alter table seasonchampion add foreign key (playerId) references player(id);

insert into seasonchampion (seasonId, playerId, points) values (8, 4, 1673);
insert into seasonchampion (seasonId, playerId, points) values (7, 1, 1643);
insert into seasonchampion (seasonId, playerId, points) values (6, 12, 1715);
insert into seasonchampion (seasonId, playerId, points) values (1, 7, 1704);
insert into seasonchampion (seasonId, playerId, points) values (2, 10, 1604);
insert into seasonchampion (seasonId, playerId, points) values (3, 9, 1756);
insert into seasonchampion (seasonId, playerId, points) values (4, 28, 1584);

insert into seasonchampion (seasonId, playerId, points) values (5, 28, 1842);
