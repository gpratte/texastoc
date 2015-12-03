ALTER TABLE seasonplayer ADD forfeit int not null default 0;
update seasonplayer set forfeit=0;

