
ALTER TABLE gameplayer ADD emailOptIn boolean default false;

update gameplayer set emailOptIn = false;
