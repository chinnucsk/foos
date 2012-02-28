create or replace function upsert_team(_teamid text, _players text[])
    returns void as $$
declare
	_wins integer;
	_wins_a integer;
	_wins_b integer;
	_loses integer;
	_loses_a integer;
	_loses_b integer;
	_played integer;
	_wlratio numeric := 0;
begin
	select count(*) as cnt into _loses_a from match where _teamid = team_a and score[1] < score[2];
	select count(*) as cnt into _loses_b from match where _teamid = team_b and score[1] > score[2];
	select count(*) as cnt into _wins_a from match where _teamid = team_a and score[1] > score[2];
	select count(*) as cnt into _wins_b from match where _teamid = team_b and score[1] < score[2];
	_wins := _wins_a + _wins_b;
	_loses := _loses_a + _loses_b;
	_played := (_wins + _loses);
	if (_played > 0) then
		_wlratio := cast(_wins as float)/_played;
	end if;
 	update team set players = _players, wins = _wins, loses = _loses, wlratio = round(_wlratio,3), played = _played where teamid = _teamid;
	if not found then
		insert into team (teamid, players, wins, loses, wlratio, played) values (_teamid, _players, _wins, _loses, round(_wlratio,3), _played);
	    return;
	end if;
end;
$$ language plpgsql volatile;