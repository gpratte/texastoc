
ALTER TABLE player ADD readonly boolean default false;
update player set readonly = false;
