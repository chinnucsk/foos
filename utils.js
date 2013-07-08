Mode = {
   SEASON_2013Q4:'season2013Q4',
   SEASON_2013Q3:'season2013Q3',
   SEASON_2013Q2:'season2013Q2',
   SEASON_2013Q1:'season2013Q1',
   SEASON_2012Q4:'season2012Q4',
   ALL_GAMES:'allGames',
   SEASON_2012Q1_3:'season2012Q1_3',
   OLD_TABLE:'oldTable',
   THIS_WEEK:'thisWeek',
   TODAY:'today',
};

SEASON_TIMESTAMPS = {
   'season2013Q4':{'start':'1380578400000', 'end':'1388530800000'},
   'season2013Q3':{'start':'1372629600000', 'end':'1380578400000'},
   'season2013Q2':{'start':'1364767200000', 'end':'1372629600000'},
   'season2013Q1':{'start':'1356994800000', 'end':'1364767200000'},
   'season2012Q4':{'start':'1346018400000', 'end':'1356994800000'},
   'allGames':{'start':'0', 'end':'95649030000000'},
   'season2012Q1_3':{'start':'1324584000000', 'end':'1346018400000'},
   'oldTable':{'start':'0', 'end':'1324584000000'},
};

DEFAULT_MODE = Mode.SEASON_2013Q3;

MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;

TRUESKILL_SERVER = 'http://foos.videoplaza.org:8080';

function currentSeason() {
   return SEASON_TIMESTAMPS[DEFAULT_MODE];
}

function makeTimestamps(mode) {
   var season = mode ? SEASON_TIMESTAMPS[mode] : currentSeason();

   return 'startDate=' + season['start'] + '&endDate=' + season['end'];
}
