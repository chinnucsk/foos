from flask import Flask, session, g, render_template

from pymongo.connection import Connection
# from gevent.event import Event
import sys
import websiteconfig
import log

# log.info('basedir ' + websiteconfig._basedir)
#app = Flask(__name__, instance_relative_config=True)
app = Flask(__name__)
app.config.from_object('websiteconfig.DevelopmentConfig')

# If you do this you'll be responsible for rendering the .less files into CSS when you deploy in non-debug mode to your production server.
# if app.debug:
#     from flaskext.lesscss import lesscss
#     lesscss(app)

# log.info('instance path: ' + app.instance_path)
# log.info('root path: ' + app.root_path)
# log.info('module name: ' + __name__)
# log.info('after getting config')

# database to use // think about if this should be initialised every time
mongo_db = app.config['MONGO_DB']
mongo_conn = Connection(app.config['MONGO_DB_SERVER'], 27017)[mongo_db]
log.info(mongo_conn)

from views import profile, overview, teams, utils
app.register_blueprint(profile.mod)
app.register_blueprint(overview.mod)
app.register_blueprint(teams.mod)
app.register_blueprint(utils.mod)
