#!/usr/bin/python

import optparse
import os
import os.path
import re
import sys
import yaml


_BASIC_TYPES = {
  "i32": "int",
  "string": "java.lang.String",
  "any": "java.lang.Object",
  "bool": "boolean"
}


_REFERENCE_TYPES = {
  "i32": "java.lang.Integer",
  "bool": "java.lang.Boolean"
}


_COMPOUND_TYPES = {
  "list": "java.util.List<%s>"
}


_DEFAULT_COMPOUND_VALUES = {
  "list": "java.util.Collections.emptyList()"
}


_DEFAULT_VALUES = {
  "i32": "0",
  "bool": "false"
}


class Configuration(object):

  def __init__(self):
    self.package = None


class FileSystem(object):

  def __init__(self, root):
    self.root = root

  def open_file(self, name):
    full_name = os.path.join(self.root, name)
    dirname = os.path.dirname(full_name)
    if not os.path.exists(dirname):
      os.makedirs(dirname)
    out = open(full_name, "wt")
    return FormatStream(out)

  def open_in_namespace(self, config, path):
    if not config.package is None:
      path = config.package.replace(".", "/") + "/" + path
    print "Generating", path
    return self.open_file(path)


# A utility for emitting text. Allows you to extract substreams that can be
# written to independently and whose results will be inserted into the parent
# at flush time.
class FormatStream(object):

  def __init__(self, out):
    self.out = out
    self.parts = []
    self.substreams = {}

  # Writes the given format string to this stream, expanding any placeholders
  # using the optional params map.
  def add(self, format, params={}):
    def flush_to(cursor, pos):
      if cursor < pos:
        self.parts.append(format[cursor:pos] % params)
    cursor = 0
    for match in re.finditer(r"#\((\w+)\)", format):
      start = match.start()
      flush_to(cursor, start)
      cursor = match.end()
      self.parts.append(self.substream(match.group(1)));
    flush_to(cursor, len(format))
    return self

  # Convenience method. Adds the given text and then inserts a newline.
  def add_line(self, format, params={}):
    return self.add(format, params).add("\n")

  # Emits a comment
  def comment(self, marker, format, params={}):
    for line in format.split("\n"):
      self.add(marker + " " + line, params).add("\n")
    return self

  # Returns the substream with the given name.
  def substream(self, name):
    if not name in self.substreams:
      self.substreams[name] = FormatStream(None)
    return self.substreams[name]

  # Creates and returns a new anonymous substream at the current position.
  def new_substream(self):
    result = FormatStream(None)
    self.parts.append(result)
    return result

  # Flushes this stream to a string and returns it.
  def flush(self):
    def flush(part):
      if type(part) is FormatStream:
        return part.flush()
      else:
        return part
    return "".join([flush(p) for p in self.parts])

  # Flushes this stream to the underlying file.
  def flush_to_file(self):
    result = self.flush()
    self.out.write(result)
    self.out.close()


# Abstract supertype for the different declaration types.
class Declaration(object):

  def configure(self, config):
    pass

  def declare(self, namespace):
    pass

  def attach(self, namespace):
    pass

  def emit(self, fs, config):
    pass


class Settings(Declaration):

  def __init__(self, data):
    self.data = data["settings"]

  def configure(self, config):
    if "package" in self.data:
      config.package = self.data["package"]


# A service declaration
class Service(Declaration):

  CLASS_TEMPLATE = """\
public class %(Name)s {

  public interface IServer {
    #(server_body)
  }

  public interface IClient {
    #(client_body)
  }

  #(messages)

  public static IClient newEncoder(final org.au.tonomy.shared.util.IFunction<org.au.tonomy.shared.plankton.RemoteMessage, ? extends org.au.tonomy.shared.util.Promise<?>> sender) {
    return new IClient() {
      #(encoder_body)
    };
  }

  public static org.au.tonomy.shared.util.Promise<?> dispatch(org.au.tonomy.shared.plankton.RemoteMessage message, IServer server) {
    java.lang.String method = message.getMethod();
    #(dispatch_body)
    return null;
  }
  
}
"""

  SERVER_COMMAND_TEMPLATE = """\
public org.au.tonomy.shared.util.Promise<? extends %(ReferenceType)s> %(lowerCamelName)s(%(UpperCamelName)sParameters params);
"""

  CLIENT_COMMAND_TEMPLATE = """\
public org.au.tonomy.shared.util.Promise<%(ReferenceType)s> %(lowerCamelName)s(%(UpperCamelName)sParameters params);
"""

  ENCODER_TEMPLATE = """\
public org.au.tonomy.shared.util.Promise<%(ReferenceType)s> %(lowerCamelName)s(%(UpperCamelName)sParameters params) {
  org.au.tonomy.shared.plankton.RemoteMessage message = org.au.tonomy.shared.plankton.RemoteMessage
      .newBuilder()
      .setMethod(\"%(name)s\")
      .setParameters(params)
      .build();
  org.au.tonomy.shared.util.Promise<?> result = sender.call(message);
  return result.then(new org.au.tonomy.shared.util.IFunction<Object, %(ReferenceType)s>() {
    @Override
    public %(ReferenceType)s call(Object arg) {
      %(ReferenceType)s result;
      #(convert)
      return result;
    }
  });
}
"""

  DISPATCH_TEMPLATE = """\
if (\"%(name)s\".equals(method)) {
  %(UpperCamelName)sParameters params = %(UpperCamelName)sParameters.parse(message.getParameters());
  return server.%(lowerCamelName)s(params);
}
"""

  def __init__(self, data):
    self.data = data
    self.commands = []

  def name(self):
    return pton_to_java_type(self.data["service"])

  def declare(self, namespace):
    namespace[self.name()] = self

  def attach_command(self, command):
    self.commands.append(command)

  def emit_class(self, config, out):
    out.comment("//", "This file is auto generated by planktonc.")
    if not config.package is None:
      out.add_line("package %(package)s;", {"package": config.package})
    out.comment("//", to_lines(self.data))
    fragments = {
      "Name": self.name(),
    }
    out.add_line(Service.CLASS_TEMPLATE, fragments)
    for command in self.commands:
      out_type = command.data["out"]
      name = command.name()
      upper_camel_name = to_upper_camel(name)
      fragments = {
        "name": name,
        "lowerCamelName": to_lower_camel(name),
        "UpperCamelName": upper_camel_name,
        "ReferenceType": pton_to_reference_type(out_type)
      }
      server_body_out = out.substream("server_body")
      server_body_out.comment("//", to_lines(command.data))
      server_body_out.add(Service.SERVER_COMMAND_TEMPLATE, fragments)
      out.substream("client_body").add(Service.CLIENT_COMMAND_TEMPLATE, fragments)
      encoder_body = out.substream("encoder_body").new_substream()
      encoder_body.add(Service.ENCODER_TEMPLATE, fragments)
      convert = encoder_body.substream("convert")
      convert.add(Type.CONVERT_TEMPLATES[get_conversion_type(out_type)], {
        "in": "arg",
        "out": "result",
        "Target": pton_to_reference_type(out_type),
        "ElementType": compound_element_type(out_type)

      })
      out.substream("dispatch_body").add(Service.DISPATCH_TEMPLATE, fragments)
      params_data = {
        "type": "%sParameters" % upper_camel_name,
        "fields": command.data["in"],
        "modifiers": ["public", "static"]
      }
      Type(params_data).emit_class(out.substream("messages").new_substream())

  def emit(self, fs, config):
    name = self.name()
    out = fs.open_in_namespace(config, name + ".java")
    # Write the source code.
    self.emit_class(config, out)
    out.flush_to_file()


# A command declaration.
class Command(Declaration):

  def __init__(self, data):
    self.data = data

  def target(self):
    return self.data["target"]

  def name(self):
    return self.data["command"]

  def attach(self, namespace):
    namespace[self.target()].attach_command(self)

  def emit_command(self, out):
    out.add("public void ", self.name(), "() {").newline()
    out.add("}").newline()


# Given a plankton type, returns the corresponding java type.
def pton_to_java_type(name):
  if type(name) is str:
    return _BASIC_TYPES.get(name, name)
  else:
    return compound_pton_to_java_type(name)

# Returns a java reference type corresponding to the given type. The only
# difference between this and pton_to_java_type is that this will never
# return primitive types.
def pton_to_reference_type(name):
  if type(name) is str:
    return _REFERENCE_TYPES.get(name, pton_to_java_type(name))
  else:
    return compound_pton_to_java_type(name)

# Converts a compound type (list or may say) to a java type. There is no
# explicity conversion to a reference type because the result will always
# be a reference type.
def compound_pton_to_java_type(name):
  (container, element) = name.items()[0]
  return _COMPOUND_TYPES[container] % pton_to_reference_type(element)

# Returns the element type of the given type if it is a compound, otherwise
# the empty string.
def compound_element_type(t):
  if is_compound_type(t):
    (container, element) = t.items()[0]
    return pton_to_reference_type(element)
  else:
    return ""

def is_compound_type(t):
  return not type(t) is str


def compound_container_type(t):
  if is_compound_type(t):
    (container, element) = t.items()[0]
    return container
  else:
    return ""

# Returns the kind of conversion necessary to convert from the given
# type to java.
def get_conversion_type(name):
  if not type(name) is str:
    (container, _) = name.items()[0]
    return container
  elif (name in _REFERENCE_TYPES) or (name in _BASIC_TYPES):
    return "cast"
  else:
    return "parse"

# Returns the default value for the given type.
def pton_to_default_value(name):
  if is_compound_type(name):
    (container, element) = name.items()[0]
    return _DEFAULT_COMPOUND_VALUES[container]
  else:
    return _DEFAULT_VALUES.get(name, "null")

def to_lower_camel(name):
  upper = to_upper_camel(name)
  return upper[0].lower() + upper[1:]

# Returns the given name in upper camel case
def to_upper_camel(name):
  def title(str):
    if len(str) == 0:
      return str
    else:
      return str[0].upper() + str[1:].lower()
  return "".join(map(title, name.split("_")))

def to_lines(data):
  if type(data) == dict:
    return "{\n" + ",\n".join(["  %s: %s" % (str(k), str(v)) for (k, v) in data.items()]) + "\n}"
  else:
    return str(data)


# A type declaration.
class Type(Declaration):

  CLASS_TEMPLATE = """\
%(modifiers)s class %(Name)s implements %(interfaces)s {
  
  #(class_fields)
  
  public static class Builder {
    #(builder_fields)
    public %(Name)s build() {
      return new %(Name)s(#(field_list));
    }
  }
  
  public static Builder newBuilder() {
    return new Builder();
  }

  private %(Name)s(#(constructor_params)) {
    #(constructor_body)
  }

  @SuppressWarnings("cast")
  public static %(Name)s parse(Object data) throws org.au.tonomy.shared.plankton.ParseError {
    if (!(data instanceof java.util.Map<?, ?>)) {
      throw new org.au.tonomy.shared.plankton.ParseError();
    }
    java.util.Map<?, ?> map = (java.util.Map<?, ?>) data;
    #(parse_body)
    return new %(Name)s(#(field_list));
  }

  @Override
  public Object toPlanktonData(org.au.tonomy.shared.plankton.IPlanktonFactory factory) {
    org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonMap map = factory.newMap();
    #(plankton_data)
    return map;
  }

  @Override
  public %(Name)s toPlankton() {
    return this;
  }

}
"""

  CLASS_FIELD_TEMPLATE = """\
private final %(JavaType)s %(lowerCamelName)s;
private final boolean has%(UpperCamelName)s;
public %(JavaType)s get%(UpperCamelName)s() {
  return this.%(lowerCamelName)s;
}
public boolean has%(UpperCamelName)s() {
  return this.has%(UpperCamelName)s;
}
"""

  BUILDER_FIELD_TEMPLATE = """\
private %(JavaType)s %(lowerCamelName)s;
private boolean has%(UpperCamelName)s = false;
public %(JavaType)s get%(UpperCamelName)s() {
  return this.%(lowerCamelName)s;
}
public Builder set%(UpperCamelName)s(%(JavaType)s %(lowerCamelName)s) {
  this.%(lowerCamelName)s = %(lowerCamelName)s;
  this.has%(UpperCamelName)s = true;
  return this;
}
"""

  CONSTRUCTOR_LINE_TEMPLATE = """\
this.%(lowerCamelName)s = %(lowerCamelName)s;\n
this.has%(UpperCamelName)s = has%(UpperCamelName)s;\n
"""

  CONSTRUCTOR_PARAM_TEMPLATE = "boolean has%(UpperCamelName)s, %(JavaType)s %(lowerCamelName)s"

  FIELD_LIST_TEMPLATE = "has%(UpperCamelName)s, %(lowerCamelName)s"

  PLANKTON_DATA_TEMPLATE = """\
if (this.has%(UpperCamelName)s)
  map.set(\"%(name)s\", this.%(lowerCamelName)s);
"""

  CONVERT_TEMPLATES = {
    "cast": """\
if (!(%(in)s instanceof %(Target)s)) {
  throw new org.au.tonomy.shared.plankton.ParseError();
}
%(out)s = (%(Target)s) %(in)s;
""",
    "parse": """\
%(out)s = %(Target)s.parse(%(in)s);
""",
    "list": """\
%(out)s = org.au.tonomy.shared.util.Factory.newArrayList();
for (Object elm : (java.util.List<?>) %(in)s)
  %(out)s.add(%(ElementType)s.parse(elm));
"""
  }

  PARSE_TEMPLATES = {
    "cast": """\
%(JavaType)s %(lowerCamelName)s;
boolean has%(UpperCamelName)s = map.containsKey(\"%(name)s\");
if (has%(UpperCamelName)s) {
  Object obj = map.get(\"%(name)s\");
  if (!(obj instanceof %(ReferenceType)s)) {
    throw new org.au.tonomy.shared.plankton.ParseError().addPathSegment("%(name)s");
  }
  %(lowerCamelName)s = (%(ReferenceType)s) obj;
} else {
  %(lowerCamelName)s = %(default_value)s;
}
""",
    "parse": """\
%(JavaType)s %(lowerCamelName)s;
boolean has%(UpperCamelName)s = map.containsKey(\"%(name)s\");
if (has%(UpperCamelName)s) {
  try {
    %(lowerCamelName)s = %(ReferenceType)s.parse(map.get(\"%(name)s\"));
  } catch (org.au.tonomy.shared.plankton.ParseError pe) {
    pe.addPathSegment(\"%(name)s\");
    throw pe;
  }
} else {
  %(lowerCamelName)s = %(default_value)s;
}
""",
    "list": """\
%(JavaType)s %(lowerCamelName)s;
boolean has%(UpperCamelName)s = map.containsKey(\"%(name)s\");
if (has%(UpperCamelName)s) {
  Object obj = map.get(\"%(name)s\");
  if (!(obj instanceof java.util.List<?>)) {
    throw new org.au.tonomy.shared.plankton.ParseError().addPathSegment("%(name)s");
  }
  %(lowerCamelName)s = org.au.tonomy.shared.util.Factory.newArrayList();
  try {
    for (Object elm : (java.util.List<?>) obj)
      %(lowerCamelName)s.add(%(ElementType)s.parse(elm));
  } catch (org.au.tonomy.shared.plankton.ParseError pe) {
    pe.addPathSegment(\"%(name)s\");
    throw pe;
  }
} else {
  %(lowerCamelName)s = %(default_value)s;
}
"""
  }

  COMPOUND_BUILDER_FIELD_TEMPLATE = {
    "list": """
public Builder addTo%(UpperCamelName)s(%(ElementType)s elm) {
  if (!has%(UpperCamelName)s) {
    %(lowerCamelName)s = org.au.tonomy.shared.util.Factory.newArrayList();
    has%(UpperCamelName)s = true;
  }
  %(lowerCamelName)s.add(elm);
  return this;
}
"""
  }

  def __init__(self, data):
    self.data = data

  # The extra interfaces implemented by generated types.
  def extra_interfaces(self):
    return [
      "org.au.tonomy.shared.plankton.IPlanktonObject",
      "org.au.tonomy.shared.plankton.IPlanktonable<%s>" % self.name()
    ]

  def emit_class(self, out):
    out.comment("//", to_lines(self.data))
    interfaces = self.implements() + self.extra_interfaces()
    fragments = {
      "modifiers": " ".join(self.data.get("modifiers", ["public"])),
      "Name": self.name(),
      "interfaces": ", ".join(interfaces)
    }
    out.add_line(Type.CLASS_TEMPLATE, fragments)
    index = 0
    for (name, type) in self.fields():
      fragments = {
        "name": name,
        "type": type,
        "lowerCamelName": to_lower_camel(name),
        "UpperCamelName": to_upper_camel(name),
        "JavaType": pton_to_java_type(type),
        "ReferenceType": pton_to_reference_type(type),
        "default_value": pton_to_default_value(type),
        "ElementType": compound_element_type(type)
      }
      class_fields_out = out.substream("class_fields")
      class_fields_out.comment("//", "%(name)s: %(type)s", fragments)
      class_fields_out.add(Type.CLASS_FIELD_TEMPLATE, fragments)
      out.substream("builder_fields").add(Type.BUILDER_FIELD_TEMPLATE, fragments)
      if is_compound_type(type):
        container = compound_container_type(type)
        out.substream("builder_fields").add(Type.COMPOUND_BUILDER_FIELD_TEMPLATE[container], fragments)        
      out.substream("constructor_body").add(Type.CONSTRUCTOR_LINE_TEMPLATE, fragments)
      constructor_params_out = out.substream("constructor_params")
      field_list_out = out.substream("field_list")
      if index > 0:
        constructor_params_out.add(", ")
        field_list_out.add(", ")
      constructor_params_out.add(Type.CONSTRUCTOR_PARAM_TEMPLATE, fragments)
      field_list_out.add(Type.FIELD_LIST_TEMPLATE, fragments)
      out.substream("plankton_data").add(Type.PLANKTON_DATA_TEMPLATE, fragments)
      self.emit_field_parse(name, type, out.substream("parse_body"), fragments)
      index += 1

  def emit_field_parse(self, name, type, out, fragments):
    out.add(Type.PARSE_TEMPLATES[get_conversion_type(type)], fragments)

  def emit(self, fs, config):
    name = self.name()
    out = fs.open_in_namespace(config, name + ".java")
    # Write the source code.
    out.comment("//", "This file is auto generated by planktonc.")
    if not config.package is None:
      out.add_line("package %(package)s;", {"package": config.package})
    self.emit_class(out)
    out.flush_to_file()

  def name(self):
    return pton_to_java_type(self.data['type'])

  def fields(self):
    items = self.data["fields"].items()
    return sorted(items)

  def implements(self):
    return self.data.get("implements", [])

# Mapping from tagnames to the ast classes that implement those clauses.
_TAGS = {
  "service": Service,
  "command": Command,
  "type": Type,
  "settings": Settings,
}

# Process the declarations from one inout file.
def process_decls(flags, decls):
  namespace = {}
  config = Configuration()
  # Allow declarations to influence the config.
  for decl in decls:
    decl.configure(config)
  # Install any toplevel declarations in the namespace.
  for decl in decls:
    decl.declare(namespace)
  # Attach subdeclarations to their host declarations.
  for decl in decls:
    decl.attach(namespace)
  # Print the declarations.
  fs = FileSystem(flags.out)
  for decl in decls:
    decl.emit(fs, config)

# Process _all_ the input files.
def process_files(flags, names):
  for name in names:
    decls = []
    data = yaml.load(open(name, "rt"))
    if data is None:
      continue
    for decl in data:
      tags = []
      for prop in decl.keys():
        tag = _TAGS.get(prop)
        if not tag is None:
          tags.append(tag)
      if len(tags) != 1:
        raise AssertionError("Invalid clause " + str(decl))
      decls.append(tags[0](decl))
    process_decls(flags, decls)

# Build the command-line parser.
def build_parser():
  parser = optparse.OptionParser()
  parser.add_option("--out")
  return parser

def main():
  parser = build_parser()
  (flags, args) = parser.parse_args()
  process_files(flags, args)

if __name__ == "__main__":
  main()
