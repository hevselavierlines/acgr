varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
uniform vec3 Light_intensities;
uniform sampler2D u_texture;
uniform mat4 u_projProj;
uniform mat4 u_projView;
const float exposure = 0.3;
const float decay = 0.5;
const float density = 0.6;
const float weight = 1.0;
const vec2 lightPositionOnScreen = vec2(0.0, 0.0);
uniform vec4 Light_position;
const int NUM_SAMPLES = 10;

void main() {
//	vec3 coordinates;
//	coordinates.x = gl_FragCoord.x / gl_FragCoord.w;
//	coordinates.y = gl_FragCoord.y / gl_FragCoord.w;
//	coordinates.z = gl_FragCoord.z / gl_FragCoord.w;
//
	vec4 lightPos = u_projProj * u_projView * Light_position;
	vec4 finalColor;
	if(lightPos.y < 1.0 && lightPos.y > -1.0) {
		finalColor = vec4(1.0, 0.0, 0.0, 1.0);
	} else {
		vec2 alpha = 1.0 - abs(v_texCoord);
		alpha.y += 0.4;
		float putTogether = (alpha.x + alpha.y) / 4.0;
		finalColor = vec4(Light_intensities, putTogether);
	}
	gl_FragColor = finalColor;

//	vec2 deltaTextCoord = lightPositionOnScreen.xy;//vec2( gl_TexCoord[0].st - lightPositionOnScreen.xy );
//	vec2 textCoo = v_texCoord.st;
//	deltaTextCoord *= 1.0 /  float(NUM_SAMPLES) * density;
//	float illuminationDecay = 1.0;
//
//
//	for(int i=0; i < NUM_SAMPLES ; i++)
//	{
//			 textCoo -= deltaTextCoord;
//			 vec4 sample = texture2D(u_texture, abs(v_texCoord));
//
//			 sample *= illuminationDecay * weight;
//
//			 gl_FragColor += sample;
//
//			 illuminationDecay *= decay;
//	 }
//	 gl_FragColor *= exposure;

//	int Samples = 128;
//	float Intensity = 0.125, Decay = 0.96875;
//	vec2 TexCoord = gl_TexCoord[0].st, Direction = vec2(0.5) - TexCoord;
//	Direction /= float(Samples) * 1.0);
//	vec3 Color = texture2D(u_texture, TexCoord).rgb;
//
//	for(int Sample = 0; Sample < Samples; Sample++)
//	{
//		Color += texture2D(u_texture, TexCoord).rgb * Intensity;
//		Intensity *= Decay;
//		TexCoord += Direction;
//	}
//
//	gl_FragColor = vec4(Color, 1.0);
}
