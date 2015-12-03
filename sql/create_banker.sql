create table gamebanker 
(
gameId int not null, 
playerId int not null, 
primary key(gameId,playerId)
);
alter table gamebanker add foreign key (gameId) references game(id);
alter table gamebanker add foreign key (playerId) references player(id);
