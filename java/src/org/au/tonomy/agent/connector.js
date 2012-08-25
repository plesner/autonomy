(function () {
  // The URL of the page that is allowed to communicate with this proxy.
  var targetOrigin = "%1$s";

  // Set of all the methods understood by this proxy.
  var methods = %2$s;

  // Id of the connection attempt that caused this file to be served.
  var connectAttempt = %3$s;

  // Establishes a connection with the host page.
  function connectToHost() {
    window.addEventListener("message", function (event) {
      forwardRequest(event);
    });
    postMessage("frameConnect", [], connectAttempt);
  }

  // Posts a message to the parent frame.
  function postMessage(method, options, serial) {
    window.parent.postMessage(JSON.stringify([method, options, serial]), targetOrigin);
  }

  // Forwards a browser request to the file proxy.
  function forwardRequest(event) {
    var data = JSON.parse(event.data);
    var method = data[0];
    // Only forward messages that we know the agent will understand.
    if (!methods[method]) {
      console.log("Blocked unsupported message", data);
      return;
    }
    var id = data[2];
    var xhr = new XMLHttpRequest();
    if (id != -1) {
      // We only care about the response if the message is sync.
      xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
          postMessage("respond", xhr.responseText, id);
        }
      };
    }
    var params = data[1];
    var paramString = params
        .map(function (pair) { return pair[0] + "=" + encodeURIComponent(pair[1]); })
        .join("&");
    var paramSuffix = paramString.length == 0 ? "" : "?" + paramString;
    xhr.open("GET", "json/" + method + paramSuffix, true);
    xhr.send(null);
  }

  // Connect as soon as the page is loaded.
  document.addEventListener("DOMContentLoaded", connectToHost);
})();
