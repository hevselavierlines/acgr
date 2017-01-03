varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
varying float v_time;

uniform vec3 u_eyePos;
uniform samplerCube u_environmentCubemap;
uniform sampler2D u_texture;
//uniforms are constants right now
//uniform float Ka;
//uniform float Kd;
//uniform float Ks;
//uniform float shininess;

//vec3 u_lightPos = vec3(mod(v_time, 80.0) - 40.0, 5.0, 0.0);

const float Ka = 1.0;
const float Kd = 1.5;
const float Ks = 5.0;
const float shininess = 1.0;

const vec3 u_lightPos = vec3(0.0, 8.0, -5.0);
const vec3 u_diffuseColor = vec3(1.0, 1.0, 1.0);
const vec3 u_specularColor = vec3(0.937, 0.9098, 0.8431);

const vec4 Light_position = vec4(0.0, 5.0, -1.0, 1.0);
const vec3 Light_intensities = vec3(0.937, 0.9098, 0.8431);
const float Light_attenuation = 0.1;
const float Light_ambientCoefficient = 1.0;
const float Light_coneAngle = 100.0;
const vec3 Light_coneDirection = vec3(0.0,0.0,-1.0);

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

vec3 ApplyLight(vec3 surfaceColor, vec3 surfaceToCamera) {
	vec3 surfaceToLight;
	float attenuation = 1.0;
	if(Light_position.w == 0.0) {
		//directional light
		surfaceToLight = normalize(Light_position.xyz);
		attenuation = 1.0;
	} else {
		//point light
		surfaceToLight = normalize(Light_position.xyz - v_pos);
		float distanceToLight = length(Light_position.xyz - v_pos);
		attenuation = 1.0 / (1.0 + Light_attenuation * pow(distanceToLight, 2.0));

		//cone restrictions
		float lightToSurfaceAngle = degrees(acos(dot(-surfaceToLight, normalize(Light_coneDirection))));
		if(lightToSurfaceAngle > Light_coneAngle) {
			attenuation = 0.0;
		}
	}
	//ambient
	vec3 ambient = Light_ambientCoefficient * surfaceColor.rgb * Light_intensities;

	//diffuse
	float diffuseCoefficient = max(0.0, dot(v_normal, surfaceToLight));
	vec3 diffuse = diffuseCoefficient * surfaceColor.rgb * Light_intensities;

	//specular
	float specularCoefficient = 0.0;
	if(diffuseCoefficient > 0.0) {
		specularCoefficient = pow(max(0.0, dot(surfaceToCamera, reflect(-surfaceToLight, v_normal))), shininess);
	}
	vec3 specular = specularCoefficient * Ks * Light_intensities;

	//linear color
	return ambient + attenuation * (diffuse + specular);
}

void main(){
	vec3 N = normalize(v_normal);
	vec3 L = normalize(u_lightPos - v_pos);

	float lambertian = max(dot(N, L), 0.0);

	float specular = 0.0;
	if (lambertian > 0.0) {
		vec3 R = reflect(-L, N);
		vec3 V = normalize(-v_pos);

		float specAngle = max(dot(R, V), 0.0);
		specular = pow(specAngle, shininess);
	}
//	vec4 texture = texture2D(u_texture, v_texCoord);
	vec4 lighting = vec4(
		Ka +
		Kd * lambertian * u_diffuseColor +
		Ks * specular * u_specularColor, 1.0);
//	gl_FragColor = lighting * texture;

	vec3 cameraRay = normalize(u_eyePos - v_pos);
	vec3 reflectRay = reflect(cameraRay, v_normal);

	vec3 refractRay = refract(cameraRay, v_normal, 1.0);
	vec4 refractColor = textureCube(u_environmentCubemap, refractRay);
	vec4 reflectColor = textureCube(u_environmentCubemap, reflectRay);
	reflectColor.a = 1.0;
	refractColor.a = 1.0;

	vec3 Fresnel = vec3( dot(cameraRay, v_normal) );
	float waterDepth = 0.56;
	vec3 fresnelOutput = ( reflectColor.rgb*waterDepth*(1.0-Fresnel) ) + ( refractColor.rgb*(1.0-waterDepth)*Fresnel );
	//gl_FragColor = vec4(fresnelOutput, 1.0) * lighting;

	vec3 pointLight = ApplyLight(fresnelOutput, cameraRay);

	gl_FragColor = vec4(pointLight, 1.0);
}
