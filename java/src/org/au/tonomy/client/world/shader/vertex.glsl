attribute vec3 aVertex;

uniform mat4 uPerspective;
uniform float uX;
uniform float uY;
uniform vec4 uColor;

varying vec4 vColor;

void main(void) {
  mat4 position = mat4(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 0, 1,
    uX, uY, 0, 1);
  gl_Position = (uPerspective * position) * vec4(aVertex, 1.0);
  vColor = uColor;
}
