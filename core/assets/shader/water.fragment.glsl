varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
varying float v_time;

uniform vec3 u_eyePos;
uniform samplerCube u_environmentCubemap;
uniform sampler2D u_texture;

uniform vec4 Light_position;
uniform vec3 Light_coneDirection0;
uniform vec3 Light_coneDirection1;

uniform float Light_ambientCoefficient;
uniform vec3 Light_intensities;//vec3(0.937, 0.9098, 0.8431);

uniform float Light_attenuation;
uniform float Light_coneAngle;

uniform vec3 Moon_position;
uniform float Moon_intensity;
uniform mat4 u_projNormal;

float fresnel(vec3 direction, vec3 normal) {
	vec3 nDirection = normalize(direction);
	vec3 nNormal = normalize(normal);

	float cosine = dot(nNormal, nDirection);
	float product = max(cosine, 0.0);
	float factor = pow(1.0 - product, 5.0);

	float fresnelR0;
	factor = fresnelR0 + (1.0 - fresnelR0) * factor;

	return factor;
}

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

	  return vec4(Light_intensities, 1.0)*fFactor/(fDistance*attenuation) * 2.0;
}

void main(){
	vec3 cameraRay = normalize(u_eyePos - v_pos);
	vec3 reflectRay = reflect(cameraRay, v_normal);

	vec3 refractRay = refract(cameraRay, v_normal, 1.33333);
	vec4 refractColor = textureCube(u_environmentCubemap, refractRay);
	vec4 reflectColor = textureCube(u_environmentCubemap, reflectRay);
	reflectColor.a = 1.0;
	refractColor.a = 1.0;

	vec3 Fresnel = vec3( dot(cameraRay, v_normal) );
	float waterDepth = 0.5;

	vec3 fresnelOutput = ( reflectColor.rgb*waterDepth*(1.0-Fresnel) ) + ( refractColor.rgb*(1.0-waterDepth)*Fresnel );
	vec4 ambientColor = vec4(fresnelOutput, 1.0);

	float moonDistance = distance(v_pos, Moon_position);

	vec3 normalMoon = (u_projNormal * vec4(v_normal, 0.0)).xyz;
	vec3 moonLight = normalize(Moon_position - v_pos);
	vec4 diffuse = ambientColor
			* max(dot(normalMoon,moonLight), 0.0)
			* Moon_intensity/(moonDistance*Light_attenuation);

	gl_FragColor = ambientColor * Light_ambientCoefficient
			+ diffuse * 0.5
			+ ambientColor * (SpotLight(Light_coneDirection0) + SpotLight(Light_coneDirection1));
}
