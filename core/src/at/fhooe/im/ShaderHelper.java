package at.fhooe.im;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderHelper {
	public static ShaderProgram createMeshShader(String shaderName) {
		ShaderProgram.pedantic = false;
		ShaderProgram shader = null;
		try {
			shader = new ShaderProgram(Util.readFile(Util.getResourceAsStream("shader/"+shaderName+".vertex.glsl")), 
					Util.readFile(Util.getResourceAsStream("shader/"+shaderName+".fragment.glsl")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String log = shader.getLog();
		if (!shader.isCompiled())
			throw new GdxRuntimeException(log);		
		if (log!=null && log.length()!=0)
			System.out.println("Shader Log: "+log);
		return shader;
	}
	
	public static Mesh loadMesh(String meshPath) {
		ObjLoader objLoader = new ObjLoader();
		FileHandle fh = Gdx.files.internal(meshPath);
		if(!fh.exists()) {
			System.err.println("file not found");
		}
		Model model = objLoader.loadModel(fh);
		return model.meshes.first();
	}
}
