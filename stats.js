(function($) {
  var NEW_TABLE_START_DATE = '1324584000000';

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

  function isChecked(element) {
    return (element.attr('checked') != undefined && element.attr('checked') == 'checked');
  }

  function getQueryString() {
     if (isChecked($('#newTable')))
       return '?startDate=' + NEW_TABLE_START_DATE;

     if (isChecked($('#oldTable')))
       return '?endDate=' + NEW_TABLE_START_DATE;

    return '';
  }

  var PlayerList = Backbone.Collection.extend({
    model: Player,
    url: 'http://rouzbeh.videoplaza.org:8080/player' + getQueryString(),
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

    initialize: function() {
      _.bindAll(this, 'render', 'appendPlayer');
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
