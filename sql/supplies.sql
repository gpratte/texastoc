
create table supply 
(
id int not null auto_increment,
gameId int,
seasonId int,
typeText varchar(32) not null,
prizePotAmount int,
annualTocAmount int,
description varchar(1024),
primary key(id)
); 

ALTER TABLE game ADD totalPotSupplies int default 0;
ALTER TABLE game ADD totalAnnualTocSupplies int default 0;

update game set totalPotSupplies = 0;
update game set totalAnnualTocSupplies = 0;

ALTER TABLE season ADD totalAnnualTocSupplies int default 0;
update season set totalAnnualTocSupplies = 0;

ALTER TABLE supply ADD createDate date not null;
