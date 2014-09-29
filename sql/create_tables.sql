
create table pointsystem 
(
numPlayers int not null, 
tenthPlaceIncr decimal(2,1) not null, 
tenthPlacePoints int not null, 
multiplier float not null,
primary key (numPlayers)
);

create table player 
(
id int  not null auto_increment,
firstName varchar(32),
lastName varchar(32),
phone varchar(32),
email varchar(64),
cellCarrier varchar(64),
address varchar(128),
active boolean not null default true,
note varchar(1024),
primary key(id)
); 

create table season 
(
id int not null auto_increment, 
startDate date not null, 
endDate date not null,
useHistoricalData boolean not null, 
finalized boolean default false,
note varchar(1024),
totalBuyIn int,
totalReBuy int,
totalPot int,
totalAnnualToc int,
lastCalculated date default null,
primary key(id)
);

create table seasonhistoryentry
(
seasonId int not null,
points int not null,
playerId int not null, 
entries int not null
);
alter table seasonhistoryentry add foreign key (seasonId) references season(id);
alter table seasonhistoryentry add foreign key (playerId) references player(id);
alter table seasonhistoryentry add constraint sh_sp unique (seasonId, playerId);

create table quarterlyseason 
(
id int not null auto_increment, 
seasonId int not null, 
quarter int not null, 
startDate date not null, 
endDate date not null,
finalized boolean default false,
note varchar(1024),
totalQuarterlyToc int default 0,
lastCalculated date default null,
primary key(id)
);
alter table quarterlyseason add foreign key (seasonId) references season(id);
alter table quarterlyseason add constraint qs_sq unique (seasonId, quarter);


create table game 
(
id int not null auto_increment,
seasonId int not null, 
gameDate date not null,
hostId int,
note varchar(1024),
numPlayers int default 0,
totalBuyIn int default 0,
totalReBuy int default 0,
totalAnnualToc int default 0,
totalQuarterlyToc int default 0,
adjustPot int default 0,
finalized boolean default false,
doubleBuyIn boolean default false,
lastCalculated date default null,
primary key(id)
);
alter table game add foreign key (seasonId) references season(id);
alter table game add constraint game_date unique (gameDate);

create table gamepayout
(
gameId int not null,
place int not null,
amount int not null
);
alter table gamepayout add foreign key (gameId) references game(id);
alter table gamepayout add constraint payout_place unique (gameId, place);

create table gameplayer 
(
id int not null auto_increment,
playerId int not null,
gameId int not null,
buyIn int,
annualTocPlayer boolean default false,
quarterlyTocPlayer boolean default false,
reBuyIn int,
finish int,
chop int,
points int,
lastCalculated date default null,
note varchar(1024),
primary key(id)
);  
alter table gameplayer add foreign key (playerId) references player(id);
alter table gameplayer add foreign key (gameId) references game(id);
alter table gameplayer add constraint game_player unique (playerId, gameId);

create table seasonplayer 
(
id int not null auto_increment,
playerId int not null,
seasonId int not null,
place int,
points int,
numEntries int,
primary key(id)
);  
alter table seasonplayer add foreign key (playerId) references player(id);
alter table seasonplayer add foreign key (seasonId) references season(id);
alter table seasonplayer add constraint season_player unique (playerId, seasonId);

create table qseasonplayer 
(
id int not null auto_increment,
playerId int not null,
qSeasonId int not null,
place int,
points int,
numEntries int,
primary key(id)
);  
alter table qseasonplayer add foreign key (playerId) references player(id);
alter table qseasonplayer add foreign key (qSeasonId) references quarterlyseason(id);
alter table qseasonplayer add constraint qseason_player unique (playerId, qSeasonId);

create table gameaudit
(
id int not null auto_increment,
seasonId int, 
gameDate date,
hostId int,
gameNote varchar(1024),
numPlayers int,
totalBuyIn int,
totalReBuy int,
totalAnnualToc int,
totalQuarterlyToc int,
adjustPot int,
finalized boolean,
doubleBuyIn boolean,
lastCalculated date,
playerId int,
gameId int,
buyIn int,
annualTocPlayer boolean,
quarterlyTocPlayer boolean,
reBuyIn int,
finish int,
chop int,
points int,
gamePlayerNote varchar(1024),
primary key(id)
);
