varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
uniform vec3 Light_intensities;

void main() {
	vec3 coordinates;
	coordinates.x = gl_FragCoord.x / gl_FragCoord.w;
	coordinates.y = gl_FragCoord.y / gl_FragCoord.w;
	coordinates.z = gl_FragCoord.z / gl_FragCoord.w;
	gl_FragColor = vec4(Light_intensities, 1.0);
}
