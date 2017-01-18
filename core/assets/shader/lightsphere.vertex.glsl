//attributes
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

//output variables
varying vec3 v_pos;
varying vec3 v_normal;
varying vec2 v_texCoord;

//uniforms
uniform mat4 u_projProj;
uniform mat4 u_projView;
uniform vec4 Light_position;

void main() {
	// Output position of the vertex, in clip space : MVP * position
	vec4 vertex = vec4(a_position, 1.0);
	//vertex.x *= 10.0;
	//vertex.y *= 10.0;
	//vertex.x /= 30.0;
	//vertex.y /= 30.0;
	//vertex.z /= 30.0;

	//vertex.x += Light_position.x;
	//vertex.z += Light_position.z;

	v_texCoord = a_texCoord0;
	gl_Position = u_projProj * vertex;
}
