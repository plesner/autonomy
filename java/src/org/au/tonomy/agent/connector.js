(function () {
  // The URL of the page that is allowed to communicate with this proxy.
  var kTargetOrigin = "%1$s";

  // Set of all the methods understood by this proxy.
  var kEndPoints = %2$s;

  // Id of the connection attempt that caused this file to be served.
  var kConnectAttempt = %3$s;

  // Establishes a connection with the host page.
  function connectToHost() {
    window.addEventListener("message", function (event) {
      forwardRequest(event);
    });
    postMessage("frameConnect", [], kConnectAttempt);
  }

  // Posts a message to the parent frame.
  function postMessage(method, data, serial) {
    window.parent.postMessage(JSON.stringify([method, data, serial]), kTargetOrigin);
  }

  // Returns a server response to the parent frame.
  function postResponse(response, status, serial) {
    postMessage("respond", [status, response], serial);
  }

  // Forwards a browser request to the file proxy.
  function forwardRequest(event) {
    var data = JSON.parse(event.data);
    var endPoint = data[1];
    // Only forward messages that we know the agent will understand.
    if (!kEndPoints[endPoint]) {
      console.log("Blocked unsupported message", data);
      return;
    }
    var id = data[3];
    var xhr = new XMLHttpRequest();
    if (id != -1) {
      // We only care about the response if the message is sync.
      xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
          postResponse(xhr.responseText, xhr.status, id);
        }
      };
    }
    var params = data[2];
    var paramString = params
        .map(function (pair) { return pair[0] + "=" + encodeURIComponent(pair[1]); })
        .join("&");
    var paramSuffix = paramString.length == 0 ? "" : "?" + paramString;
    var method = data[0];
    xhr.open(method, "json/" + endPoint + paramSuffix, true);
    xhr.send(null);
  }

  // Connect as soon as the page is loaded.
  document.addEventListener("DOMContentLoaded", connectToHost);
})();
