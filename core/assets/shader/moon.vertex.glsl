//attributes
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_projView;
uniform mat4 u_projProj;
uniform vec3 Moon_position;
uniform float Moon_size;

varying vec3 v_position;
varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {
	vec3 vertex = a_position;
	vertex.x *= Moon_size;
	vertex.y *= Moon_size;
	vertex.z *= Moon_size;

	vertex += Moon_position;

	v_position = vertex;
	v_normal = a_normal;
	v_texCoord = a_texCoord0;

	gl_Position = u_projProj * u_projView * vec4(vertex, 1.0);
}
