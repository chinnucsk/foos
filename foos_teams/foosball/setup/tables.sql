create type goaltype as enum (
    'normal',
    'donk',
	'owngoal'
);

drop table team;
drop table goal;
drop table match;

create table match 
( 
	matchid varchar not null, 
	timestamp timestamp,
	score integer[], -- not really sure how.. 4 5? // in mongodb, if the score is empty, the match is not over. Do not count it
	positions_a varchar[], -- "[Pablo,Nils]" -- positions in game
	positions_b varchar[], -- positions in game
	team_a varchar, -- team id
	team_b varchar, -- team id
	primary key (matchid)
);

create table goal (
	goalid varchar,
	timestamp timestamp,
	position varchar, 
	player varchar,
	type goaltype not null default 'normal',
	matchid varchar,
	primary key (goalid),
	foreign key (matchid) references match(matchid)
);

drop table if exists team;
create table team (
	teamid varchar not null,
	players varchar[],
	wins integer,
	loses integer,
	played integer,
	-- avgts numeric(2), -- double? float?
	wlratio float,
	primary key (teamid)
);


select * from team where 'Pablo' = any(players);
select * from match where 'Pablo' = any(positions_a) or 'Pablo' = any(positions_b);
