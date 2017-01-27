package at.fhooe.im;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Moon {
	private ShaderProgram shader;
	private Mesh mesh;
	public static final float[] MOON_POSITION = new float[] {-15000.0f, 15000.0f, -15000.0f};
	public final float MOON_SIZE = 200.0f;
	public static final float MOON_INTENSITY = 10.0f;
	public Moon() {
		mesh = ShaderHelper.loadMesh("moon/moon.obj");
		shader = ShaderHelper.createMeshShader("moon");
	}
	
	public void render(MyCamera cam) {
		shader.begin();
		shader.setUniformMatrix("u_projView", cam.view);
		shader.setUniformMatrix("u_projProj", cam.projection);
		
		shader.setUniform3fv("Moon_position", MOON_POSITION, 0, 3);
		shader.setUniformf("Moon_size", MOON_SIZE);
		shader.setUniformi("u_texture", 10);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
	}
}
