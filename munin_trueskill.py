#!/usr/bin/python

import sys
import json
from urllib2 import urlopen

url = 'http://rouzbeh.videoplaza.org/foos.php'
players = json.loads(urlopen(url).read())

# config printers
def conf(player, type):
    id = player['name']
    print '%s.label %s'   %(id, player['name'])
    print '%s.min 0'      %(id)
    print '%s.type %s'    %(id, type)
    print '%s.draw LINE1' %(id)

# config
if len(sys.argv) > 1 and sys.argv[1] == 'config':
    print 'graph_title Videoplaza Foos Trueskill'
    print 'graph_args --base 1000 -l 0'
    print 'graph_vlabel trueskill'
    print 'graph_category Foos'
    for player in players:
        conf(player, 'GAUGE')
    exit(0)


# print values (always the 'thisWeek' value)
for player in players:
    print '%s.value %f' %(player['name'], player['trueskill'])
