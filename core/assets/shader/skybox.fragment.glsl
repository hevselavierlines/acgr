uniform samplerCube u_environmentCubemap;
uniform float Light_ambientCoefficient;
//input from vertex shader
varying vec3 v_cubeMapUV;
void main() {
	gl_FragColor = textureCube(u_environmentCubemap, v_cubeMapUV) * Light_ambientCoefficient;
}
