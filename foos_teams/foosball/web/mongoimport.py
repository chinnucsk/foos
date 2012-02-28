from web import mongo_conn, app
import psycopg2
import log
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy import create_engine, func, Column, Integer
from sqlalchemy.sql.expression import text
import time

game_collection = mongo_conn['game']
conn = None
c = None
s = None
engine = None

def import_games(new_games_only=False):
	updated = 0
	log.info('new games only? %s' % bool(new_games_only))
	a = time.time()
	before_request()
	# global conn
	global s
	global engine
	global c
	unfinished = 0
	matches = list(game_collection.find())
	# cur = conn.cursor()
	for match in matches:
		if match.get('score') == None:
			unfinished = unfinished + 1
			continue

		oid = match['_id']

		# TODO extract
		# if 'only new', query the db for this match. If found, continue.
		if new_games_only:
			result = s.execute('select matchid from match where matchid = :matchid', params={'matchid':str(oid),})
			if result.rowcount > 0:
				continue
		
		score = map(lambda x: int(x),match['score']) # converts every element into int
		positions_a = map(lambda x: x.encode('utf-8'), match['teams'][0])
		positions_b = map(lambda x: x.encode('utf-8'), match['teams'][1])
		team_a = get_team_key(positions_a)
		team_b = get_team_key(positions_b)
		
		# TODO leave the option of update existing matches or just insert the new ones

		# callproc_params = [str(oid),match['date'],score,team_a,team_b,positions_a,positions_b]
		# callproc_params = {'_matchid':str(oid),'_timestamp':match['date'],'_score':score,
		# '_team_a':team_a,'_team_b':team_b,'_positions_a':positions_a,'_positions_b':positions_b}

		# cur.callproc('upsert_match', callproc_params)
		# s.execute(func.upsert_match(callproc_params))
		
		t = text("select * from upsert_match(:matchid, :tim, :score, :team_a, :team_b, :pos_a, :pos_b)")
			
		result = s.execute(t,params=dict(
			matchid=str(oid),
			tim=match['date'],
			score=score,
			team_a=team_a,
			team_b=team_b, 
			pos_a=positions_a,
			pos_b=positions_b)
			)
		
		updated = updated + 1

		[team.sort() for team in match['teams']]
		# TODO extract
		positions_a = map(lambda x: x.encode('utf-8'), match['teams'][0])
		positions_b = map(lambda x: x.encode('utf-8'), match['teams'][1])
		s.execute(text('select * from upsert_team(:id,:team)'), params=[
			dict(id=team_a, team=positions_a), 
			dict(id=team_b, team=positions_b), 			
			])
	
	# conn.commit()
	s.commit()
	
	teardown_request()
	log.info("unfinished matches: %d" % unfinished)
	log.info("updated matches: %d" % updated)

	return updated

def get_team_key(original_team):
	team = []
	team.extend(original_team)
	team.sort()
	playerone = team[0]
	playertwo = team[1]
	team_key = '%s_%s' % (playerone,playertwo)
	# if team_key == 'Alex_Blake':
	# 	log.info(team_key)
	return team_key

# def clean_db():
# 	before_request()
# 	global conn
# 	cur = conn.cursor()
# 	cur.execute("DELETE FROM match")
# 	conn.commit()
# 	teardown_request()

# def connect_db():
# 	conn = psycopg2.connect("dbname=%s user=%s password=%s" % 
# 		(app.config['PG_DB'],app.config['PG_USER'],app.config['PG_PASS']))
# 	return conn


def before_request():
	# global conn
	# conn = connect_db()
	global c
	global s
	global engine
	engine = create_engine('postgresql+psycopg2://%s:%s@%s:%s/%s' % (app.config['PG_USER'], app.config['PG_PASS'], 'localhost', '5432', app.config['PG_DB']), echo = False)
	c = engine.connect()
	Session = scoped_session(sessionmaker())
	s = Session(bind=c)
	# s = sessionmaker(bind=c)

def teardown_request():
	if conn:
		conn.close()
		print "conn was closed"
	else:
		print "conn was none"
	if s:
		s.close()
"""
{
	'date': datetime.datetime(2011, 8, 23, 8, 8, 4, 386000),
	'_id': ObjectId('4e535fe597f2f77e9f000079'),
	'teams': [
		['Rouzbeh', 'Joshua'],
		['Hedenius', 'Alex']
	],
	'score': [5, 10],
	'scores': [{
		'date': datetime.datetime(2011, 8, 23, 8, 9, 18, 384000),
		'player': 'Rouzbeh',
		'team': 0,
		'position': 'w2'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 10, 1, 278000),
		'player': 'Joshua',
		'team': 0,
		'position': 'w7'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 10, 27, 69000),
		'player': 'Alex',
		'team': 1,
		'position': 'b9'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 11, 0, 128000),
		'player': 'Alex',
		'team': 1,
		'position': 'b7'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 11, 42, 192000),
		'player': 'Alex',
		'team': 1,
		'position': 'b5'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 12, 27, 476000),
		'player': 'Hedenius',
		'team': 1,
		'position': 'b2'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 13, 0, 147000),
		'player': 'Joshua',
		'team': 0,
		'position': 'w9'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 13, 27, 52000),
		'player': 'Joshua',
		'team': 0,
		'position': 'w9'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 14, 46, 200000),
		'player': 'Hedenius',
		'team': 1,
		'position': 'b2'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 15, 33, 294000),
		'player': 'Alex',
		'team': 1,
		'position': 'w2'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 16, 2, 684000),
		'player': 'Hedenius',
		'team': 1,
		'position': 'w3'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 16, 29, 63000),
		'player': 'Alex',
		'team': 1,
		'position': 'w0'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 16, 41, 750000),
		'player': 'Rouzbeh',
		'team': 0,
		'position': 'b7'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 16, 59, 281000),
		'player': 'Alex',
		'team': 1,
		'position': 'w2'
	}, {
		'date': datetime.datetime(2011, 8, 23, 8, 17, 36, 879000),
		'player': 'Hedenius',
		'team': 1,
		'position': 'w8',
		'ownGoal': True
	}]
}
"""