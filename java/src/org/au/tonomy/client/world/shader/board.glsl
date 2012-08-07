// Shader used to draw the board.

// The coordinate offset.
uniform vec3 uPosition;

void main(void) {
  vec3 position = aVertexPosition + uPosition;
  gl_Position = uPerspective * vec4(position, 1.0);
  vColor = uColor;
}
