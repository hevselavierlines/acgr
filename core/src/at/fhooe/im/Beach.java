package at.fhooe.im;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Beach {
	private ShaderProgram shader;
	private Mesh mesh;
	private float brightness;
	public Beach(Mesh mesh) {
		this.mesh = mesh;
		shader = ShaderHelper.createMeshShader("beach");
	}
	public float getBrightness() {
		return brightness;
	}
	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}
	
	public void render(MyCamera cam, float[] lightDir0, float[] lightDir1) {
		shader.begin();
		shader.setUniformMatrix("u_projView", cam.view);
		shader.setUniformMatrix("u_projProj", cam.projection);
		Matrix4 normalMatrix = cam.view.cpy();
		normalMatrix.inv().tra();
		shader.setUniformMatrix("u_projNormal", normalMatrix);
		
		shader.setUniformf("Light_ambientCoefficient", brightness);
		shader.setUniform3fv("Light_coneDirection0", lightDir0, 0, 3);
		shader.setUniform3fv("Light_coneDirection1", lightDir1, 0, 3);
		
		shader.setUniform4fv("Light_position", WaterTest.LIGHTHOUSE_POS, 0, WaterTest.LIGHTHOUSE_POS.length);
		shader.setUniform3fv("Light_intensities", WaterTest.LIGHT_COLOR, 0, WaterTest.LIGHT_COLOR.length);
		shader.setUniformf("Light_attenuation", WaterTest.LIGHT_ATTENUATION);
		shader.setUniformf("Light_coneAngle", WaterTest.LIGHT_CONEANGLE);
		
		shader.setUniform3fv("Moon_position", Moon.MOON_POSITION, 0, 3);
		shader.setUniformf("Moon_intensity", Moon.MOON_INTENSITY);
		
		shader.setUniformi("u_texture", 8);
		shader.setUniformi("u_normal", 9);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
	}
	
	
}
