#!/usr/bin/python
#
# A helper process for accessing the local file system.

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import optparse
import os
import urlparse

_INDEX_SCRIPT = """\
(function () {
  // The URL of the page that is allowed to communicate with this proxy.
  var targetOrigin = "%(target_origin)s";

  // Set of all the methods understood by this proxy.
  var methods = %(methods)s;

  // Establishes a connection with the host page.
  function connectToHost() {
    window.addEventListener("message", function (event) {
      forwardRequest(event);
    });
    postMessage("frameConnect", [], 0);
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
    if (!methods[method])
      return;
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
    xhr.open("GET", "agent/" + method + paramSuffix, true);
    xhr.send(null);
  }

  // Connect as soon as the page is loaded.
  document.addEventListener("DOMContentLoaded", connectToHost);
})();\
"""


_INDEX_PAGE = "<html><head><script>%s</script></head><body></body></html>" % _INDEX_SCRIPT


# Escapes that have a special syntax.
_SPECIAL_ESCAPES = {'"': '\\"', '\\': '\\\\', '\b': '\\b', '\f': '\\f',
  '\n': '\\n', '\r': '\\r', '\t': '\\t'}

# Replaces all non-ascii characters with character escapes in a string.
def json_escape_string(s):
  result = []
  for c in s:
    escapee = _SPECIAL_ESCAPES.get(c, None)
    if escapee:
      result.append(escapee)
    elif c < ' ':
      result.append("\\u%.4X" % ord(c))
    else:
      result.append(c)
  return "".join(result)

# Encodes a python object as a JSON string.
def encode_json(obj):
  t = type(obj)
  if (t == str) or (t == unicode):
    return "\"%s\"" % json_escape_string(obj)
  elif t == list:
    return "[%s]" % ",".join(map(encode_json, obj))
  elif t == dict:
    entries = ["%s:%s" % (encode_json(k), encode_json(v)) for (k, v) in obj.items()]
    return "{%s}" % ",".join(entries)
  elif (t == int) or (t == float):
    return str(obj)
  elif t == bool:
    if obj:
      return "1"
    else:
      return "0"
  else:
    return encode_json(obj.to_json())


class HttpHandler(BaseHTTPRequestHandler):

  def __init__(self, *args, **kwargs):
    self.proxy = _PROXY
    BaseHTTPRequestHandler.__init__(self, *args, **kwargs)

  def do_GET(self):
    parsed_path = urlparse.urlparse(self.path)
    if parsed_path.path in ["/", ""]:
      self.send_index_page(parsed_path)
    else:
      method = self.proxy.get_handler_map().get(parsed_path.path, None)
      if method is None:
        self.send_response(404)
      else:
        params = urlparse.parse_qs(parsed_path.query)
        self.call_method(method, params)

  # Sends the initial bridge/index page.
  def send_index_page(self, parsed_path):
    params = urlparse.parse_qs(parsed_path.query)
    self.send_response(200)
    self.send_header("Content-Type", "text/html")
    self.end_headers()
    handlers = self.proxy.get_handler_list()
    self.wfile.write(_INDEX_PAGE % {
      "target_origin": params.get("target_origin", ["null"])[0],
      "methods": "{%s}" % ",".join(["%s: true" % k for k in handlers])
    })

  # Invokes a method on the handler and takes care of sending back the
  # result.
  def call_method(self, method, params):
    try:
      value = method(self.proxy, params)
      result = {"value": value}
    except Exception, e:
      result = {"error": str(e)}
    self.send_response(200)
    self.send_header("Content-Type", "application/json")
    self.end_headers()
    self.wfile.write(encode_json(result))

  # Ignore all successful requests.
  def log_request(self, *args):
    pass


class FileHandle(object):

  def __init__(self, name, path, id):
    self.id = id
    self.name = name
    self.path = path

  def to_json(self):
    if os.path.isdir(self.name):
      type = "dir"
    else:
      type = "file"
    return {
        "name": self.name,
        "path": self.path,
        "id": self.id,
        "type": type
    }


class Proxy(object):

  def __init__(self, flags):
    self.flags = flags
    self.httpd = None
    self.handler_map = None
    self.handler_list = None
    self.next_id = 0
    self.handle_map = {}

  # Returns a map from endpoints to the methods that implement the endpoints.
  def get_handler_map(self):
    if self.handler_map is None:
      self.init_data()
    return self.handler_map

  # Returns a list of the methods supported by this handler.
  def get_handler_list(self):
    if self.handler_list is None:
      self.init_data()
    return self.handler_list

  # Initialize the handler map and list.
  def init_data(self):
    self.handler_map = {}
    self.handler_list = [];
    methods = self.__class__.__dict__
    for method in methods:
      if method.startswith("handle_"):
        op = method[7:]
        path = "/agent/%s" % op
        self.handler_map[path] = methods[method]
        self.handler_list.append(op)
    self.handler_list.sort()

  def handle_get_file_list(self, params):
    result = []
    id = int(params.get("handle")[0])
    root = self.handle_map[id].name
    for name in os.listdir(root):
      if name.startswith("."):
        continue
      path = os.path.join(root, name)
      result.append(self.get_file_handle(name, path))
    return result

  def handle_read_file(self, params):
    id = int(params.get("id")[0])
    path = self.handle_map[id].path
    return open(path, "rt").read()

  def handle_start_session(self, params):
    print "Connected to %s." % params["href"][0]
    self.handle_map.clear()
    return self.get_file_handle(self.flags.root, self.flags.root)

  def get_file_handle(self, name, path):
    old_handle = self.handle_map.get(path, None)
    if old_handle:
      return old_handle
    else:
      id = self.next_id
      self.next_id += 1
      new_handle = FileHandle(name, path, id)
      self.handle_map[id] = new_handle
      return new_handle

  def start(self):
    addr = ('', int(self.flags.port))
    self.httpd = HTTPServer(addr, HttpHandler)
    print "Running agent on localhost:%s" % self.flags.port
    self.httpd.serve_forever()


_PROXY = None


def new_option_parser():
  parser = optparse.OptionParser()
  parser.add_option("--port", action="store", default="8040")
  parser.add_option("--root", action="store", default=".")
  return parser

def main():
  parser = new_option_parser()
  (flags, args) = parser.parse_args()
  global _PROXY
  proxy = _PROXY = Proxy(flags)
  try:
    proxy.start()
  except KeyboardInterrupt:
    print "Exiting"

if __name__ == "__main__":
  main()
