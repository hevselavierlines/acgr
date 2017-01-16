varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
uniform vec3 Light_intensities;
uniform sampler2D u_texture;
const float exposure = 0.3;
const float decay = 0.5;
const float density = 0.6;
const float weight = 1.0;
const vec2 lightPositionOnScreen = vec2(1.0, 0.0);
const int NUM_SAMPLES = 10;

void main() {
//	vec3 coordinates;
//	coordinates.x = gl_FragCoord.x / gl_FragCoord.w;
//	coordinates.y = gl_FragCoord.y / gl_FragCoord.w;
//	coordinates.z = gl_FragCoord.z / gl_FragCoord.w;
//
//	vec4 finalColor = vec4(Light_intensities, 0.4);
//	gl_FragColor = finalColor;
//
	vec2 deltaTextCoord = lightPositionOnScreen.xy;//vec2( gl_TexCoord[0].st - lightPositionOnScreen.xy );
	vec2 textCoo = v_texCoord.st;
	deltaTextCoord *= 1.0 /  float(NUM_SAMPLES) * density;
	float illuminationDecay = 1.0;


	for(int i=0; i < NUM_SAMPLES ; i++)
	{
			 textCoo -= deltaTextCoord;
			 vec4 sample = texture2D(u_texture, v_texCoord);

			 sample *= illuminationDecay * weight;

			 gl_FragColor += sample;

			 illuminationDecay *= decay;
	 }
	 gl_FragColor *= exposure;
}
