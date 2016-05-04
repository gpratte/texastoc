
ALTER TABLE season ADD annualTocAmount int default 0;
update season set annualTocAmount = 5;
update season set annualTocAmount = 10 where id = 10;

