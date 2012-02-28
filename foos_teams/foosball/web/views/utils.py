from flask import Blueprint, render_template, session, redirect, url_for, \
	 request, flash, g, jsonify, abort
from web import mongo_conn, mongoimport
import psycopg2
import log
# from web import app
from hashlib import md5

mod = Blueprint('utils', __name__)
game_collection = mongo_conn['game']

@mod.route('/utils/reload',methods=['POST', 'GET'])
def reload_data():
	# new = request.form.get('new')
	new = request.args.get('new')
	log.info('importing new matches: %s' % new )
	updated_matches = mongoimport.import_games(new)
	# list_of_matches = list(matches)
	message = "Imported %s games from mongodb into PostgreSQL. Reload the page with awe!" % updated_matches 
	return jsonify(data={'message':message})


# @mod.route('/utils/clean')

# def clean():
# 	mongoimport.clean_db()
# 	message = "db cleaned"
# 	return render_template('utils.html', message=message)

