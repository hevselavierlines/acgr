//variables from the lighthouse light.
uniform vec4 Light_position;
uniform vec3 Light_coneDirection0;
uniform vec3 Light_coneDirection1;

//actual constants from the light house.
uniform float Light_ambientCoefficient;
uniform vec3 Light_intensities;//vec3(0.937, 0.9098, 0.8431);
uniform float Light_attenuation;
uniform float Light_coneAngle;

//input from the vertex shader.
varying vec3 v_pos;
varying vec2 v_texCoord;
uniform sampler2D u_texture;

vec4 SpotLight(vec3 _coneDirection) {
	//vec4 GetSpotLightColor(const SpotLight spotLight, vec3 vWorldPos)
	  float fDistance = distance(v_pos, Light_position.xyz);

	  vec3 vDir = v_pos - Light_position.xyz;
	  vDir = normalize(vDir);

	  float fCosine = dot(_coneDirection, vDir);
	  float fConeCosine = cos(Light_coneAngle);
	  float fDif = 1.0-fConeCosine;
	  float fFactor = clamp((fCosine-fConeCosine)/fDif, 0.0, 1.0);
	  float attenuation = Light_attenuation;
	  if(fConeCosine > fCosine) {
		  attenuation = 180.0;
	  }

	  return vec4(Light_intensities, 1.0)*fFactor/(fDistance*attenuation) + Light_ambientCoefficient;
}

void main() {
	gl_FragColor = texture2D(u_texture, v_texCoord) * (SpotLight(Light_coneDirection0) + SpotLight(Light_coneDirection1));
}
