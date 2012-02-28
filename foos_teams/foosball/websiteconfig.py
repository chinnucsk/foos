import os

_basedir = os.path.abspath(os.path.dirname(__file__))

class Config(object):
	DEBUG = False
	SECRET_KEY = 'testkey'
	# MONGO_DB_SERVER = 'localhost'
	MONGO_DB_SERVER = 'rouzbeh.videoplaza.org'
	MONGO_DB = 'foos'
	PG_DB_SERVER = 'localhost'
	PG_DB = 'foosball'
	PG_USER = 'foosball'
	PG_PASS = 'foosball'
	
class ProductionConfig(Config):
	# DATABASE_URI = 'mysql://user@localhost/foo'
	pass

class DevelopmentConfig(Config):
	DEBUG = True

class MakoConfig(Config):
	# one or more directories
	MAKO_DIR = 'foosball/web/templates'
	# optional, if specified Mako will cache to this directory
	MAKO_CACHEDIR = '/tmp/mako'
	# optional, if specified Mako will respect the cache size
	MAKO_CACHESIZE = 500
	

del os
