//attributes
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

//output variables
varying vec2 v_texCoord;
varying vec3 v_pos;
varying vec3 v_normal;
varying float v_time;

//uniforms
uniform mat4 u_projTrans;
uniform float time;
const float pi = 3.14159;
uniform float waterHeight;
uniform int numWaves;
uniform float amplitude[8];
uniform float wavelength[8];
uniform float speed[8];
uniform vec2 direction[8];

float calcPhase(int i, float frequency) {
	return speed[i] * frequency;
}

float calcTheta(int i, float x, float y) {
	return dot(direction[i], vec2(x, y));
}

float calcFrequency(int i) {
	return 2.0 * pi / wavelength[i];
}

float wave(int i, float x, float y) {
    float frequency = calcFrequency(i);
    float phase = calcPhase(i, frequency);
    float theta = dot(direction[i], vec2(x, y));
    return amplitude[i] * sin(theta * frequency + time * phase);
}

float waveHeight(float x, float y) {
    float height = 0.0;
    for (int i = 0; i < numWaves; ++i)
        height += wave(i, x, y);
    return height;
}

float dWavedx(int i, float x, float y) {
    float frequency = calcFrequency(i);
    float phase = calcPhase(i, frequency);
    float theta = calcTheta(i, x, y);
    return (amplitude[i] * direction[i].x * frequency) *
    		cos(theta * frequency + time * phase);
}

float dWavedy(int i, float x, float y) {
    float frequency = calcFrequency(i);
    float phase = calcPhase(i, frequency);
    float theta = calcTheta(i, x, y);
    return (amplitude[i] * direction[i].y * frequency) *
    		cos(theta * frequency + time * phase);
}

vec3 waveNormal(float x, float y) {
    float dx = 0.0;
    float dy = 0.0;
    for (int i = 0; i < numWaves; i++) {
        dx += dWavedx(i, x, y);
        dy += dWavedy(i, x, y);
    }
    vec3 n = vec3(-dx, 1.0, -dy);
    return normalize(n);
}

void main() {
	vec3 vertex = a_position;
	vertex.x = vertex.x * log(abs(vertex.x));
	vertex.z = vertex.z * log(abs(vertex.z));
	if(vertex.z >= -600.0) {
		vertex.z -= waveHeight(vertex.x, vertex.z) * 50.0;
	}
	v_time = time;
	v_texCoord = a_texCoord0;

	vec4 pos = vec4(vertex, 1.0);
	pos.y = waterHeight + waveHeight(pos.x, pos.z);
	v_normal = waveNormal(pos.x, pos.z);
	v_pos = pos.xyz;
	gl_Position = u_projTrans * pos;
}

