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

float wave(int i, float x, float y) {
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
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
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, y));
    float A = amplitude[i] * direction[i].x * frequency;
    return A * cos(theta * frequency + time * phase);
}

float dWavedy(int i, float x, float y) {
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, y));
    float A = amplitude[i] * direction[i].y * frequency;
    return A * cos(theta * frequency + time * phase);
}

vec3 waveNormal(float x, float y) {
    float dx = 0.0;
    float dy = 0.0;
    for (int i = 0; i < numWaves; ++i) {
        dx += dWavedx(i, x, y);
        dy += dWavedy(i, x, y);
    }
    vec3 n = vec3(-dx, -dy, 1.0);
    return normalize(n);
}

void main() {
	// Output position of the vertex, in clip space : MVP * position
	vec3 vertex = a_position;
	vertex.x = vertex.x * log(abs(vertex.x));
	vertex.z = vertex.z * log(abs(vertex.z));
	v_normal = a_normal;
	v_time = time;
	v_pos = vertex;
	v_texCoord = a_texCoord0;


	vec4 pos = vec4(vertex, 1.0);
	pos.y = waterHeight + waveHeight(pos.x, pos.z);
	v_normal = waveNormal(pos.x, pos.z);
	gl_Position = u_projTrans * pos;
}

