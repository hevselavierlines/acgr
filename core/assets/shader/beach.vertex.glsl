//attributes
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

//uniforms
uniform mat4 u_projProj;
uniform mat4 u_projView;

//output
varying vec3 v_pos;
varying vec2 v_texCoord;
varying vec3 v_normal;

void main() {
	vec3 vertex = a_position;
	v_pos = vertex;
	v_normal = a_normal;
	v_texCoord = a_texCoord0;
	vec4 pos = vec4(vertex, 1.0);
	gl_Position = u_projProj * u_projView * pos;
}
