
ALTER TABLE gameplayer ADD knockedout boolean default false;

update gameplayer set knockedout = false;
