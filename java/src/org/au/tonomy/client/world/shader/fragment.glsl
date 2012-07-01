precision mediump float;
uniform vec4 colors[2];
uniform int colorSelector;

void main(void) {
  if (colorSelector == 0) {
    gl_FragColor = colors[0];
  } else {  
    gl_FragColor = colors[1];
  }
}
