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
varying vec3 v_normal;
uniform sampler2D u_texture;
uniform sampler2D u_normal;

uniform vec3 Moon_position;
uniform float Moon_intensity;
uniform mat4 u_projNormal;

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

float SpotLightIntensity(vec3 _coneDirection) {
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

	  return fFactor/(fDistance*attenuation);
}

void main() {
	vec4 normalMap = texture2D(u_normal, v_texCoord);
	vec3 normal = normalMap.rgb * 2.0 - 1.0;
	normal += v_normal;

	vec4 tex = texture2D(u_texture, v_texCoord);

	vec3 N = normalize(normal.xyz);
	vec3 lightHouseRay = normalize(v_pos - Light_position.xyz);
	float moonDistance = distance(v_pos, Moon_position);
	vec3 moonLight = normalize(Moon_position.xyz - v_pos);

	vec4 defuseMoon = tex
			* max(dot(N,moonLight), 0.0)
			* Moon_intensity/(moonDistance*Light_attenuation);

	float cone1 = SpotLightIntensity(Light_coneDirection0);
	float cone2 = SpotLightIntensity(Light_coneDirection1);

	vec3 defuseLightHouse = vec3(0.0,0.0,0.0);

	if(cone1 + cone2 > 0.0) {
		defuseLightHouse = Light_intensities * max(dot(N, lightHouseRay), 0.0) * (cone1 + cone2);
	}
	gl_FragColor = defuseMoon * 0.5 + vec4(defuseLightHouse, 1.0)
			+ tex * Light_ambientCoefficient;
}
