drop database toc;
create database toc;
grant all on toc.* TO tocuser@localhost identified by 'tocpass';
FLUSH PRIVILEGES;
