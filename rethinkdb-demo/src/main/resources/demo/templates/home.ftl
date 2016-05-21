<!DOCTYPE HTML>
<html>
  <head>
    <script src="/jquery.js"></script>
    <link rel="stylesheet" type="text/css" media="screen" href="/style.css" />
  </head>
  <body>
    <script type="text/javascript">
    $(document).ready(function() {
      function renderMessage(data) {
        var dom = $("<div></div>");
        dom.text(data.name + ': ' + data.message);
        dom.appendTo($("#messages"));
      }

      var socket = new WebSocket("${endpoint}");
      socket.onopen = function(event) {
        window.console.log(event);
      };
      socket.onmessage = function(event) {
        window.console.log(event);
        var messages = JSON.parse(event.data);

        for (var i = 0; i < messages.length; i++) {
          renderMessage(messages[i]);
        }
      };
      socket.onerror = function(event) {
        window.console.log(event);
      };
      socket.onclose = function(event) {
        window.console.log(event);
      };

      $("#sender").submit(function(event) {
        event.preventDefault();
        var input = $("#sender").find("input[name=message]");
        socket.send(input.val());
        input.val('');
      });
    });

    </script>
    <div id="messages">

    </div>
    <form id="sender">
      <input type="text" name="message" />
      <input type="submit" value="Send!" />
    </form>
  </body>
</html>
