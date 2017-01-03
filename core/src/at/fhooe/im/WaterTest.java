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
	protected static ShaderProgram createMeshShader() {
		ShaderProgram.pedantic = false;
		ShaderProgram shader = null;
		try {
			shader = new ShaderProgram(Util.readFile(Util.getResourceAsStream("shader/shader.vertex.glsl")), 
					Util.readFile(Util.getResourceAsStream("shader/shader.fragment.glsl")));
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
	Mesh mesh;
	ShaderProgram shader;
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
	Model triangle;
	
	@Override
	public void create () {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		img = new Texture("lostvalley_up.bmp");
		Texture posX = new Texture("lostvalley_east.bmp");
		Texture negX = new Texture("lostvalley_west.bmp");
		Texture posY = new Texture("lostvalley_up.bmp");
		Texture negY = new Texture("lostvalley_down.bmp");
		Texture posZ = new Texture("lostvalley_north.bmp");
		Texture negZ = new Texture("lostvalley_south.bmp");
		
		textureData = new TextureData[6];
		
		textureData[0] = posX.getTextureData();
		textureData[1] = negX.getTextureData();
		textureData[2] = posY.getTextureData();
		textureData[3] = negY.getTextureData();
		textureData[4] = posZ.getTextureData();
		textureData[5] = negZ.getTextureData();
		
		mapCube = new Cubemap(textureData[0], textureData[1], textureData[2], textureData[3], textureData[4], textureData[5]);
		
		ObjLoader objLoader = new ObjLoader();
		FileHandle fh = Gdx.files.internal("plane.obj");
		if(!fh.exists()) {
			System.err.println("file not found");
		}
		plane = objLoader.loadModel(fh);
		mesh = plane.meshes.get(0);
		
		
		shader = createMeshShader();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 2f, -2f);
        cam.lookAt(0,0,0);
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
		img.bind();
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
	        amplitude[i] = (float)(0.2f / (i + 1));

	        wavelength[i] = (float)(2.0 * 3.141592654 / (i + 1));

	        speed[i] = 0.1f*i;
	        
	        double angle = (Math.random() * 2.094395102393195 - 1.047197551196598);
	        
	        direction[dirPointer++] = (float)Math.cos(angle);
	        direction[dirPointer++] = (float)Math.sin(angle);
	    }
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		shader.begin();
		
		shader.setUniformMatrix("u_projTrans", cam.combined);
		eyePos[0] = cam.position.x;
		eyePos[1] = cam.position.y;
		eyePos[2] = cam.position.z;
		
		shader.setUniform3fv("u_eyePos", eyePos, 0, 3);
		shader.setUniformf("time", time);
		
		shader.setUniformf("waterHeight", 0.6f);
		shader.setUniformi("numWaves", numWaves);
		shader.setUniform1fv("amplitude", amplitude, 0, numWaves);
		shader.setUniform1fv("wavelength", wavelength, 0, numWaves);
		shader.setUniform1fv("speed", speed, 0, numWaves);
		shader.setUniform2fv("direction", direction, 0, numWaves * 2);
		time += 0.1f;
	
	    shader.setUniformi("u_texture", 0);
	    
		mesh.render(shader, GL20.GL_TRIANGLES, 0, mesh.getMaxVertices());
		
		//ec.render(cam);
		shader.end();
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
