package at.fhooe.im;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public final class MyCamera {
	private float[] lightHousePos;
	
	private static final Vector3[] CASTLE_CORNERS = new Vector3[] {
			new Vector3(-1201.188f, 1700.0f, 3470.757f),
			new Vector3(1120.705f, 1700.0f, 3470.757f),
			new Vector3(1120.705f, 1700.0f, 2617.947f),
			new Vector3(-1201.188f, 1700.0f, 2617.947f)
	};

	private static final int START_WATER = 2400; 
	private Vector3 lastPosition;
	/** the field of view of the height, in degrees **/
	public float fieldOfView = 67;
	private int time = START_WATER;
	/** the position of the camera **/
	public final Vector3 position = new Vector3();
	/** the unit length direction vector of the camera **/
	public final Vector3 direction = new Vector3(0, 0, -1);
	/** the unit length up vector of the camera **/
	public final Vector3 up = new Vector3(0, 1, 0);

	/** the projection matrix **/
	public final Matrix4 projection = new Matrix4();
	/** the view matrix **/
	public final Matrix4 view = new Matrix4();
	/** the combined projection and view matrix **/
	public final Matrix4 combined = new Matrix4();
	/** the inverse combined projection and view matrix **/
	public final Matrix4 invProjectionView = new Matrix4();

	/** the near clipping plane distance, has to be positive **/
	public float near = 1;
	/** the far clipping plane distance, has to be positive **/
	public float far = 100;

	/** the viewport width **/
	public float viewportWidth = 0;
	/** the viewport height **/
	public float viewportHeight = 0;

	/** the frustum **/
	public final Frustum frustum = new Frustum();

	private final Vector3 tmpVec = new Vector3();
	private final Ray ray = new Ray(new Vector3(), new Vector3());

	/** Constructs a new {@link PerspectiveCamera} with the given field of view and viewport size. The aspect ratio is derived from
	 * the viewport size.
	 * 
	 * @param fieldOfViewY the field of view of the height, in degrees, the field of view for the width will be calculated
	 *           according to the aspect ratio.
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height */
	public MyCamera (float fieldOfViewY, float viewportWidth, float viewportHeight) {
		this.fieldOfView = fieldOfViewY;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}

	final Vector3 tmp = new Vector3();

	public void update () {
		update(true);
	}
	

	public float[] getLightHousePos() {
		return lightHousePos;
	}



	public void setLightHousePos(float[] lightHousePos) {
		this.lightHousePos = lightHousePos;
	}



	public void update (boolean updateFrustum) {
		float aspect = viewportWidth / viewportHeight;
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}

	/** Recalculates the direction of the camera to look at the point (x, y, z). This function assumes the up vector is normalized.
	 * @param x the x-coordinate of the point to look at
	 * @param y the y-coordinate of the point to look at
	 * @param z the z-coordinate of the point to look at */
	public void lookAt (float x, float y, float z) {
		tmpVec.set(x, y, z).sub(position).nor();
		if (!tmpVec.isZero()) {
			float dot = tmpVec.dot(up); // up and direction must ALWAYS be orthonormal vectors
			if (Math.abs(dot - 1) < 0.000000001f) {
				// Collinear
				up.set(direction).scl(-1);
			} else if (Math.abs(dot + 1) < 0.000000001f) {
				// Collinear opposite
				up.set(direction);
			}
			direction.set(tmpVec);
			normalizeUp();
		}
	}

	/** Recalculates the direction of the camera to look at the point (x, y, z).
	 * @param target the point to look at */
	public void lookAt (Vector3 target) {
		lookAt(target.x, target.y, target.z);
	}
	
	public void setUpY() {
		up.set(0.0f, 1.0f, 0.0f);
	}

	/** Normalizes the up vector by first calculating the right vector via a cross product between direction and up, and then
	 * recalculating the up vector via a cross product between right and direction. */
	public void normalizeUp () {
		tmpVec.set(direction).crs(up).nor();
		up.set(tmpVec).crs(direction).nor();
	}

	/** Rotates the direction and up vector of this camera by the given angle around the given axis. The direction and up vector
	 * will not be orthogonalized.
	 * 
	 * @param angle the angle
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis */
	public void rotate (float angle, float axisX, float axisY, float axisZ) {
		direction.rotate(angle, axisX, axisY, axisZ);
		up.rotate(angle, axisX, axisY, axisZ);
	}

	/** Rotates the direction and up vector of this camera by the given angle around the given axis. The direction and up vector
	 * will not be orthogonalized.
	 * 
	 * @param axis the axis to rotate around
	 * @param angle the angle */
	public void rotate (Vector3 axis, float angle) {
		direction.rotate(axis, angle);
		up.rotate(axis, angle);
	}

	/** Rotates the direction and up vector of this camera by the given rotation matrix. The direction and up vector will not be
	 * orthogonalized.
	 * 
	 * @param transform The rotation matrix */
	public void rotate (final Matrix4 transform) {
		direction.rot(transform);
		up.rot(transform);
	}

	/** Rotates the direction and up vector of this camera by the given {@link Quaternion}. The direction and up vector will not be
	 * orthogonalized.
	 * 
	 * @param quat The quaternion */
	public void rotate (final Quaternion quat) {
		quat.transform(direction);
		quat.transform(up);
	}

	/** Rotates the direction and up vector of this camera by the given angle around the given axis, with the axis attached to given
	 * point. The direction and up vector will not be orthogonalized.
	 * 
	 * @param point the point to attach the axis to
	 * @param axis the axis to rotate around
	 * @param angle the angle */
	public void rotateAround (Vector3 point, Vector3 axis, float angle) {
		tmpVec.set(point);
		tmpVec.sub(position);
		translate(tmpVec);
		rotate(axis, angle);
		tmpVec.rotate(axis, angle);
		translate(-tmpVec.x, -tmpVec.y, -tmpVec.z);
	}

	/** Transform the position, direction and up vector by the given matrix
	 * 
	 * @param transform The transform matrix */
	public void transform (final Matrix4 transform) {
		position.mul(transform);
		rotate(transform);
	}

	/** Moves the camera by the given amount on each axis.
	 * @param x the displacement on the x-axis
	 * @param y the displacement on the y-axis
	 * @param z the displacement on the z-axis */
	public void translate (float x, float y, float z) {
		position.add(x, y, z);
	}

	/** Moves the camera by the given vector.
	 * @param vec the displacement vector */
	public void translate (Vector3 vec) {
		position.add(vec);
	}

	/** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject, but does not
	 * rely on OpenGL. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left corner, y
	 * pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0 will return a
	 * point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to specify the
	 * viewport position and dimensions in the coordinate system expected by {@link GL20#glViewport(int, int, int, int)}, with the
	 * origin in the bottom left corner of the screen.
	 * @param screenCoords the point in screen coordinates (origin top left)
	 * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportWidth the width of the viewport in pixels
	 * @param viewportHeight the height of the viewport in pixels
	 * @return the mutated and unprojected screenCoords {@link Vector3} */
	public Vector3 unproject (Vector3 screenCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		float x = screenCoords.x, y = screenCoords.y;
		x = x - viewportX;
		y = Gdx.graphics.getHeight() - y - 1;
		y = y - viewportY;
		screenCoords.x = (2 * x) / viewportWidth - 1;
		screenCoords.y = (2 * y) / viewportHeight - 1;
		screenCoords.z = 2 * screenCoords.z - 1;
		screenCoords.prj(invProjectionView);
		return screenCoords;
	}

	/** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject but does not
	 * rely on OpenGL. The viewport is assumed to span the whole screen and is fetched from {@link Graphics#getWidth()} and
	 * {@link Graphics#getHeight()}. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left
	 * corner, y pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0
	 * will return a point on the near plane, a z-coordinate of 1 will return a point on the far plane.
	 * @param screenCoords the point in screen coordinates
	 * @return the mutated and unprojected screenCoords {@link Vector3} */
	public Vector3 unproject (Vector3 screenCoords) {
		unproject(screenCoords, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		return screenCoords;
	}

	/** Projects the {@link Vector3} given in world space to screen coordinates. It's the same as GLU gluProject with one small
	 * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
	 * <b>bottom</b> left, with the y-axis pointing <b>upwards</b> and the x-axis pointing to the right. This makes it easily
	 * useable in conjunction with {@link Batch} and similar classes.
	 * @return the mutated and projected worldCoords {@link Vector3} */
	public Vector3 project (Vector3 worldCoords) {
		project(worldCoords, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		return worldCoords;
	}

	/** Projects the {@link Vector3} given in world space to screen coordinates. It's the same as GLU gluProject with one small
	 * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
	 * <b>bottom</b> left, with the y-axis pointing <b>upwards</b> and the x-axis pointing to the right. This makes it easily
	 * useable in conjunction with {@link Batch} and similar classes. This method allows you to specify the viewport position and
	 * dimensions in the coordinate system expected by {@link GL20#glViewport(int, int, int, int)}, with the origin in the bottom
	 * left corner of the screen.
	 * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportWidth the width of the viewport in pixels
	 * @param viewportHeight the height of the viewport in pixels
	 * @return the mutated and projected worldCoords {@link Vector3} */
	public Vector3 project (Vector3 worldCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		worldCoords.prj(combined);
		worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2 + viewportX;
		worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2 + viewportY;
		worldCoords.z = (worldCoords.z + 1) / 2;
		return worldCoords;
	}

	/** Creates a picking {@link Ray} from the coordinates given in screen coordinates. It is assumed that the viewport spans the
	 * whole screen. The screen coordinates origin is assumed to be in the top left corner, its y-axis pointing down, the x-axis
	 * pointing to the right. The returned instance is not a new instance but an internal member only accessible via this function.
	 * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	 * @param viewportWidth the width of the viewport in pixels
	 * @param viewportHeight the height of the viewport in pixels
	 * @return the picking Ray. */
	public Ray getPickRay (float screenX, float screenY, float viewportX, float viewportY, float viewportWidth,
		float viewportHeight) {
		unproject(ray.origin.set(screenX, screenY, 0), viewportX, viewportY, viewportWidth, viewportHeight);
		unproject(ray.direction.set(screenX, screenY, 1), viewportX, viewportY, viewportWidth, viewportHeight);
		ray.direction.sub(ray.origin).nor();
		return ray;
	}

	/** Creates a picking {@link Ray} from the coordinates given in screen coordinates. It is assumed that the viewport spans the
	 * whole screen. The screen coordinates origin is assumed to be in the top left corner, its y-axis pointing down, the x-axis
	 * pointing to the right. The returned instance is not a new instance but an internal member only accessible via this function.
	 * @return the picking Ray. */
	public Ray getPickRay (float screenX, float screenY) {
		return getPickRay(screenX, screenY, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void positionAnimateTo(Vector3 startPos, Vector3 endPos, int startTime, int endTime) {
		position.set(calculateIntermediatePos(startPos, endPos, startTime, endTime));
	}
	
	public Vector3 calculateIntermediatePos(Vector3 startPos, Vector3 endPos, int startTime, int endTime) {
		float steps = endTime - startTime;
		float xPerFragm = (endPos.x - startPos.x) / steps;
		float yPerFragm = (endPos.y - startPos.y) / steps;
		float zPerFragm = (endPos.z - startPos.z) / steps;
		Vector3 ret = endPos;
		if(time >= startTime && time <= endTime) {
			ret = new Vector3(startPos.x + xPerFragm * (time - startTime),
					startPos.y + yPerFragm * (time - startTime),
					startPos.z + zPerFragm * (time - startTime));
		}
		if(time == endTime) {
			lastPosition = ret;
		}
		
		return ret;
	}
	
	public void lookAtAnimateto(Vector3 startPos, Vector3 endPos, int startTime, int endTime) {
		lookAt(calculateIntermediatePos(startPos, endPos, startTime, endTime));
		setUpY();
	}
	
	public void rotateAnimation(Vector3 startPos, Vector3 rotationPoint, float startAngle, float endAngle, int startTime, int endTime) {
		float anglePerFrame = (endAngle - startAngle) / (endTime - startTime);
		int relTime = time - startTime;
		float rotationDistance = Math.abs(rotationPoint.z - startPos.z);
		float angle = relTime * anglePerFrame + startAngle;
		Vector3 pos = new Vector3((float)(Math.sin(angle) * rotationDistance) + rotationPoint.x, 
					1000.0f, (float)(Math.cos(angle) * rotationDistance) + rotationPoint.z);
		if(time <= endTime) {
			position.set(pos);
			lookAt(rotationPoint);
			setUpY();
		}
		
		if(time == endTime) {
			lastPosition = pos;
		}
	}
	
	public void calculateNextCameraPosition() {
		if(time <= 400) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[0]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[1]);
			positionAnimateTo(startPos, endPos, 0, 400);
			lookAtAnimateto(startPos.add(1.0f, 0, 0.0f), endPos.add(1.0f,0,0), 0, 400);
		} else if(time <= 500) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[1]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[1]);
			lookAtAnimateto(startPos.add(1.0f, 0, 0), endPos.add(0,0,-1.0f), 400, 500);
		} else if(time <= 700) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[1]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[2]);
			positionAnimateTo(startPos, endPos, 500, 700);
			lookAtAnimateto(startPos.add(0,0,-1.0f), endPos.add(0,0,-1.0f), 500, 700);
		} else if(time <= 800) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[2]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[2]);
			lookAtAnimateto(startPos.add(0, 0, -1.0f), endPos.add(-1.0f,0,0), 700, 800);
		} else if(time <= 1200) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[2]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[3]);
			positionAnimateTo(startPos, endPos, 800, 1200);
			lookAtAnimateto(startPos.add(-1.0f,0,0), endPos.add(-1.0f,0,0), 800, 1200);
		} else if(time <= 1300) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[3]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[3]);
			lookAtAnimateto(startPos.add(-1.0f, 0, 0), endPos.add(0,0,1.0f), 1200, 1300);
		} else if(time <= 1500) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[3]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[0]);
			positionAnimateTo(startPos, endPos, 1300, 1500);
			lookAtAnimateto(startPos.add(0,0,1.0f), endPos.add(0,0,1.0f), 1300, 1500);
		} else if(time <= 1600) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[0]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[0]);
			lookAtAnimateto(startPos.add(0, 0, 1.0f), endPos.add(1.0f,0,0), 1500, 1600);
		} else if(time <= 2000) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[0]);
			Vector3 endPos = new Vector3(CASTLE_CORNERS[1].x, 3000.0f, CASTLE_CORNERS[1].z);
			positionAnimateTo(startPos, endPos, 1600, 2000);
			lookAtAnimateto(startPos.add(1.0f, 0, 0), endPos.add(0,0,-1.0f), 1600, 2000);
		} else if(time <= START_WATER) {
			Vector3 startPos = new Vector3(CASTLE_CORNERS[1].x, 3000.0f, CASTLE_CORNERS[1].z);
			Vector3 endPos = new Vector3(1000.0f, 2500.0f, 2000.0f);
			positionAnimateTo(startPos, endPos, 2000, START_WATER);
			lookAtAnimateto(startPos.add(0,0,-1.0f), endPos.add(0, -1.0f, -1.0f), 2000, START_WATER);
		} else if(time <= START_WATER + 100) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(1000.0f, 2000.0f, 1500.0f);
			positionAnimateTo(startPos, endPos, START_WATER + 0, START_WATER + 100);
			lookAtAnimateto(startPos.add(0, -1.0f, -1.0f), endPos.add(0,-0.5f,-1.0f), START_WATER + 0, START_WATER + 100);
			
		} else if(time <=START_WATER + 200) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(1000.0f, 1500.0f, 0.0f);
			positionAnimateTo(startPos, endPos,START_WATER +  101,START_WATER + 200);
			lookAtAnimateto(startPos.add(0, -0.5f, -1.0f), endPos.add(0,-0.1f,-1.0f),START_WATER + 101,START_WATER + 200);
		} else if(time <=START_WATER + 500) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(1000.0f, 1000.0f, -4000.0f);
			positionAnimateTo(startPos, endPos,START_WATER + 201,START_WATER +  500);
			lookAtAnimateto(startPos.add(0, -0.1f, -1.0f), endPos.add(0,0,-1.0f),START_WATER + 201,START_WATER +  500);
		} else if(time <=START_WATER + 600) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(750.0f, 1000.0f, -4750.0f);
			positionAnimateTo(startPos, endPos,START_WATER + 500,START_WATER + 600);
			lookAtAnimateto(startPos.add(0, 0, -1.0f), endPos.add(-1.0f, 0, 0.0f),START_WATER + 500,START_WATER + 600);
		} else if(time <=START_WATER + 800) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(lightHousePos[0], 1000.0f, -3000.0f);
			positionAnimateTo(startPos, endPos,START_WATER + 600,START_WATER + 800);
			lookAtAnimateto(startPos.add(-1.0f, 0, 0), endPos.add(0, 0, 1.0f),START_WATER + 600,START_WATER + 800);
		} else if(time <=START_WATER + 2000) {
			Vector3 target = new Vector3(lightHousePos[0], 1000.0f, lightHousePos[2]);
			Vector3 startPos = new Vector3(lastPosition);
			rotateAnimation(startPos, target, (float) (Math.PI), (float) (Math.PI * 4),START_WATER + 800,START_WATER + 2000);
		} else if(time <=START_WATER + 3000) {
			Vector3 startPos = new Vector3(lastPosition);
			Vector3 endPos = new Vector3(-1500.0f, 3500.0f, 4500.0f);
			
			Vector3 startTarget = new Vector3(lightHousePos[0], 1000.0f, lightHousePos[2]);
			Vector3 endTarget = new Vector3(lightHousePos[0], 0.0f, lightHousePos[2]);
			positionAnimateTo(startPos, endPos,START_WATER + 2000,START_WATER + 3000);
			lookAtAnimateto(startTarget, endTarget,START_WATER + 2000,START_WATER + 3000);
		}
		time++;
	}
		
//	if(time <= 100.0f) {
//		float newPosZ = lightHousePos[2] + 1000.0f - (time * 100.0f);
//		position.set(new Vector3(-100.0f, positionY, newPosZ));
//		lookAt(-100.0f, positionY - 1.0f, newPosZ - 1.0f);
//		positionY = 2000.0f - (time * 1.0f);
//		setUpY();
//	}
//		if(time > 100.0f) {
//				position.set((float)(Math.sin(cameraAngle) * rotationDistance) + lightHousePos[0], 1000.0f, (float)(Math.cos(cameraAngle) * rotationDistance) + lightHousePos[2]);
//				lookAt(lightHousePos[0], 0.0f, lightHousePos[2]);
//				setUpY();
//				update();
//				cameraAngle += 0.005f;
//				if(cameraAngle >= 6.283185307179586f) {
//					cameraAngle = 0;
//				}
//		}
}

