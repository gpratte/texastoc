
ALTER TABLE supply ADD kittyAmount int;

ALTER TABLE season ADD kittyGameDebit int default 0;
update season set kittyGameDebit = 0;

ALTER TABLE game ADD kittyDebit int default 0;
update game set kittyDebit = 0;
