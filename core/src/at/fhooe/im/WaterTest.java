package at.fhooe.im;

import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;


public class WaterTest extends ApplicationAdapter {
	protected static ShaderProgram createMeshShader(String shaderName) {
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
	SpriteBatch batch;
	Texture img;
	Model plane;
	Model cube;
	Mesh planeMesh;
	Mesh cubeMesh;
	Mesh[] lighthouseMeshes;
	Model lightHouse;
	ShaderProgram waterShader;
	ShaderProgram lighthouseShader;
	private PerspectiveCamera cam;
	private CameraInputController camController;
	private float time = 0.0f;
	float[] amplitude;
	float[] wavelength;
	float[] speed;
	float[] direction;
	float[] eyePos = new float[3];
	TextureData[] textureData;
	int numWaves = 3;
	Cubemap mapCube;
	
	@Override
	public void create () {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		Texture posX = new Texture("water/lostvalley_east.bmp");
		Texture negX = new Texture("water/lostvalley_west.bmp");
		Texture posY = new Texture("water/lostvalley_up.bmp");
		Texture negY = new Texture("water/lostvalley_down.bmp");
		Texture posZ = new Texture("water/lostvalley_north.bmp");
		Texture negZ = new Texture("water/lostvalley_south.bmp");
		
		textureData = new TextureData[6];
		
		textureData[0] = posX.getTextureData();
		textureData[1] = negX.getTextureData();
		textureData[2] = posY.getTextureData();
		textureData[3] = negY.getTextureData();
		textureData[4] = posZ.getTextureData();
		textureData[5] = negZ.getTextureData();
		
		mapCube = new Cubemap(textureData[0], textureData[1], textureData[2], textureData[3], textureData[4], textureData[5]);
		
		ObjLoader objLoader = new ObjLoader();
		FileHandle fh = Gdx.files.internal("water/bigplane.obj");
		if(!fh.exists()) {
			System.err.println("file not found");
		}
		plane = objLoader.loadModel(fh);
		planeMesh = plane.meshes.first();
		
		fh = Gdx.files.internal("lighthouse/lighthouse.obj");
		if(!fh.exists()) {
			System.err.println("lighthouse.obj not found");
		}
		lightHouse = objLoader.loadModel(fh);
		
		lighthouseMeshes = new Mesh[3];
		
		fh = Gdx.files.internal("lighthouse/roof.obj");
		if(!fh.exists()) {
			System.err.println("roof does not exist");
		}
		lighthouseMeshes[0] = objLoader.loadModel(fh).meshes.first();
		
		fh = Gdx.files.internal("lighthouse/tower.obj");
		if(!fh.exists()) {
			System.err.println("tower does not exist!");
		}
		lighthouseMeshes[1] = objLoader.loadModel(fh).meshes.first();
		
		fh = Gdx.files.internal("lighthouse/sticks.obj");
		if(!fh.exists()) {
			System.err.println("tower does not exist!");
		}
		lighthouseMeshes[2] = objLoader.loadModel(fh).meshes.first();
		
		
		
		fh = Gdx.files.internal("cube.obj");
		if(!fh.exists()) {
			System.err.println("cube not found");
		}
		cube = objLoader.loadModel(fh);
		cubeMesh = cube.meshes.first();
		
		waterShader = createMeshShader("water");
		lighthouseShader = createMeshShader("lighthouse");
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 5f, 25f);
        cam.lookAt(0,0,0f);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
        generateValues(numWaves);
        
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
		
		textureData[0].prepare();
		textureData[1].prepare();
		textureData[2].prepare();
		textureData[3].prepare();
		textureData[4].prepare();
		textureData[5].prepare();
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, textureData[0].getWidth(), textureData[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[0].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, textureData[1].getWidth(), textureData[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[1].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, textureData[2].getWidth(), textureData[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[2].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, textureData[3].getWidth(), textureData[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[3].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, textureData[4].getWidth(), textureData[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[4].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, textureData[5].getWidth(), textureData[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, textureData[5].consumePixmap().getPixels());
	
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER,GL20.GL_LINEAR_MIPMAP_LINEAR );     
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER,GL20.GL_LINEAR );
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE );
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE );   

	    Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
	    
	    
	    Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
	    img = new Texture("lighthouse/roof.png");
		img.bind();
		
	
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		Texture textureTower = new Texture("lighthouse/wall.jpg");
		textureTower.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE2);
		Texture textureStick = new Texture("lighthouse/wall.jpg");
		textureStick.bind();
	}
	
	public void randomGenerator() {
		 amplitude = randomEight(0.0, 0.2, 8);
	     wavelength = randomEight(0.1, 5.0, 8);
	     speed = randomEight(0.0, 1.0, 8);
	     direction = randomEight(0.0, 1.0, 16);
	}
	
	public void generateValues(int numWaves) {
		amplitude = new float[numWaves];
		wavelength = new float[numWaves];
		speed = new float[numWaves];
		int dirPointer = 0;
		direction = new float[numWaves * 2];
		for (int i = 0; i < numWaves; i++) {
	        amplitude[i] = (float)(0.4f / (i + 1));

	        wavelength[i] = (float)(10.0 * 3.141592654 / (i + 1));

	        speed[i] = 1.0f*i;
	        
	        double angle = (Math.random() * 2.094395102393195 - 1.047197551196598);
	        
	        direction[dirPointer++] = (float)Math.cos(angle);
	        direction[dirPointer++] = (float)Math.sin(angle);
	    }
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		waterShader.begin();
		
		waterShader.setUniformMatrix("u_projTrans", cam.combined);
		eyePos[0] = cam.position.x;
		eyePos[1] = cam.position.y;
		eyePos[2] = cam.position.z;
		
		waterShader.setUniform3fv("u_eyePos", eyePos, 0, 3);
		waterShader.setUniformf("time", time);
		
		waterShader.setUniformf("waterHeight", 0.6f);
		waterShader.setUniformi("numWaves", numWaves);
		waterShader.setUniform1fv("amplitude", amplitude, 0, numWaves);
		waterShader.setUniform1fv("wavelength", wavelength, 0, numWaves);
		waterShader.setUniform1fv("speed", speed, 0, numWaves);
		waterShader.setUniform2fv("direction", direction, 0, numWaves * 2);
		time += 0.1f;
	
	    planeMesh.render(waterShader, GL20.GL_TRIANGLES, 0, planeMesh.getMaxIndices());
	    waterShader.end();
	    
	    lighthouseShader.begin();
	    lighthouseShader.setUniformMatrix("u_projTrans", cam.combined);

		//Mesh currMesh = lightHouse.meshes.get(0);
		
		
		//currMesh.render(lighthouseShader, GL20.GL_TRIANGLES, 0, currMesh.getMaxIndices());
		int currTexture = 0;
		for(Mesh currMesh : lighthouseMeshes) {
			lighthouseShader.setUniformi("u_texture", currTexture++);
			currMesh.render(lighthouseShader, GL20.GL_TRIANGLES, 0, currMesh.getMaxIndices());
		}
		
		//ec.render(cam);
		lighthouseShader.end();
	}
	
	public float[] randomEight(double min, double max, int numbers) {
		float[] ret = new float[numbers];
		for(int i = 0; i < numbers; i++) {
			ret[i] = (float)(Math.random() * (max - min) + min);
		}
		return ret;
	}
	
	@Override
	public void dispose () {
		plane.dispose();
		img.dispose();
	}
}
