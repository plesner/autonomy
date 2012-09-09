(function () {
  // The URL of the page that is allowed to communicate with this proxy.
  var kTargetOrigin = "%1$s";

  // The web socket connected to the server.
  var serverConnection;
  
  // Connect to the server 
  function connectToServer() {
	var location = window.location;
	var socketUrl = "ws://" + location.host + location.pathname + "api";
    serverConnection = new WebSocket(socketUrl);
    serverConnection.onopen = connectToHost;
    serverConnection.onmessage = function (event) {
      forwardServerMessage(event.data);
    };
    serverConnection.onclose = function (event) {
      console.log(event);
    };
  }
  
  // Forwards a message from the server to the host.
  function forwardServerMessage(message) {
    window.parent.postMessage(message, kTargetOrigin);
  }
  
  // Establishes a connection with the host page.
  function connectToHost() {
    window.addEventListener("message", forwardHostMessage);
    forwardServerMessage("");
  }

  // Forwards a browser request to the server.
  function forwardHostMessage(event) {
    serverConnection.send(event.data);
  }

  // Connect as soon as the page is loaded.
  document.addEventListener("DOMContentLoaded", connectToServer);
})();
