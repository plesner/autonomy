// Common declarations used both in the board and unit shaders.

// The position of the current vertex.
attribute vec3 aVertexPosition;

// The perspective matrix.
uniform mat4 uPerspective;

// The vertex color input.
uniform vec4 uColor;

// The vertex color output that will be passed to the fragment shader.
varying vec4 vColor;
