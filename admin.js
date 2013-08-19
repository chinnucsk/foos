$(function() {

   var FOOS_HOST = 'http://foos.videoplaza.org/mongo/';
//   var FOOS_HOST = 'http://localhost:27080/';

   $("#changeName").click(function() {
      var criteria = 'criteria={"name":"' + $("#toName").val() + '"}'
      $.get(FOOS_HOST + "foos/player/_find", criteria, function(data) {
         var target = data.results[0];
         if (target) {
            alert(target.name + ' already exists. Choose another name!');
            return false;
         } else {
            var criteria = 'criteria={"name":"' + $("#fromName").val() + '"}'
            var update = '&newobj={"$set":{"name":"'+ $("#toName").val() + '"}}';
            $.post(FOOS_HOST + "foos/player/_update", criteria + update, 'json');
            var teamPositions = ["0.0", "0.1", "1.0", "1.1"];
            teamPositions.forEach(function(pos) {
               var criteria = 'criteria={"teams.'+ pos + '":"' + $("#fromName").val() + '"}'
               var update = '&newobj={"$set":{"teams.'+ pos + '":"'+ $("#toName").val() + '"}}';
               $.post(FOOS_HOST + "foos/game/_update", criteria + update + '&multi=true', 'json');
            });
            for (var i = 0; i < 19; ++i) {
               var criteria = 'criteria={"scores.' + i + '.player":"' + $("#fromName").val() + '"}'
               var update = '&newobj={"$set":{"scores.' + i + '.player":"'+ $("#toName").val() + '"}}';
               $.post(FOOS_HOST + "foos/game/_update", criteria + update + '&multi=true', 'json');
            }
            alert($("#fromName").val() + ' changed name to ' + $("#toName").val());
         }

      }, 'json');
   });
});


