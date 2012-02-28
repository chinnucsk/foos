from flask import Blueprint, render_template, session, redirect, url_for, \
	 request, flash, g, jsonify, abort
from web import mongo_conn
import log
# from web import app
from hashlib import md5

mod = Blueprint('profile', __name__)
game_collection = mongo_conn['game']

@mod.route('/profile/<player>')
def profile(player):

	# query gives matches where player has scored
	matches = list(game_collection.find({"scores.player" : player}))
	total_goals = 0
	goals = []
	stats = PersonalStats()
	
	# entries is every goal in the match where player has scored
	for entries in matches:
		#total_goals += len([value for value in entries['scores'] if value['player'] == "Pablo"])
		goals += [value for value in entries['scores'] if value['player'] == player]
	total_goals += len(goals)
	
	stats.total_goals = total_goals
	stats.games_played = len(matches)
	m = md5()
	m.update("pablo.medina@videoplaza.com")
	avatar_url = "http://www.gravatar.com/avatar/%s?s=200" % (m.hexdigest(),)
	return render_template('profile.html', goals = goals, personal_stats = stats, avatar_url = avatar_url, mod=mod)

@mod.route('/profile/number')
def n1():
    # gevent.sleep(2)
    return jsonify(dict(data=['fake number 5000']))

@mod.route('/profile/number2')
def n2():
    # gevent.sleep(5)
    return jsonify(dict(data=['fake number 10000']))




class PersonalStats:

	def __init__(self, total_goals=None, games_played=None):
		self.total_goals = total_goals
		self.games_played = games_played
   

"""
{u'_id': ObjectId('4e5b7f9097f2f77e9f000124'), u'scores': [{u'player': u'Pablo'}, {u'player': u'Joshua'}, {u'player': u'Michael'}, {u'player': u'Michael'}, {u'player': u'Geries'}, {u'player': u'Michael'}, {u'player': u'Michael'}, {u'player': u'Pablo'}, {u'player': u'Geries'}, {u'player': u'Pablo'}, {u'player': u'Geries'}, {u'player': u'Joshua'}, {u'player': u'Pablo'}, {u'player': u'Pablo'}, {u'player': u'Michael'}, {u'player': u'Joshua'}, {u'player': u'Joshua'}, {u'player': u'Michael'}]}
"""

