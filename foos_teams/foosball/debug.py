#from gevent.pywsgi import WSGIServer
# from web import log
import log

def initMessage():
	log.info("========= RUNNING debug.py ================")

initMessage()

from web import app
app.run('0.0.0.0',3031, debug=True)
# WSGIServer(('0.0.0.0',5000),app,log=None).serve_forever()
# app.run()

