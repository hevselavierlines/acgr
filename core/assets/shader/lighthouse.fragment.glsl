varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;

uniform sampler2D u_texture;
uniform float Light_ambientCoefficient;

void main() {
	gl_FragColor = texture2D(u_texture, v_texCoord) * Light_ambientCoefficient;
}
