from web import app

import psycopg2
import log


def connect_db():
	conn = psycopg2.connect("host=localhost dbname=%s user=%s password=%s" % 
		(app.config['PG_DB'],app.config['PG_USER'],app.config['PG_PASS']))
	return conn

# def before_request():
# 	global conn
# 	conn = connect_db()

def teardown_request():
	if conn:
		conn.close()
		print "conn was closed"
	else:
		print "conn was none"
	if s:
		s.close()