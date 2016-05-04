
ALTER TABLE season ADD quarterlyTocAmount int default 0;
update season set quarterlyTocAmount = 5;
update season set quarterlyTocAmount = 10 where id = 10;

ALTER TABLE season ADD quarterlyTocPayouts int default 0;
update season set quarterlyTocPayouts = 2;
update season set quarterlyTocPayouts = 3 where id = 10;
