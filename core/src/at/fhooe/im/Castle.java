package at.fhooe.im;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Castle {
	private Model castleModel;
	private ShaderProgram shader;
	
	public Castle() {
		ObjLoader objLoader = new ObjLoader();
		FileHandle fh = Gdx.files.internal("castle/castle.obj");
		if(!fh.exists()) {
			System.err.println("castle not found");
		}
		castleModel = objLoader.loadModel(fh);
		
		shader = ShaderHelper.createMeshShader("castle");
	}
	
	public void render(MyCamera cam) {
		shader.begin();
		shader.setUniformMatrix("u_projView", cam.view);
		shader.setUniformMatrix("u_projProj", cam.projection);
		
		for(Mesh castleMesh : castleModel.meshes) {
			castleMesh.render(shader, GL20.GL_TRIANGLES);
		}
		shader.end();
	}
}
