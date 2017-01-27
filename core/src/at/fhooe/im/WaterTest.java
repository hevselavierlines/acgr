package at.fhooe.im;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;


public class WaterTest extends ApplicationAdapter implements InputProcessor {
	private Model plane;
	private Mesh meshPlane;
	private Mesh meshBeach;
	private Model lightHouse;
	private Mesh screenRect;
	private ShaderProgram waterShader;
	private ShaderProgram lighthouseShader;
	private ShaderProgram lightsphereShader;
	private MyCamera cam;
	private float waterTime = 0.0f;
	private float cameraTime = 0.0f;
	private float[] amplitude;
	private float[] wavelength;
	private float[] speed;
	private float[] direction;
	private float[] eyePos = new float[3];
	private float[] lightDir0 = new float[3];
	private float[] lightDir1 = new float[3];
	
	private float lighthouseAngle0 = 0.0f;
	private float brightness = 0.25f;
	private TextureData[] textureData;
	private int numWaves = 3;
	
	private Skybox skybox;
	private Beach beach;
	private boolean automaticRotation = true;
	private final float WATER_SPEED = 1.0f;
	private final float WATER_AMPLITUDE = 0.6f;
	private final float WATER_WAVELENGTH = 30.0f;
	private Castle castle;
	private Moon moon;
	
	public final static float[] LIGHT_COLOR = new float[] {0.94f, 0.856f, 0.934f};
	public final static float[] LIGHTHOUSE_POS = new float[] {10.0f, 1061.84f, -1000.0f, 1.0f};
	public final static float LIGHT_ATTENUATION = 0.0004f;
	public final static float LIGHT_CONEANGLE = 0.7f;
	
	private Mesh createScreenRect(float size) {
		float vertices[] = new float[]{
				-size, -size, -1.0f,
				0.0f, 0.0f, -1.0f,
				-1.0f, -1.0f,
				
				-size, size, -1.0f,
				0.0f, 0.0f, -1.0f,
				-1.0f, 1.0f,
				
				size, -size, -1.0f,
				0.0f, 0.0f, -1.0f,
				1.0f, -1.0f,
				
				size, size, -1.0f,
				0.0f, 0.0f, -1.0f,
				1.0f, 1.0f
		};
		short indices[] = new short[] {
				0, 1, 2,
				1, 3, 2
		};
		Mesh ret = new Mesh(true, vertices.length, indices.length, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
		ret.setVertices(vertices);
		ret.setIndices(indices);
		return ret;
	}
	
	@Override
	public void create () {
		ObjLoader objLoader = new ObjLoader();
		FileHandle fh = Gdx.files.internal("water/plane.obj");
		if(!fh.exists()) {
			System.err.println("file not found");
		}
		plane = objLoader.loadModel(fh);
		meshPlane = plane.meshes.first();
		
		fh = Gdx.files.internal("lighthouse/lighthouse.obj");
		if(!fh.exists()) {
			System.err.println("lighthouse.obj not found");
		}
		lightHouse = objLoader.loadModel(fh);
		
		fh = Gdx.files.internal("water/beach.obj");
		if(!fh.exists()) {
			System.err.println("beach not found");
		}
		meshBeach = objLoader.loadModel(fh).meshes.first();
		
		waterShader = ShaderHelper.createMeshShader("water");
		lighthouseShader = ShaderHelper.createMeshShader("lighthouse");
		lightsphereShader = ShaderHelper.createMeshShader("lightsphere");
		
		cam = new MyCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 50f, 200f);
        cam.lookAt(0f, 0f, 100f);
        cam.near = 1f;
        cam.far = 100000f;
        cam.setLightHousePos(LIGHTHOUSE_POS);
        cam.update();
        
        Gdx.input.setInputProcessor(this);
        generateValues(numWaves);
        
	    loadEnvironmentalCube();
	    loadTextures();
		
		screenRect = createScreenRect(0.3f);
		
		skybox = new Skybox();
		skybox.setBrightness(brightness);
		
		beach = new Beach(meshBeach);
		beach.setBrightness(brightness);
		
		castle = new Castle();
		moon = new Moon();
	}
	
	private void loadEnvironmentalCube() {
		Texture posX = new Texture("water/sky_right.png");
		Texture negX = new Texture("water/sky_left.png");
		Texture posY = new Texture("water/sky_up.png");
		Texture negY = new Texture("water/sky_down.png");
		Texture posZ = new Texture("water/sky_back.png");
		Texture negZ = new Texture("water/sky_front.png");
		
		textureData = new TextureData[6];
		
		textureData[0] = posX.getTextureData();
		textureData[1] = negX.getTextureData();
		textureData[2] = posY.getTextureData();
		textureData[3] = negY.getTextureData();
		textureData[4] = posZ.getTextureData();
		textureData[5] = negZ.getTextureData();
        
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
		
		textureData[0].prepare();
		textureData[1].prepare();
		textureData[2].prepare();
		textureData[3].prepare();
		textureData[4].prepare();
		textureData[5].prepare();
		
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, textureData[0].getWidth(), textureData[0].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[0].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, textureData[1].getWidth(), textureData[1].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[1].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, textureData[2].getWidth(), textureData[2].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[2].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, textureData[3].getWidth(), textureData[3].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[3].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, textureData[4].getWidth(), textureData[4].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[4].consumePixmap().getPixels());
	    Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, textureData[5].getWidth(), textureData[5].getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, textureData[5].consumePixmap().getPixels());
	
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER,GL20.GL_LINEAR_MIPMAP_NEAREST );     
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER,GL20.GL_LINEAR );
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE );
	    Gdx.gl20.glTexParameteri ( GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE );   

	    Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
	}
	
	private void loadTextures() {
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
	    Texture textureWall = new Texture("lighthouse/wall.jpg");
		textureWall.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE0);
	
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		Texture textureTower = new Texture("lighthouse/wall.jpg");
		textureTower.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE1);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE2);
		Texture textureStick = new Texture("lighthouse/wall.jpg");
		textureStick.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE2);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE3);
		Texture texture3 = new Texture("lighthouse/roof.png");
		texture3.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE3);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE4);
		Texture texture4 = new Texture("lighthouse/wall.jpg");
		texture4.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE4);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE5);
		Texture texture5 = new Texture("lighthouse/soil.jpg");
		texture5.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE5);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE6);
		Texture texture6 = new Texture("lighthouse/wall.jpg");
		texture6.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE6);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE7);
		Texture texture7 = new Texture("halo.bmp");
		texture7.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE7);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE8);
		Texture texture8 = new Texture("water/sandtexture.jpg");
		texture8.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE8);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE9);
		Texture texture9 = new Texture("water/sandnormal.jpg");
		texture9.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE9);
		
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE10);
		Texture texture10 = new Texture("moon/moon.jpg");
		texture10.bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE10);
	}
	
	public void generateValues(int numWaves) {
		amplitude = new float[numWaves];
		wavelength = new float[numWaves];
		speed = new float[numWaves];
		int dirPointer = 0;
		direction = new float[numWaves * 2];
		for (int i = 0; i < numWaves; i++) {
	        amplitude[i] = (float)(WATER_AMPLITUDE / (i + 1));

	        wavelength[i] = (float)(WATER_WAVELENGTH * 3.141592654 / (i + 1));

	        speed[i] = WATER_SPEED*i;
	        
	        double angle = (Math.random() * 2.094395102393195 - 1.047197551196598);
	        
	        direction[dirPointer++] = (float)Math.cos(angle);
	        direction[dirPointer++] = (float)Math.sin(angle);
	    }
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		
		//this calculates the next position for the camera motion
		if(automaticRotation) {
			cam.calculateNextCameraPosition();
			cam.update();
		}
		
		waterShader.begin();
		waterShader.setUniformMatrix("u_projTrans", cam.combined);
		
		eyePos[0] = cam.position.x;
		eyePos[1] = cam.position.y;
		eyePos[2] = cam.position.z;
		/*mat4 normalMatrix = transpose(inverse(modelView));*/
		//Matrix4 normalMatrix = cam.view.inv().tra();
		Matrix4 normalMatrix = cam.view.cpy();
		normalMatrix.inv().tra();
		waterShader.setUniformMatrix("u_projNormal", normalMatrix);
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
		
		waterShader.setUniform4fv("Light_position", LIGHTHOUSE_POS, 0, LIGHTHOUSE_POS.length);
		waterShader.setUniform3fv("Light_intensities", LIGHT_COLOR, 0, LIGHT_COLOR.length);
		waterShader.setUniformf("Light_attenuation", LIGHT_ATTENUATION);
		waterShader.setUniformf("Light_coneAngle", LIGHT_CONEANGLE);
		
		waterShader.setUniformf("time", waterTime);
		
		waterShader.setUniformf("waterHeight", 0.6f);
		waterShader.setUniformi("numWaves", numWaves);
		waterShader.setUniform1fv("amplitude", amplitude, 0, numWaves);
		waterShader.setUniform1fv("wavelength", wavelength, 0, numWaves);
		waterShader.setUniform1fv("speed", speed, 0, numWaves);
		waterShader.setUniform2fv("direction", direction, 0, numWaves * 2);
		
		waterShader.setUniform3fv("Moon_position", Moon.MOON_POSITION, 0, 3);
		waterShader.setUniformf("Moon_intensity", Moon.MOON_INTENSITY);
		
		waterTime += 0.1f;
		
	    meshPlane.render(waterShader, GL20.GL_TRIANGLES, 0, meshPlane.getMaxIndices());
	    waterShader.end();
	    
	    beach.render(cam, lightDir0, lightDir1);
	    castle.render(cam);
	    
	    lighthouseShader.begin();
	    lighthouseShader.setUniform4fv("Light_position", LIGHTHOUSE_POS, 0, LIGHTHOUSE_POS.length);
	    lighthouseShader.setUniformf("Light_ambientCoefficient", brightness);
	    lighthouseShader.setUniformMatrix("u_projTrans", cam.combined);
	    lighthouseShader.setUniformMatrix("u_projNormal", normalMatrix);
	    lighthouseShader.setUniform3fv("Moon_position", Moon.MOON_POSITION, 0, 3);
	    lighthouseShader.setUniformf("Moon_intensity", Moon.MOON_INTENSITY);
	    lighthouseShader.setUniformf("Light_attenuation", LIGHT_ATTENUATION);
		int currTexture = 0;
		for(Mesh currMesh : lightHouse.meshes) {
			lighthouseShader.setUniformi("u_texture", currTexture++);
			currMesh.render(lighthouseShader, GL20.GL_TRIANGLES, 0, currMesh.getMaxIndices());
		}
		lighthouseShader.end();
		skybox.render(cam);
		moon.render(cam);
//		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		lightsphereShader.begin();
//		lightsphereShader.setUniform4fv("Light_position", LIGHTHOUSE_POS, 0, LIGHTHOUSE_POS.length);
//		lightsphereShader.setUniformMatrix("u_projProj", cam.projection);
//	    lightsphereShader.setUniformMatrix("u_projView", cam.view);
//	    lightsphereShader.setUniform3fv("Light_intensities", LIGHT_COLOR, 0, LIGHT_COLOR.length);
//	    lightsphereShader.setUniformi("u_texture", 7);
//	    
//	    screenRect.render(lightsphereShader, GL20.GL_TRIANGLES);
//		lightsphereShader.end();
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
		lightHouse.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		switch(character) {
		case 63236:
			brightness -= 0.05f;
			if(brightness < 0.0f) {
				brightness = 0.0f;
			}
			skybox.setBrightness(brightness);
			beach.setBrightness(brightness);
			break;
		case 63237:
			brightness += 0.05f;
			if(brightness > 1.0f) {
				brightness = 1.0f;
			}
			skybox.setBrightness(brightness);
			beach.setBrightness(brightness);
			break;
		case 32:
			automaticRotation = !automaticRotation;
			break;
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
