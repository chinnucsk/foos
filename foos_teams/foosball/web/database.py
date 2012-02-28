from web import app

from sqlalchemy.orm import sessionmaker, scoped_session
# from sqlalchemy.sql.expression import text
# from sqlalchemy.sql import select
from sqlalchemy import create_engine, func, Column, Integer
from sqlalchemy import Table, Column, Integer, String, MetaData, ForeignKey, Float, ColumnDefault
from sqlalchemy.dialects.postgresql import \
    ARRAY, BIGINT, BIT, BOOLEAN, BYTEA, CHAR, CIDR, DATE, \
    DOUBLE_PRECISION, ENUM, FLOAT, INET, INTEGER, INTERVAL, \
    MACADDR, NUMERIC, REAL, SMALLINT, TEXT, TIME, TIMESTAMP, \
    UUID, VARCHAR

metadata = None
conn = None

def connect_and_bind():
	"""docstring for de"""
	global metadata
	global conn
	engine = create_engine('postgresql+psycopg2://%s:%s@%s:%s/%s' % (app.config['PG_USER'], app.config['PG_PASS'], 'localhost', '5432', app.config['PG_DB']), echo = False)
	conn = engine.connect()
	Session = scoped_session(sessionmaker())
	s = Session(bind=conn)
	metadata = MetaData()

connect_and_bind()

team = Table('team', metadata,
	Column('teamid', TEXT, primary_key = True),
	Column('players', ARRAY(String)),
	Column('wins', INTEGER),
	Column('loses', INTEGER), 
	Column('played', INTEGER),
	Column('wlratio', FLOAT),
)

match = Table('match', metadata,
	Column('matchid', TEXT, primary_key = True, nullable = False),
	Column('timestamp', TIMESTAMP),
	Column('score', ARRAY(Integer)),
	Column('positions_a', ARRAY(String)), 
	Column('positions_b', ARRAY(String)), 
	Column('team_a', TEXT), 
	Column('team_b', TEXT), 
)

goaltype = ENUM('normal','donk','owngoal')
goal = Table('goal', metadata,
	Column('goalid', TEXT, primary_key = True),
	Column('timestamp', TIMESTAMP),
	Column('position', TEXT),
	Column('player', TEXT),
	Column('goaltype', goaltype, nullable = False, default = 'normal'), 
	Column('matchid', None, ForeignKey('match.matchid')), 
)
