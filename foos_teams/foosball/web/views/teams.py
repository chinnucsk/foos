from flask import Blueprint, render_template, session, redirect, url_for, \
	 request, flash, g, abort, jsonify
from web import app
# from web.database import team, goal, match, conn
# from sqlalchemy.sql import select, and_, or_
import log
from web import db
from psycopg2.extras import DictCursor, RealDictCursor
# from web.jzon import jsonify
# from fedora.tg.json import jsonify_saresult

mod = Blueprint('teams', __name__)
conn = None
cur = None

@mod.route('/teams')
def teams():
	global conn, cur
	conn = db.connect_db()
	cur = conn.cursor(cursor_factory=RealDictCursor)
	cur.execute('select timestamp from match order by timestamp desc limit 1')
	result = cur.fetchall()
	return render_template('teams.html', mod=mod, timestamp=result[0]['timestamp'])
	
@mod.route('/teams/getteams',methods=['POST', 'GET'])
def get_teams():
	player = request.form.get('player')
	global conn, cur
	conn = db.connect_db()
	cur = conn.cursor(cursor_factory=RealDictCursor)
	if player == None or player == '':
		cur.execute('select * from team order by wlratio desc, played desc')
	else:
		cur.execute('select * from team where %s = any(players) order by wlratio desc, played desc', (player,))
	return jsonify(data=cur.fetchall())

	
@mod.before_request
def open_conn():
	# global conn
	pass
	
	
@mod.after_request
def close_result_proxy(response):
	# print response.mimetype
	global conn, cur
	if cur != None and not cur.closed:
		cur.close()
	if conn != None and not conn.closed:
		conn.close()
	return response
	
	
