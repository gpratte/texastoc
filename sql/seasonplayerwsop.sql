ALTER TABLE seasonplayer ADD wsop int not null default 0;
update seasonplayer set wsop=0;

ALTER TABLE seasonhistoryentry ADD wsop int not null default 0;
update seasonhistoryentry set wsop=0;

// 2014 Gil
update seasonhistoryentry set wsop=1 where seasonId=4 and playerId=28;
// 2014 Freddy
update seasonhistoryentry set wsop=1 where seasonId=4 and playerId=12;
// 2014 G
update seasonhistoryentry set wsop=1 where seasonId=4 and playerId=2;
