ALTER TABLE game ADD homegame int not null default 1;
update game set homegame=1;

