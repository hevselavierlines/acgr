//attributes
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

//output variables
varying vec3 v_cubeMapUV;

//uniforms
uniform mat4 u_projProj;
uniform mat4 u_projView;

void main() {
	vec3 vertex = a_position;
	vec4 pos = vec4(vertex, 1.0);
	v_cubeMapUV = normalize(vertex);
	gl_Position = u_projProj * u_projView * pos;
}
