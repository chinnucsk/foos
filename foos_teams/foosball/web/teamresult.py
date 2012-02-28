from collections import Counter
from web import mongo_conn
import log

game_collection = mongo_conn['game']


def gather_all_teams():
	matches = game_collection.find({},{"teams":1, "score":1})
	teams_and_results = []
	for match in matches:
		try:
			scores = match['score']
		except KeyError:
			log.info("Score key error. Skipping %s" % match ) # means match not over
		win_team_idx = scores.index(max(scores))
		for idx, team in enumerate(match['teams']):
			team.sort() # make the team unique and hashable
			teams_and_results.append({"team":tuple(team), "result": 1 if idx == win_team_idx else 0 })
			# log.info("team %s: %s. Goals %s" % (idx, str(team), match["score"][idx]))
	return teams_and_results

def get_played(teams_and_results):
	team_count_dict = get_counter(teams_and_results)
	col = []
	for team_and_result in teams_and_results:
		newdic = {}
		newdic['result'] = team_and_result['result']
		newdic['team'] = team_and_result['team']
		newdic['played'] = team_count_dict[team_and_result['team']]
		col.append(newdic)
	return col

def get_counter(teams_and_results):
	extracted = []
	for i in teams_and_results:
		extracted.append(i['team'])
	return Counter(extracted)

def get_win_lose(teams_and_results):
	newdic = {}
	for i in teams_and_results:
		playerone = i['team'][0]
		playertwo = i['team'][1]
		team_key = '%s_%s' % (playerone,playertwo)
		try:
			newdic[team_key]
		except KeyError:
			newdic[team_key] = {}
		newdic[team_key]['played'] = i['played']
		try:
			newdic[team_key]['win'] = newdic[team_key]['win'] + i['result']
		except KeyError:
			newdic[team_key]['win'] = i['result']
		newdic[team_key]['lose'] = i['played'] - newdic[team_key]['win']
		newdic[team_key]['ratio'] = (float(newdic[team_key]['win'])/float(i['played']))*100
		newdic[team_key]['playerone'] = playerone
		newdic[team_key]['playertwo'] = playertwo
	return newdic
