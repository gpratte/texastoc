ALTER TABLE game ADD annualIndex int;
ALTER TABLE game ADD quarterlyIndex int;
update game set annualIndex=1;
update game set quarterlyIndex=1;
