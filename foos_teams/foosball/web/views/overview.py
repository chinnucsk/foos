from flask import Blueprint, render_template, session, redirect, url_for, \
	 request, flash, g, jsonify, abort
# from web import mongo_connv
# import time
import log
from web import teamresult


mod = Blueprint('overview', __name__)
# game_collection = mongo_conn['game']

@mod.route('/')
def overview():
	return render_template('overview.html', mod=mod)

@mod.route('/getteamresults',methods=['POST'])
def getteamresults():
	teams_and_results = teamresult.gather_all_teams()
	teams_and_results = teamresult.get_played(teams_and_results)
	teams_played_results = teamresult.get_win_lose(teams_and_results)
	results = teams_played_results.items()
	results = sorted(results, key=lambda result: result[1]['ratio']) 
	return jsonify(dict(data=results))