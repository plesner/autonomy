attribute vec3 vertex;
uniform mat4 perspective;
uniform mat4 position;

void main(void) {
  gl_Position = perspective * position * vec4(vertex, 1.0);
}
