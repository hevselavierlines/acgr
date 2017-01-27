varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;

uniform sampler2D u_texture;
uniform float Light_ambientCoefficient;

uniform vec3 Moon_position;
uniform float Moon_intensity;
uniform mat4 u_projNormal;

uniform float Light_attenuation;

void main() {
	float moonDistance = distance(v_pos, Moon_position);
	vec4 ambientColor = texture2D(u_texture, v_texCoord);
	vec3 normalMoon = v_normal;
	vec3 moonLight = normalize(Moon_position - v_pos);
	vec4 diffuse = ambientColor
				* max(dot(normalMoon,moonLight), 0.0)
				* Moon_intensity/(moonDistance*Light_attenuation);

	gl_FragColor = diffuse * 0.5;
}
