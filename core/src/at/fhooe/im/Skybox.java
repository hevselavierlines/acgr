package at.fhooe.im;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Skybox {
	private ShaderProgram shader;
	private Mesh mesh;
	private float brightness;
	
	public Skybox() {
		shader = ShaderHelper.createMeshShader("skybox");
		mesh = createCube(50000f);
	}
	
	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		if(brightness <= 1.0f) {
			this.brightness = brightness;
		} else {
			this.brightness = 1.0f;
		}
	}

	private Mesh createCube(float size) {
		Mesh mesh = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));

		float[] cubeVerts = {-size, -size, -size, -size, -size, size, size, -size, size, size, -size, -size, -size, size, -size,
			-size, size, size, size, size, size, size, size, -size, -size, -size, -size, -size, size, -size, size, size, -size,
			size, -size, -size, -size, -size, size, -size, size, size, size, size, size, size, -size, size, -size, -size, -size,
			-size, -size, size, -size, size, size, -size, size, -size, size, -size, -size, size, -size, size, size, size, size,
			size, size, -size,};

		float[] cubeNormals = {0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			-1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,};

		float[] cubeTex = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,};

		float[] vertices = new float[24 * 8];
		int pIdx = 0;
		int nIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeTex[tIdx++];
			vertices[i++] = cubeTex[tIdx++];
		}

		short[] indices = {0, 2, 1, 0, 3, 2, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 15, 14, 12, 14, 13, 16, 17, 18, 16, 18, 19,
			20, 23, 22, 20, 22, 21};

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	public void render(MyCamera cam) {
		shader.begin();
	    Matrix4 worldTrans = new Matrix4();
	    worldTrans.set(cam.view);
	    worldTrans.setTranslation(0, 0, 0);

	    shader.begin();     
	    shader.setUniformMatrix("u_projView", worldTrans);
		shader.setUniformMatrix("u_projProj", cam.projection);
		shader.setUniformf("Light_ambientCoefficient", brightness);
		mesh.render(shader, GL20.GL_TRIANGLES, 0, mesh.getMaxIndices());
		shader.end();
	}
}
