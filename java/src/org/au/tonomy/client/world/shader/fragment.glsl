precision mediump float;

// The color value set in the vertex shader.
varying vec4 vColor;

void main(void) {
  gl_FragColor = vColor;
}
