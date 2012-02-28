drop function upsert_match(_matchid text, _timestamp timestamp, _score integer[], _team_a text, _team_b text, _positions_a text[], _positions_b text[]);

create or replace function upsert_match(_matchid text, _timestamp timestamp, _score integer[], _team_a text, _team_b text, _positions_a text[], _positions_b text[])
    returns match as $$
declare
	_match match;
begin
	loop
		update match set timestamp = _timestamp, score = _score, team_a = _team_a, team_b = _team_b, positions_a = _positions_a, 
			positions_b = _positions_b where matchid = _matchid returning * into _match;
		if found then
			return _match;
		end if;
		
		begin
			insert into match (matchid, timestamp, score, team_a, team_b, positions_a, positions_b) values 
				(_matchid, _timestamp, _score, _team_a, _team_b, _positions_a, _positions_b) returning * into _match;
		    if found then
				return _match;
			end if;
			exception when unique_violation then
				-- do nothing, and loop and try the update again
		end;
	end loop;
end;
$$ language plpgsql;
