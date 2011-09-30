(function($) {
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
      wins: 0,
      losses: 0,
      winning_pct: 0.0,
      goals: 0,
      goals_per_game: 0.0,
      trueskill: 0.0,
      mu: 0.0,
      sigma: 0.0,
    },
//	games_played : function() { return this.get("wins") + this.get("losses"); }
//	,winning_pct : function () { return this.games_played() != 0 ? this.get("wins")/this.games_played() : 0;}
//	,goals_per_game : function () {return this.games_played() != 0 ? this.get("goals")/this.games_played(): 0;}
  });

  var PlayerList = Backbone.Collection.extend({
    model: Player,
    url: 'http://rouzbeh.videoplaza.org/foos.php',
  });

  var PlayerView = Backbone.View.extend({
    tagName: 'tr',

    initialize: function() {
      _.bindAll(this, 'render');
    },

    render: function() {
      console.log("rendering item");
      var html = '';
      _(columns).each(function(column) {
        if (this.model.has(attrFrom(column)))
          html += '<td>' + this.model.get(attrFrom(column)) + '</td>';
        else {
          console.log(attrFrom(column));
          var attr = this.model[attrFrom(column)];
          var tdContents = attr ? attr.call() : '';
          html += '<td>' + tdContents + '</td>';
        }
      }, this);
      $(this.el).html(html);

      return this;
    }
  });

  var PlayerListView = Backbone.View.extend({
    el: $('#players'),

    initialize: function() {
      _.bindAll(this, 'render', 'addPlayer', 'appendPlayer');
      console.log("initialize");
      this.collection = new PlayerList();
      this.collection.bind('all', this.render, this);
      this.collection.bind('add', this.appendPlayer,this);
      this.collection.bind('refresh', this.render,this);
      this.collection.fetch({success:function(c,r) {console.log("success" +r);},error:function(c,r) {console.log("FAIL"+r);window.errr=r}});
      this.counter = 0;

      this.render();
    },

    render: function() {
      console.log("rendering collection");
      this.$('tr','thead').html('');
      _(columns).each(function(column) {
        $('tr', 'thead', this.el).append('<th>' + column + '</th>');
      }, this);

      console.log("rendering items");
      _(this.collection.models).each(function(item) {
        this.appendPlayer(item);
      }, this);
      $('#players').tablesorter({sortList: [[0,0]]}); // sort on first column, ascending
    },

    /*** Fake data for demonstration purposes ***/
    addPlayer: function(name) {
      this.counter++;
      var games_played = Math.floor(Math.random() * 101) + 1;
      var wins = Math.floor(Math.random() * games_played) + 1;
      var losses = games_played - wins;
      var winning_pct = (wins / games_played * 100).toFixed(2) + '%';
      var goals = Math.floor(Math.random() * games_played * 10);
      var goals_per_game = (goals / games_played).toFixed(2);

      var item = new Player();
      item.set({
        rank: this.counter,
        name: name,
        games_played: games_played,
        wins: wins,
        losses: losses,
        winning_pct: winning_pct,
        goals: goals,
        goals_per_game: goals_per_game,
        trueskill: (Math.random() * 30).toFixed(10),
        μ: (Math.random() * 40).toFixed(10),
        σ: (Math.random() * 10).toFixed(10),
      });
      this.collection.add(item);
    },
    /*** ***/

    appendPlayer: function(item) {
      console.log("appendPlayer");
      var itemView = new PlayerView({
        model: item
      });
      $('tbody', this.el).append(itemView.render().el);
    }
  });

  var listView = new PlayerListView();
  
  //$('#players').tablesorter({sortList: [[0,0]]}); // sort on first column, ascending
})(jQuery);
