varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
varying float v_time;

uniform vec3 u_eyePos;
uniform samplerCube u_environmentCubemap;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

uniform vec4 Light_position;
uniform vec3 Light_coneDirection0;
uniform vec3 Light_coneDirection1;

uniform float Light_ambientCoefficient;
uniform vec3 Light_intensities;//vec3(0.937, 0.9098, 0.8431);

const float Light_attenuation = 0.001;

const float Light_coneAngle = 0.9;


const float shininess = 1.0;

const vec3 u_diffuseColor = vec3(1.0, 1.0, 1.0);
const vec3 u_specularColor = vec3(0.937, 0.9098, 0.8431);

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

	  return vec4(Light_intensities, 1.0)*fFactor/(fDistance*attenuation) + Light_ambientCoefficient;
}

vec3 ApplyLight(vec3 surfaceColor, vec3 normal, vec3 surfacePos, vec3 surfaceToCamera) {
    vec3 surfaceToLight;
    float attenuation = 1.0;
    if(Light_position.w == 0.0) {
        //directional light
        surfaceToLight = normalize(Light_position.xyz);
        attenuation = 1.0; //no attenuation for directional lights
    } else {
        //point light
        surfaceToLight = normalize(Light_position.xyz - surfacePos);
        float distanceToLight = length(Light_position.xyz - surfacePos);
        attenuation = 1.0 / (1.0 + Light_attenuation * pow(distanceToLight, 2.0));

        //cone restrictions (affects attenuation)
        float lightToSurfaceAngle = degrees(acos(dot(-surfaceToLight, normalize(Light_coneDirection0))));
        if(lightToSurfaceAngle > Light_coneAngle){
            attenuation -= (lightToSurfaceAngle - Light_coneAngle);
        }
    }

    //ambient
    vec3 ambient = Light_ambientCoefficient * surfaceColor.rgb * Light_intensities;

    //diffuse
    float diffuseCoefficient = max(0.0, dot(normal, surfaceToLight));
    vec3 diffuse = diffuseCoefficient * surfaceColor.rgb * Light_intensities;

    //specular
    float specularCoefficient = 0.0;
    if(diffuseCoefficient > 0.0) {
        specularCoefficient = pow(max(0.0, dot(surfaceToLight, reflect(-surfaceToLight, normal))), shininess);
    }
    vec3 specular = specularCoefficient * u_specularColor * Light_intensities;

    //linear color (color before gamma correction)
    return ambient + attenuation*(diffuse + specular);
}

void main(){
//	vec4 texture = texture2D(u_texture, v_texCoord);
//	gl_FragColor = lighting * texture;

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
	//gl_FragColor = vec4(fresnelOutput, 1.0) * lighting;

//
//	vec3 normal = normalize(gl_NormalMatrix * v_normal);
//
//	vec3 surfacePos = vec3(gl_NormalMatrix * v_pos);
//
//	vec4 surfaceColor = vec4(fresnelOutput, 1.0);
//	vec3 surfaceToCamera = normalize(u_eyePos - surfacePos);
//
//	vec3 linearColor = ApplyLight(surfaceColor.rgb, normal, surfacePos, surfaceToCamera);
//
//	vec3 gamma = vec3(1.0/2.2);
//	vec4 finalColor = vec4(pow(linearColor, gamma), surfaceColor.a);
//	if(abs(v_pos.z - Light_position.z) <= 10.0 || abs(v_pos.x - Light_position.x) <= 10.0) {
//		finalColor = vec4(1.0, 0.0, 0.0, 1.0);
//	}
	gl_FragColor = vec4(fresnelOutput, 1.0) * (SpotLight(Light_coneDirection0) + SpotLight(Light_coneDirection1));

	//vec3 pointLight = ApplyLight(fresnelOutput, cameraRay);

	//gl_FragColor = finalColor;
}
