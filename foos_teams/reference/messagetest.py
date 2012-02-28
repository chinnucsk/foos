from flask import Blueprint, render_template, session, redirect, url_for, \
	 request, flash, g, jsonify, abort
import time
print 'messagetest.py'
from web import app
from gevent import monkey
monkey.patch_all()

mod = Blueprint('messagetest', __name__)
print 'en messagetest ' + __name__
from gevent.event import Event
import gevent

@mod.route('/')
def index():
	print 'mod name:' + mod.name
	print app.cache
	return render_template('messagetest.html',messages=app.cache, mod=mod)

@mod.route('/put',methods=['POST'])
def put_message():
	print 'atlantis is putting'
	message = request.form.get('message','')
	app.cache.append('{0} - {1}'.format(time.strftime('%m-%d %X'),message.encode('utf-8')))
	if len(app.cache) >= app.cache_size:
		app.cache = app.cache[-1:-(app.cache_size):-1]
	app.event.set()
	app.event.clear()
	return 'OK'

@mod.route('/poll',methods=['POST'])
def poll_message():
	print 'atlantis is polling'
	app.event.wait()
	print 'rait'
	return jsonify(dict(data=[app.cache[-1]]))
