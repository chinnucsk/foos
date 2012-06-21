(function($) {
  var Mode = {
    ALL_GAMES : 'allGames',
    NEW_TABLE : 'newTable',
    OLD_TABLE : 'oldTable',
    TODAY : 'today',
  };

  var DEFAULT_MODE = Mode.NEW_TABLE;
  var DEFAULT_START_DATE = '1324584000000';

  var columns = [
    'Rank',
    'Name',
    'Games played',
    'Wins',
    'Losses',
    'Winning pct',
    'Goals',
    'Goals per game',
    'TrueSkill',
    'mu',
    'sigma',
  ];

  var attrFrom = function(column) {
    return column.toLowerCase().replace(/ /g, '_');
  };

  var Player = Backbone.Model.extend({
    defaults: {
      rank: 0,
      name: 'player',
      games_played: 0,
      wins: 0,
      losses: 0,
      winning_pct: 0,
      goals: 0,
      goals_per_game: 0,
      trueskill: 0.0,
      mu: 0.0,
      sigma: 0.0,
    },

    setDynamicAttrs: function() {
      this.attributes.games_played = this.get('wins') + this.get('losses');
      this.attributes.winning_pct = ((this.get('games_played') != 0 ? this.get('wins') / this.get('games_played') : 0) * 100).toFixed(2) + '%';
      this.attributes.goals_per_game = (this.get('games_played') != 0 ? this.get('goals') / this.get('games_played') : 0).toFixed(2);
    },

    initialize: function() {
       _.bindAll(this, 'setDynamicAttrs');
    },
  });

  function playerDelta(one, two) {
     var delta = new Player();
     delta.attributes.name = one.get('name');
     delta.attributes.wins = one.get('wins') - two.get('wins');
     delta.attributes.losses = one.get('losses') - two.get('losses');
     delta.attributes.goals = one.get('goals') - two.get('goals');
     delta.attributes.trueskill = one.get('trueskill') - two.get('trueskill');
     delta.setDynamicAttrs();

     return delta;
  }

  function isChecked(element) {
    return (element.attr('checked') != undefined && element.attr('checked') == 'checked');
  }

  function getMode() {
    if (isChecked($('#newTable'))) return Mode.NEW_TABLE;
    if (isChecked($('#oldTable'))) return Mode.OLD_TABLE;
    if (isChecked($('#today'))) return Mode.TODAY;

    return Mode.ALL_GAMES;
  }

  function startOfDay(date) {
     var start = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 4, 0, 0, 0, 0); /// DEBUGGERY
     ///var start = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
     return start.getTime();
  }

  function getQueryString(mode) {
    if (mode == Mode.NEW_TABLE) {
      console.log('fetching data for new table');
      return '?startDate=' + DEFAULT_START_DATE;
    }

    if (mode == Mode.OLD_TABLE) {
      console.log('fetching data for old table');
      return '?endDate=' + DEFAULT_START_DATE;
    }

    if (mode == Mode.TODAY) {
      console.log('fetching data up until today');
      return getQueryString(DEFAULT_MODE) + '&endDate=' + startOfDay(new Date());
    }

    console.log('fetching data for all games');
    return '';
  }

  function getPlayerListUrl(mode) {
    return 'http://rouzbeh.videoplaza.org:8080/player' + getQueryString(mode);
  }

  function copyCollection(orig) {
     var copy = new Backbone.Collection();
     orig.each(function(model) {
        copy.add(new Backbone.Model(model.toJSON()));
     });

     return copy;
  }

  function findPlayer(playerList, name) {
     var foundPlayer;
     playerList.each(function(player) {
        if (name == player.get('name')) {
           foundPlayer = player;
           return false; // stop looping
        }
     });

     return foundPlayer;
  }

  var PlayerList = Backbone.Collection.extend({
    model: Player,
  });

  var PlayerView = Backbone.View.extend({
    tagName: 'tr',

    initialize: function() {
      _.bindAll(this, 'render');
    },

    render: function() {
      console.log("rendering item");
      var html = '';

      this.model.setDynamicAttrs();
      _(columns).each(function(column) {
        var tdContents = '';

        if (this.model.has(attrFrom(column))) {
          tdContents = this.model.get(attrFrom(column));
        }

        html += '<td>' + tdContents + '</td>';
      }, this);
      $(this.el).html(html);

      return this;
    }
  });

  var PlayerListView = Backbone.View.extend({
    el: $('#players'),

    fetchAndRender: function() {
       var mode = getMode();

       this.collection = new PlayerList();
       this.collection.url = getPlayerListUrl(mode == Mode.TODAY ? DEFAULT_MODE : mode);

       var view = this;
       this.collection.fetch({
          success: function(c,r) {
             if (mode == Mode.TODAY)
                view.leaderBoard(view, c);
          },
          error: function(c,r) {
             console.log("FAIL"+r);
             window.err=r;
          }
       });
       this.counter = 0;

       // Stats for today are displayed differently, so don't render the table on load
       if (mode != Mode.TODAY) {
          this.collection.bind('add', this.appendPlayer,this);
          this.collection.bind('all', this.render, this);
          this.collection.bind('refresh', this.render,this);
       }
    },

    initialize: function() {
      _.bindAll(this, 'render', 'appendPlayer');
      console.log("initialize");
      this.fetchAndRender();
    },

    events: {
      'click #newTable': 'fetchAndRender',
      'click #oldTable': 'fetchAndRender',
      'click #allGames': 'fetchAndRender',
      'click #today': 'fetchAndRender',
    },

    render: function() {
      console.log("rendering collection");
      this.$('table').html('');
      this.$('table').append('<thead></thead>');
      this.$('thead', 'table').append('<tr></tr>');
      _(columns).each(function(column) {
        $('tr', 'thead', 'table', this.el).append('<th>' + column + '</th>');
      }, this);

      console.log("rendering items");
      this.$('table').append('<tbody></tbody>');
      _(this.collection.models).each(function(item) {
        this.appendPlayer(item);
      }, this);
      $('.tablesorter').tablesorter({sortList: [[0,0]]}); // sort on first column, ascending
    },

    leaderBoard: function(view, c) {
       var allGames = copyCollection(c);

       view.collection.url = getPlayerListUrl(getMode());
       view.collection.fetch({
          success: function(c,r) {
             view.calculateLeaderBoard(view, allGames, c);
          },
          error: function(c,r) {
             console.log("FAIL"+r);
             window.err=r;
          }
       });
    },

    calculateLeaderBoard: function(view, allGames, gamesExcludingPeriod) {
       var leaders = new Backbone.Collection();
       gamesExcludingPeriod.each(function(player) {
          var one = findPlayer(allGames, player.get('name'));
          var delta = playerDelta(one, player);
          if (delta.get('games_played') > 0)
             leaders.add(delta);
       });

       // TODO: fix rank
       var foo = 1; /// DEBUGGERY
    },

    appendPlayer: function(item) {
      console.log("appendPlayer");
      var itemView = new PlayerView({
        model: item
      });
      $('tbody', 'table', this.el).append(itemView.render().el);
    }
  });

  var listView = new PlayerListView();

})(jQuery);
