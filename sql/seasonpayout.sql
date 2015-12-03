create table seasonpayout 
(
id int not null auto_increment,
seasonId int not null,
place varchar(64) not null,
amount int not null default 0,
description varchar(1024),
temp boolean not null default false,
primary key(id)
); 

alter table seasonpayout add foreign key (seasonId) references season(id);
