
ALTER TABLE gameplayer ADD optIn boolean default false;

update gameplayer set optIn = false;
