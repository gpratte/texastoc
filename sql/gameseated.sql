ALTER TABLE game ADD seated int not null default 0;
update game set seated=0;
