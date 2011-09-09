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
    'μ',
    'σ',
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
      winning_pct: 0.0,
      goals: 0,
      goals_per_game: 0.0,
      trueskill: 0.0,
      μ: 0.0,
      σ: 0.0,
    }
  });

  var PlayerList = Backbone.Collection.extend({
    model: Player
  });

  var PlayerView = Backbone.View.extend({
    tagName: 'tr',

    initialize: function() {
      _.bindAll(this, 'render');
    },

    render: function() {
      var html = '';
      _(columns).each(function(column) {
        html += '<td>' + this.model.get(attrFrom(column)) + '</td>';
      }, this);
      $(this.el).html(html);

      return this;
    }
  });

  var PlayerListView = Backbone.View.extend({
    el: $('#players'),

    initialize: function() {
      _.bindAll(this, 'render', 'addPlayer', 'appendPlayer');

      this.collection = new PlayerList();
      this.collection.bind('add', this.appendPlayer);

      this.counter = 0;
      this.render();

      /*** Fake data for demonstration purposes ***/
      this.addPlayer('Rouzbeh');
      this.addPlayer('Bergman');
      this.addPlayer('Jesper');
      this.addPlayer('Alex');
      this.addPlayer('Jakob');
      this.addPlayer('Sarnelid');
      this.addPlayer('Joshua');
      this.addPlayer('Hedenius');
      this.addPlayer('Pablo');
      this.addPlayer('Per-Anders');
      this.addPlayer('Michael');
      this.addPlayer('Alfred');
      this.addPlayer('Geries');
      this.addPlayer('Haseeb');
      this.addPlayer('Björn');
      this.addPlayer('Nils');
      this.addPlayer('Fredrik');
      this.addPlayer('Owen');
      this.addPlayer('Yonathan');
      this.addPlayer('Dante');
      this.addPlayer('Molgan');
      this.addPlayer('Timh');
      this.addPlayer('Caroline');
      this.addPlayer('Nic');
      this.addPlayer('Elin');
      this.addPlayer('Manuel');
      this.addPlayer('Erik');
      this.addPlayer('Nelson');
      this.addPlayer('Jonas');
      /*** ***/
    },

    render: function() {
      _(columns).each(function(column) {
        $('tr', 'thead', this.el).append('<th>' + column + '</th>');
      }, this);
      _(this.collection.models).each(function(item) {
        appendPlayer(item);
      }, this);
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
      var itemView = new PlayerView({
        model: item
      });
      $('tbody', this.el).append(itemView.render().el);
    }
  });

  var listView = new PlayerListView();
  $('#players').tablesorter({sortList: [[0,0]]}); // sort on first column, ascending
})(jQuery);
