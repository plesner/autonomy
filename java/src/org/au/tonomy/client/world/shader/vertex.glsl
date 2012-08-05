// The position of the current vertex.
attribute vec3 aVertexPosition;

// The perspective matrix.
uniform mat4 uPerspective;

// The start coordinates.
uniform vec3 uSourcePosition;

// The end coordinates.
uniform vec3 uTargetPosition;

// How far between start and end are we?
uniform float uProgress;

// The vertex color input.
uniform vec4 uColor;

// The vertex color output that will be passed to the fragment shader.
varying vec4 vColor;

void main(void) {
  // Calculate the appropriate position based on how much progress
  // we've made between the source and target positions.
  vec3 translation = (uSourcePosition * (1.0 - uProgress)) + (uTargetPosition * uProgress);
  // Move the vertex using the calculated translation.
  vec3 position = aVertexPosition + translation;
  // Finally apply the perspective.
  gl_Position = uPerspective * vec4(position, 1.0);
  // Set the color directly.
  vColor = uColor;
}
