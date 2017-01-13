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
import com.badlogic.gdx.math.Vector3;
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
	Mesh lightSphere;
	Model lightHouse;
	ShaderProgram waterShader;
	ShaderProgram lighthouseShader;
	ShaderProgram lightsphereShader;
	private MyCamera cam;
	private CameraInputController camController;
	private float time = 0.0f;
	float[] amplitude;
	float[] wavelength;
	float[] speed;
	float[] direction;
	float[] eyePos = new float[3];
	float[] lightDir0 = new float[3];
	float[] lightDir1 = new float[3];
	
	float lighthouseAngle0 = 0.0f;
	float lighthouseAngle1 = 180.0f;
	float brightness = 0.25f;
	TextureData[] textureData;
	int numWaves = 3;
	final float[] lightColor = new float[] {1.0f, 0.8f, 0.5f};
	final float[] lighthousePos = new float[] {10.0f, 50.0f, 60.0f, 1.0f};
	private float cameraAngle = 0.0f;
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
		
//		fh = Gdx.files.internal("johnny.obj");
//		if(!fh.exists()) {
//			System.err.println("jonas mayr!");
//		}
//		cube = objLoader.loadModel(fh);
		
		fh = Gdx.files.internal("lighthouse/lightsphere.obj");
		if(!fh.exists()) {
			System.err.println("lightsphere not found!");
		}
		lightSphere = objLoader.loadModel(fh).meshes.first();
		
		waterShader = createMeshShader("water");
		lighthouseShader = createMeshShader("lighthouse");
		lightsphereShader = createMeshShader("lightsphere");
		
		cam = new MyCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 50f, 200f);
        cam.lookAt(0f, 0f, 100f);
        cam.near = 1f;
        cam.far = 900f;
        cam.update();
        
        //camController = new CameraInputController(cam);
        //Gdx.input.setInputProcessor(camController);
        
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
	    img = new Texture("lighthouse/wall.jpg");
		img.bind();
		
	
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		Texture textureTower = new Texture("lighthouse/wall.jpg");
		textureTower.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE2);
		Texture textureStick = new Texture("lighthouse/wall.jpg");
		textureStick.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE3);
		Texture texture3 = new Texture("lighthouse/roof.png");
		texture3.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE4);
		Texture texture4 = new Texture("lighthouse/wall.jpg");
		texture4.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE5);
		Texture texture5 = new Texture("lighthouse/soil.jpg");
		texture5.bind();
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE6);
		Texture texture6 = new Texture("lighthouse/wall.jpg");
		texture6.bind();
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
		if(time <= 100.0f) {
			cam.position.set(new Vector3(-50.0f, 50.0f, time * 2.0f));
			cam.lookAt(lighthousePos[0], 0.0f, lighthousePos[2]);
			cam.setUpY();
			cam.update();
		}
		if(time > 100.0f) {
			cam.position.set((float)(Math.sin(cameraAngle) * 100.0f) + lighthousePos[0], 50.0f, (float)(Math.cos(cameraAngle) * 100.0f) + lighthousePos[2]);
			cam.lookAt(lighthousePos[0], 0.0f, lighthousePos[2]);
			cam.setUpY();
			cam.update();
			cameraAngle += 0.005f;
			if(cameraAngle >= 6.283185307179586f) {
				cameraAngle = 0;
			}
		}
		waterShader.setUniformMatrix("u_projTrans", cam.combined);
		
		
		
		eyePos[0] = cam.position.x;
		eyePos[1] = cam.position.y;
		eyePos[2] = cam.position.z;
		waterShader.setUniform3fv("u_eyePos", eyePos, 0, 3);
		
		lightDir0[0] = (float) Math.sin(Math.toRadians(lighthouseAngle0));
		lightDir0[1] = 0.0f;
		lightDir0[2] = (float) Math.cos(Math.toRadians(lighthouseAngle0));
		
		lightDir1[0] = (float) Math.sin(Math.toRadians(lighthouseAngle0 - 180.0f));
		lightDir1[1] = 0.0f;
		lightDir1[2] = (float) Math.cos(Math.toRadians(lighthouseAngle0 - 180.0f));
		lighthouseAngle0 += 0.5f;
		waterShader.setUniform3fv("Light_coneDirection0", lightDir0, 0, 3);
		waterShader.setUniform3fv("Light_coneDirection1", lightDir1, 0, 3);
		waterShader.setUniformf("Light_ambientCoefficient", brightness);
		waterShader.setUniformf("time", time);
		
		waterShader.setUniformf("waterHeight", 0.6f);
		waterShader.setUniformi("numWaves", numWaves);
		waterShader.setUniform1fv("amplitude", amplitude, 0, numWaves);
		waterShader.setUniform1fv("wavelength", wavelength, 0, numWaves);
		waterShader.setUniform1fv("speed", speed, 0, numWaves);
		waterShader.setUniform2fv("direction", direction, 0, numWaves * 2);
		waterShader.setUniform4fv("Light_position", lighthousePos, 0, lighthousePos.length);
		waterShader.setUniform3fv("Light_intensities", lightColor, 0, lightColor.length);
		
		time += 0.1f;
	
	    planeMesh.render(waterShader, GL20.GL_TRIANGLES, 0, planeMesh.getMaxIndices());
	    waterShader.end();
	    
	    lighthouseShader.begin();
	    lighthouseShader.setUniform4fv("Light_position", lighthousePos, 0, lighthousePos.length);
	    lighthouseShader.setUniformf("Light_ambientCoefficient", brightness);
	    lighthouseShader.setUniformMatrix("u_projTrans", cam.combined);
		int currTexture = 0;
		for(Mesh currMesh : lightHouse.meshes) {
			lighthouseShader.setUniformi("u_texture", currTexture++);
			currMesh.render(lighthouseShader, GL20.GL_TRIANGLES, 0, currMesh.getMaxIndices());
		}
		lighthouseShader.end();
		
		lightsphereShader.begin();
		lightsphereShader.setUniform4fv("Light_position", lighthousePos, 0, lighthousePos.length);
	    lightsphereShader.setUniformMatrix("u_projTrans", cam.combined);
	    lightsphereShader.setUniform3fv("Light_intensities", lightColor, 0, lightColor.length);
	    lightSphere.render(lightsphereShader, GL20.GL_TRIANGLES, 0, lightSphere.getMaxIndices());
		lightsphereShader.end();
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
		lightHouse.dispose();
	}
}
