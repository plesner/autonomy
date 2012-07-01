attribute vec3 vertex;
uniform mat4 perspective;
uniform float x;
uniform float y;
uniform float scaleX;
uniform float scaleY;

void main(void) {
  mat4 position = mat4(
    scaleX, 0, 0, 0,
    0, scaleY, 0, 0,
    0, 0, 0, 1,
    x, y, 0, 1);
  gl_Position = (perspective * position) * vec4(vertex, 1.0);
}
