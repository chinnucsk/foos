import sys
import os

def initMessage():
	log.info("""
	================
	RUNNING run.py
	================
	""")


initMessage()

# flaskfirst = "/projects/foosball/foosball"
# flaskfirst = "/Users/pmedina/development/python/projects/foosball/foosball"
flaskfirst = os.path.dirname(__file__)

log.info('FLASKFIRST: ' + flaskfirst)
if not flaskfirst in sys.path:
    sys.path.insert(0, flaskfirst)

# for i in sys.path:
    # print i
    
from web import app
print 'first time'
application = app