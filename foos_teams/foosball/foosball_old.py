# all the imports
import psycopg2
import sqlite3
from flask import Flask, request, session, g, redirect, url_for, \
     abort, render_template, flash

# configuration
DATABASE = 'foosball'
#DATABASE_URI = 'mysql://user@localhost/foo'
DEBUG = True
SECRET_KEY = 'development key'
USERNAME = 'foosball'
PASSWORD = 'foosball'

conn = None
# create our little application :)
app = Flask(__name__)
app.config.from_object(__name__)

def connect_db():
    conn = psycopg2.connect("dbname=%s user=%s password=%s" % (app.config['DATABASE'],app.config['USERNAME'],app.config['PASSWORD']))
    return conn 

@app.route('/', methods=['GET'])
def show_entries():
    global conn
    cur = conn.cursor()
    cur.execute("SELECT * FROM test;")
    entries = [dict(title=row[0], text=row[1]) for row in cur.fetchall()]
    #conn.close()
    return render_template('show_entries.html', entries=entries)



@app.route('/add', methods=['POST'])
def add_entry():
    if not session.get('logged_in'):
        abort(401)
    #g.db.execute('insert into entries (title, text) values (?, ?)',
                 #[request.form['title'], request.form['text']])
    #g.db.commit()
    flash('New entry was successfully posted')
    return redirect(url_for('show_entries'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        if request.form['username'] != app.config['USERNAME']:
            error = 'Invalid username'
        elif request.form['password'] != app.config['PASSWORD']:
            error = 'Invalid password'
        else:
            session['logged_in'] = True
            flash('You were logged in')
            return redirect(url_for('show_entries'))
    return render_template('login.html', error=error)

@app.route('/logout')
def logout():
    session.pop('logged_in', None)
    flash('You were logged out')
    return redirect(url_for('show_entries'))


@app.before_request
def before_request():
    global conn
    conn = connect_db()

@app.teardown_request
def teardown_request(exception):
    #if conn != None:
    if conn:
        conn.close()
        print "conn was closed"
    else:
        print "conn was none"
    #pass

if __name__ == '__main__':
    app.run()

