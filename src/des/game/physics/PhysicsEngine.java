/*******************************************************************************
 * Copyright 2011 Douglas Siemon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package des.game.physics;


import android.os.SystemClock;

import java.util.*;


import des.game.base.BaseObject;
import des.game.base.DebugLog;
import des.game.base.GLPoint;
import des.game.boundary.Boundary;
import des.game.boundary.Circle;
import des.game.boundary.Pair;
import des.game.boundary.Polygon;
import des.game.boundary.Rectangle;



public class PhysicsEngine extends BaseObject{

	
	private int mProfileFrames = 0;
    private long mProfileTime = 0;
    private static final float PROFILE_REPORT_DELAY = 3.0f;
    
	public static PhysicsEngine instance;
	//depth
	private int zPlane;
	private int planeDepth;
	private Pair dummyPair = new Pair();

	
	
    public static final int    DEFAULT_BOARD_RESET_TIME = 1000;	
    public static final int    MIN_BOARD_RESET_TIME = 100;

    public static final double TILE_TOLERANCE = .01;
    public static final double MIN_TILE_SIZE = .1;
	public static final int    MAX_BOARD_DIMENSION = 200;
	public static final int    MIN_BOARD_DIMENSION = 10;


	//timing and limits
	public static final int MAX_DELAY = 30;
	public static final int DELAY = 25;
	public static final int LONG_DELAY = 500;
	public static final int MAX_VELOCITY = 1000;
	public static final int MAX_MASS = 50;
	public static final int MIN_SIZE = 5;
	
	public static final double EXPUNGE_TOLERANCE = .1;
	
	
	private PhysicsEngine(){
		zPlane = 0;
		planeDepth = 2;

	}
	
	public static void initialize(int fieldSetSize, int vectorSetSize, int boundarySetSize, int physicsSetSize){
		if(PhysicsEngine.instance == null){
			PhysicsEngine.instance = new PhysicsEngine();
			FieldSet.initialize(fieldSetSize);
			VectorObjectSet.initializeSet(vectorSetSize);
			BoundarySet.initializeSet(boundarySetSize);
			PhysicsObjectSet.initializeSet(physicsSetSize);
		}
	}
	
	////////Settings////////////
	/**
	 * gets the value of the bottom of the main z plane slice
	 * @return value of main z plane
	 */
	public int getZPlane() {
		return zPlane;
	}
	/**
	 * sets the value of the bottom of the main z plane slice, which also determines where slices start
	 * @param plane new value
	 */
	public void setZPlane(int plane) {
		zPlane = plane;
	}
	/**
	 * gets thickness of the z plane slices
	 * @return 
	 */
	public int getPlaneDepth() {
		return planeDepth;
	}
	/**
	 * sets thickness of the z plane slices
	 * @param planeDepth
	 */
	public void setPlaneDepth(int planeDepth) {
		this.planeDepth = planeDepth;
	}

	/**
	 * gets the size of the collision matrix, also the same as the number of types available for physics objects
	 * @return the size
	 */
	public static int getCollisionMatrixSize(){
		return BoundarySet.collisionMatrix.length;
	}
	/**
	 * adds a row to the collisionmatrix, adding a new type for the physicsObjects
	 * @param newRow contains whether or not the new type is allowed to collide with the type at each index
	 * 			should be either size of the matrix long
	 * @return the id for the new type
	 */
	public static int addRowToCollisionMatrix( ArrayList<Boolean> newRow){
		return BoundarySet.addRow(newRow);
	}
	/////////Services////////////////////

	/**
	 * This function advances the physics engine by time milliseconds
	 */
	@Override
	public void update(float timeDelta, BaseObject parent){
		
		final long realtime = SystemClock.uptimeMillis();
		
		int time = (int)(timeDelta*1000);
		if(time > MAX_DELAY){
			time = MAX_DELAY;
		}
		updateSets(time);
		
		clearOutsideAccelerations();
		applyFields(time);
		moveVectorObjects(time);
		checkCollisions(time);
		
		final long endTime = SystemClock.uptimeMillis();
		
		mProfileTime += endTime-realtime;
        mProfileFrames++;
        if (mProfileTime > PROFILE_REPORT_DELAY * 100) {
            final long averageFrameTime = mProfileTime / mProfileFrames;
            DebugLog.d("Game Profile", "Physics Average: " + averageFrameTime);
            mProfileTime = 0;
            mProfileFrames = 0;
            //mGameRoot.sSystemRegistry.hudSystem.setFPS(1000 / (int)averageFrameTime); TODO
        }
	}
	/**
	 * clears all sets in the physics engine
	 */
	public void clearObjects(){
		BoundarySet.instance.clear();
		VectorObjectSet.instance.clear();
		FieldSet.instance.clear();
		PhysicsObjectSet.instance.clear();
	}

	/**
	 * finds all physics objects that are intersecting with b, excluding passive type objects but
	 * 		otherwise ignoring type constraints 
	 * @param b
	 * @return
	 */
	public ArrayList<PhysicsObject> checkCollision(Boundary b){
		ArrayList<PhysicsObject> intersections = new ArrayList<PhysicsObject>();
		int size = BoundarySet.instance.getCount();
		for(int i = 0; i < size; i++){
			PhysicsObject a = BoundarySet.instance.get(i);
			boolean coPlanar = a.getBoundary().isCoplanar(b);

			if(a.getType() != PhysicsObject.PASSIVE_TYPE && coPlanar && BoundarySet.instance.get(i).getBoundary().collision(b)){
				intersections.add(a);
			}
		}
		return intersections;
	}
	/**
	 * checks if there is a line of sight between the two locations, ignoring differences in z.
	 * 		Also ignores passive objects
	 * @param startX 
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return true if there is a line of sight between the two points.
	 */
	public boolean lineOfSight(double startX,double startY,double endX,double endY){
		boolean flag = true;
		int size = BoundarySet.instance.getCount();
		for(int i = 0; i < size && flag; i++){
			PhysicsObject a = BoundarySet.instance.get(i);

			if(a.getType() != PhysicsObject.PASSIVE_TYPE && BoundarySet.instance.get(i).getBoundary().collision(startX,startY,endX,endY)){
				flag = false;
			}
		}
		return flag;
	}
	/**
	 * checks if there is a line of sight between the two locations, using the z coord to determine collisions. 
	 * 		Also ignores passive objects
	 * @param startX 
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param zPlane the z coordinate to use, the line is parrallel to the x,y plane
	 * @return true if there is a line of sight betweent the two points.
	 */
	public boolean lineOfSight(double startX,double startY,double endX,double endY, double zPlane){
		boolean flag = true;
		int size = BoundarySet.instance.getCount();
		for(int i = 0; i < size && flag; i++){
			PhysicsObject a = BoundarySet.instance.get(i);
			boolean coPlanar = a.getBoundary().isCoplanar(zPlane);

			if(a.getType() != PhysicsObject.PASSIVE_TYPE && coPlanar && BoundarySet.instance.get(i).getBoundary().collision(startX,startY,endX,endY)){
				flag = false;
			}
		}
		return flag;
	}
	public PhysicsObject[] getObjects(){
		return PhysicsObjectSet.instance.getObjects().getArray();
	}
	/////////private functions//////////////////////
	/**
	 * updates each object manager that the physics engine is using, the order is important
	 */
	public void updateSets(float timeDelta){
		VectorObjectSet.instance.update(timeDelta, this);
		FieldSet.instance.update(timeDelta, this);
		BoundarySet.instance.update(timeDelta, this);
		PhysicsObjectSet.instance.update(timeDelta, this);
	}

	/**
	 * Clears the outsideAccelerations of all VectorObjects.
	 */
	protected void clearOutsideAccelerations(){
		final int size = VectorObjectSet.instance.getCount();
		
		
		Object[] set = VectorObjectSet.instance.getObjects().getArray();
		for(int i = 0; i < size; i++){
			((VectorObject)set[i]).clearOutsideAcceleration();
		}
	}
	/**
	 * Moves vectorObjects forward in time, by DELAY milliseconds.
	 */
	protected void moveVectorObjects(int time){
		
		final int count = VectorObjectSet.instance.getCount();
		//DebugLog.e("physics", "vectorCount: " +  count);
		final Object[] set = VectorObjectSet.instance.getObjects().getArray();
		
//		for(int i = 0; i < count-1; i++){
//			for(int j = i+1; j < count; j++){
//				if(set[i] == set[j]){
//					DebugLog.e("physics", "verror: " );
//				}
//			}
//		}
//	
//		final int bcount = BoundarySet.instance.getCount();
//		final Object[] bset = BoundarySet.instance.getObjects().getArray();
//		
//		for(int i = 0; i < bcount-1; i++){
//			for(int j = i+1; j < bcount; j++){
//				if(bset[i] == bset[j]){
//					DebugLog.e("physics", "berror: " );
//				}
//			}
//		}
		for(int i = 0; i < count; i++){
			((VectorObject)set[i]).moveVector(time);
		}
	}
	/**
	 * Applies Fields to VectorObjects.
	 */
	protected void applyFields(int time){
		//set up projections and sort the field set, a high initial cost
		//	but this will allow optimizations later when checking physics 
		//	objects against each field
		FieldSet.instance.updateBoxes();
		FieldSet.instance.sort();
		BoundarySet.instance.updateBoxes();
		Field f;
		PhysicsObject po;	
		
		final int size = FieldSet.instance.getCount();
		final Object[] fieldSet = FieldSet.instance.getObjects().getArray();
		
		final int physicsSize = PhysicsObjectSet.instance.getCount();
		final Object[] physicsSet = PhysicsObjectSet.instance.getObjects().getArray();
		//for each physics object
		for(int i = 0; i < physicsSize; i++){
			po = (PhysicsObject)physicsSet[i];
			if(po.getVector() != null){
				
				int start = 0;
				//if it has a boundary, apply fields based on the boundary
				if(po.getBoundary() != null){
					//find the first field whose x projection overlaps with the objects projection
					while(start < size && po.getBoundary().getXProjection().getEnd() < ((Field)fieldSet[start]).getArea().getXProjection().getStart()){
						start++;
					}
					//apply each field until a field no longer intersects with the objects projection
					for(int j = start; j < size && po.getBoundary().getXProjection().overlap(((Field)fieldSet[j]).getArea().getXProjection()); j++){
						f = (Field)fieldSet[j];
						//check if the y projection is overlapping
						boolean coPlanar = po.getBoundary().isCoplanar(f.getArea());
	
						if(coPlanar && po.getBoundary().getYProjection().overlap(f.getArea().getYProjection())){
							if(po.getBoundary().collision(f.area)){
								f.handleCollision(po,time);
							}
						}
					}
				}
				else{
					
					while(start < size && po.getVector().getLocation().getX() < ((Field)fieldSet[start]).getArea().getXProjection().getStart()){
						start++;
					}
					
					//apply each field until a field no longer intersects with the objects location
					for(int j = start; j < size && ((Field)fieldSet[j]).getArea().getXProjection().overlap(po.getVector().getLocation().getX()); j++){
						f = (Field)fieldSet[j];
						boolean coPlanar = f.getArea().isCoplanar(po.getLocation().getZ());
				
						if(coPlanar && f.getArea().getYProjection().overlap(po.getVector().getLocation().getY())){
							if(f.area.collision(po.getVector().getLocation())){
								f.handleCollision(po,time);
							}
						}
					}
				}
			}

		}

	}
	/**
	 * Checks for Collisions in the BoundrySet and then resolves them accordingly.
	 */
	protected void checkCollisions(int time){
		//set bounding boxes of boundaries
		BoundarySet.instance.updateBoxes();
		//sort by min x coordinate in the bounding boxes
		BoundarySet.instance.sort();
		
		PhysicsObject a;
		PhysicsObject b;
		//for each boundary
		final int size = BoundarySet.instance.getCount();
		final Object[] boundaryArray = BoundarySet.instance.getObjects().getArray();
		
		for(int i = 0; i < size-1; i++){
			a = (PhysicsObject)boundaryArray[i];
			
			
			
			//start at the next boundary in the sorted list and check until the bounding boxes 
			//   no longer overlap, after this no boundaries in the list will overlap with this 
			//   boundary as they are sorted
			for(int j = i+1; j < size && a.getBoundary().getXProjection().overlap(((PhysicsObject)boundaryArray[j]).getBoundary().getXProjection()); j++){
				b = (PhysicsObject)boundaryArray[j];
				//first check the y bounding box
				if(a.getBoundary().getYProjection().overlap(b.getBoundary().getYProjection())){
					
					int aType = a.getType();
					int bType = b.getType();
					if(aType == PhysicsObject.PASSIVE_TYPE || bType == PhysicsObject.PASSIVE_TYPE){
						//check if objects are in the same z coordinate slice
						boolean coPlanar = a.getBoundary().isCoplanar(b.getBoundary());
					
						
						if(coPlanar && a.getBoundary().collision(b.getBoundary())){
							if(aType != PhysicsObject.PASSIVE_TYPE){
								b.handleCollision(a);
							}
							else if(bType != PhysicsObject.PASSIVE_TYPE){
								a.handleCollision(b);
							}
						}
					}
					//check that at least one object has a velocity vector, and that at least one is not phantom
					// and that they overlap
					else if((a.getVector() != null ||b.getVector() != null) && (BoundarySet.collisionMatrix[aType][bType])){
						//check if objects are in the same z coordinate slice
						boolean coPlanar = a.getBoundary().isCoplanar(b.getBoundary());
						
						if(coPlanar && a.getBoundary().collision(b.getBoundary())){
							
							//move object back in time until they are no longer colliding
							int temp = walkBack(a,b,time);
							//change the velocity vectors accordingly
							resolveCollision(a,b);
							
		
							//move vector again?
							
							if(a.getVector()!= null){
								a.getVector().moveVector(temp);
							}
							if(b.getVector()!= null){
								b.getVector().moveVector(temp);
							}
							//if the boundaries are still overlapping move them so that they are not
							if(a.getBoundary().collision(b.getBoundary())){
								expunge(a,b);
							}
							//let each object know that there was a collision
							a.handleCollision(b);
							b.handleCollision(a);
						}
					}
				}
			}
		}
	}
	/**
	 * takes two PhysicsObjects that have collided and moves them backward in time until they
	 * 		are no longer overlapping.
	 * @param a
	 * @param b
	 * @return returns how far back they went in time.
	 */
	protected int walkBack(PhysicsObject a,PhysicsObject b,int time){
		
		if(a.getVector() != null || b.getVector() != null){
			if(a.getVector() == null){
				PhysicsObject c = b;
				b = a;
				a = c;
			}
			a.getVector().moveVector(-time/2);
			if(b.getVector() != null)
			b.getVector().moveVector(-time/2);
			
			time = rBisect(0,time/2,time,a,b);
		}
		
		return time;
	}
	/**
	 * Recursive function to find the point when two physicsObjects collided, uses a binary search
	 * @param left latest time with no collision found
	 * @param cur  current spot,also current location of the two physicsObjects
	 * @param right earliest time with collision found
	 */
	protected int rBisect(int left,int cur,int right,PhysicsObject a,PhysicsObject b){
		int temp = cur;
		if(a.getBoundary().collision(b.getBoundary())){
			right = cur;
			cur = (right + left)/2;
		}
		else{
			left = cur;
			cur = (right + left)/2;
		}
		temp = cur - temp;
		a.getVector().moveVector(temp);
		if(b.getVector() != null)
		b.getVector().moveVector(temp);
		if(left < right-1)
		cur = rBisect(left,cur,right,a,b);
		
		return cur;
	}
	/**
	 * Resolves a collision betweeen two physics objects by changing their velocity magnitude and direction
	 *  
	 * @param a 
	 * @param b
	 */
	protected void resolveCollision(PhysicsObject a,PhysicsObject b){
		//if both objects have velocity vectors
		if(a.getVector() != null && b.getVector() != null){
			VectorObject av = a.getVector();
			VectorObject bv = b.getVector();
			//calculate impulse
			double impulse = (2 * av.getMass() * bv.getMass()/(av.getMass() + bv.getMass()));
			final double normal = a.getBoundary().getNormal(b.getBoundary());
			//differences in velocity
			final double tempX = (av.getVelocityXComponent() - bv.getVelocityXComponent()) * Math.cos(normal);
			final double tempY = (av.getVelocityYComponent() - bv.getVelocityYComponent()) * Math.sin(normal);
			//account for mass
			impulse = impulse * (tempX + tempY);
			
			final double impulseX = impulse * Math.cos(normal);
			final double impulseY = impulse * Math.sin(normal);
			//set new velocities
			av.setVelocityXComponent(av.getVelocityXComponent()-impulseX/av.getMass() );
			av.setVelocityYComponent(av.getVelocityYComponent() - impulseY/av.getMass());
			
			bv.setVelocityXComponent(impulseX/bv.getMass() + bv.getVelocityXComponent());
			bv.setVelocityYComponent(impulseY/bv.getMass() + bv.getVelocityYComponent());
			
		}
		//if only one of the objects has a velocity vector
		else if(a.getVector() != null || b.getVector() != null){
			//make sure a is the object with the vector
			if(a.getVector() == null){
				PhysicsObject c = b;
				b = a;
				a = c;
			}
			//get the vector and the normal of collision
			VectorObject av = a.getVector();
			final double normal = a.getBoundary().getNormal(b.getBoundary());
			//calculate new direction
			double dir = av.getVelocityDirection()+Math.PI;
			
			while(dir >= Math.PI*2) dir -= Math.PI*2;
		
			
			final double delta = normal-dir;
			dir += 2*delta;// + Math.PI;
			//set the new direction, the magnitude will not change
			av.setVelocityMagDir(av.getVelocityMagnitude(),dir);
		}
	}
	/**
	 * This function will move two physics objects whose boundries are intersecting apart 
	 * @param a
	 * @param b
	 */
	protected void expunge(PhysicsObject a,PhysicsObject b){
		//cases
		// polygon *
		// circle circle
		// circle rectangle
		// rectangle rectangle
	
		if(a.getBoundary().getPolygon() == null && b.getBoundary().getCircle() != null){
			PhysicsObject temp = a;
			a = b;
			b = temp;
		}

		if(a.getVector() != null && b.getVector() != null){///////both///////////////////////////////////////////
			
			if(a.getBoundary().getPolygon() != null){
				if(b.getBoundary().getPolygon() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Polygon p2 = b.getBoundary().getPolygon();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, p2, normal);
					//each polygon will be moved by mindistance
					
					normal.y = normal.y/2;
					GLPoint point = p1.getCenter();
					double dx = normal.y * Math.cos(normal.x);
					double dy = normal.y * Math.sin(normal.x);
					
					point.setX(point.getX() + dx);
					point.setY(point.getY() + dy);
					
					point = p2.getCenter();

					point.setX(point.getX() - dx);
					point.setY(point.getY() - dy);
					
				}
				else if(b.getBoundary().getCircle() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Circle c2 = b.getBoundary().getCircle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, c2, normal);
					//each polygon will be moved by mindistance
					
					normal.y = normal.y/2;
					GLPoint point = p1.getCenter();
					double dx = normal.y * Math.cos(normal.x);
					double dy = normal.y * Math.sin(normal.x);
					
					point.setX(point.getX() - dx);
					point.setY(point.getY() - dy);
					
					point = c2.getCenter();

					point.setX(point.getX() + dx);
					point.setY(point.getY() +dy);
				}
				else if(b.getBoundary().getRectangle() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Rectangle r2 = b.getBoundary().getRectangle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, r2, normal);
					//each polygon will be moved by mindistance
			
					normal.y = normal.y/2 + EXPUNGE_TOLERANCE;
					GLPoint point = p1.getCenter();
					double dx = normal.y * Math.cos(normal.x);
					double dy = normal.y * Math.sin(normal.x);
					
					point.setX(point.getX() + dx);
					point.setY(point.getY() + dy);
					
					point = r2.getCenter();

					point.setX(point.getX() - dx);
					point.setY(point.getY() - dy);
				}
			}
			else if(a.getBoundary().getCircle() != null){
				if(b.getBoundary().getCircle() != null){
					Circle c1 = a.getBoundary().getCircle();
					Circle c2 = b.getBoundary().getCircle();
					
					double normal = c1.getNormal(c2);
					
					double dis = c1.getCenter().distance(c2.getCenter());
					
					
					dis =  ((c1.getRadius() + c2.getRadius())+EXPUNGE_TOLERANCE - dis)/2;
					c2.getCenter().setX(c2.getCenter().getX() +(dis)*Math.cos(normal));
					c2.getCenter().setY(c2.getCenter().getY() + (dis)*Math.sin(normal));
					normal+= Math.PI;
					c1.getCenter().setX(c1.getCenter().getX() +(dis)*Math.cos(normal));
					c1.getCenter().setY(c1.getCenter().getY() + (dis)*Math.sin(normal));
				}
				else if(b.getBoundary().getRectangle() != null){
					Circle c = a.getBoundary().getCircle();
					Rectangle r = b.getBoundary().getRectangle();
					double x;
					double y;
					double left = r.getLeft();
					double right = r.getRight();
					double top = r.getTop();
					double bottom = r.getBottom();
					double normal = c.getNormal(r);
					if(c.getCenter().getX() > right){
						x = right;
					}
					else if(c.getCenter().getX() < left){
						x = left;
					}
					else{
						x = c.getCenter().getX();
					}
					
					if(c.getCenter().getY() > top){
						y = top;
					}
					else if(c.getCenter().getY() < bottom){
						y = bottom;
					}
					else{
						y = c.getCenter().getY();
					}
					
					//
					if(x == c.getCenter().getX() && y == c.getCenter().getY()){
						if(normal == 0){
							c.getCenter().setX(c.getCenter().getX() + (c.getRadius() + (right-c.getCenter().getX()))/2);
							r.getCenter().setX(r.getCenter().getX() - (c.getRadius() + (right-c.getCenter().getX()))/2);
						}
						else if(normal == Math.PI){
							c.getCenter().setX(c.getCenter().getX() - (c.getRadius() + (c.getCenter().getX()-left))/2);
							r.getCenter().setX(r.getCenter().getX() + (c.getRadius() + (c.getCenter().getX()-left))/2); 
						}
						else if(normal == Math.PI/2){
							c.getCenter().setY(c.getCenter().getY() + (c.getRadius() + (top - c.getCenter().getY()))/2);
							r.getCenter().setX(r.getCenter().getX() - (c.getRadius() + (top - c.getCenter().getY()))/2);
						}
						else{
							c.getCenter().setY(c.getCenter().getY() - (c.getRadius() + (c.getCenter().getY()-bottom))/2);
							r.getCenter().setX(r.getCenter().getX() + (c.getRadius() + (c.getCenter().getY()-bottom))/2);
						}
					}
					else{
						c.getCenter().setX(x + (c.getRadius()*Math.cos(normal))/2);
						c.getCenter().setY(y + (c.getRadius()*Math.sin(normal))/2);
						
						r.getCenter().setX(r.getCenter().getX() - (c.getRadius()*Math.cos(normal))/2);
						r.getCenter().setY(r.getCenter().getY() - (c.getRadius()*Math.sin(normal))/2);
					}
					//
					/*
					double dis = c.getCenter().distance(new Point(x,y));
					
					dis = (c.getRadius()+.1-dis)/2;
					r.getCenter().x = r.getCenter().x +(dis)*Math.cos(normal);
					r.getCenter().y = r.getCenter().y + (dis)*Math.sin(normal);
					normal+= Math.PI;
					c.getCenter().x = c.getCenter().x +(dis)*Math.cos(normal);
					c.getCenter().y = c.getCenter().y + (dis)*Math.sin(normal);*/
				}
			}
			else if(a.getBoundary().getRectangle() != null){
				if(b.getBoundary().getRectangle() != null){
					Rectangle r1 = a.getBoundary().getRectangle();
					Rectangle r2 = b.getBoundary().getRectangle();
					
					double normal = r1.getNormal(r2);
					if(normal == Math.PI || normal == 0){
						if(r1.getLeft() > r2.getLeft()){
							Rectangle temp = r1;
							r1 = r2;
							r2 = temp;
						}
						double delta = r2.getLeft() - r1.getLeft();
						delta = (r1.getWidth() +EXPUNGE_TOLERANCE - delta)/2;
						r1.getCenter().setX(r1.getCenter().getX() - delta);
						r2.getCenter().setX(r2.getCenter().getX() + delta);
					}
					else{
						if(r1.getTop() > r2.getTop()){
							Rectangle temp = r1;
							r1 = r2;
							r2 = temp;
						}
						double delta = r2.getTop() - r1.getTop();
						delta = (r2.getHeight() +EXPUNGE_TOLERANCE - delta)/2;
						r1.getCenter().setY(r1.getCenter().getY() - delta);
						r2.getCenter().setY(r2.getCenter().getY() + delta);
					}
				}	
			}
			
		}
		else if(a.getVector() != null || b.getVector() != null){/////////one//////////////////////////////
			if(a.getVector() == null){
				PhysicsObject c = b;
				b = a;
				a = c;
			}
			if(a.getBoundary().getPolygon() != null){
				if(b.getBoundary().getPolygon() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Polygon p2 = b.getBoundary().getPolygon();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, p2, normal);
					//each polygon will be moved by mindistance
				
					GLPoint point = p1.getCenter();
			
					point.setX(point.getX() + normal.y * Math.cos(normal.x) + EXPUNGE_TOLERANCE);
					point.setY(point.getY() + normal.y * Math.sin(normal.x) + EXPUNGE_TOLERANCE);
				}
				else if(b.getBoundary().getCircle() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Circle c2 = b.getBoundary().getCircle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, c2, normal);
					//each polygon will be moved by mindistance
					GLPoint point = p1.getCenter();

					point.setX(point.getX() - (normal.y * Math.cos(normal.x) + EXPUNGE_TOLERANCE));
					point.setY(point.getY() - (normal.y * Math.sin(normal.x) + EXPUNGE_TOLERANCE));
				}
				else if(b.getBoundary().getRectangle() != null){
					Polygon p1 = a.getBoundary().getPolygon();
					Rectangle r2 = b.getBoundary().getRectangle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p1, r2, normal);
					//each polygon will be moved by mindistance
					GLPoint point = p1.getCenter();		
					point.setX(point.getX() + (normal.y * Math.cos(normal.x) + EXPUNGE_TOLERANCE));
					point.setY(point.getY() + (normal.y * Math.sin(normal.x) + EXPUNGE_TOLERANCE));
				}
			}
			else if(a.getBoundary().getCircle() != null){
				if(b.getBoundary().getPolygon() != null){
					Polygon p2 = b.getBoundary().getPolygon();
					Circle c1 = a.getBoundary().getCircle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p2, c1, normal);
					//each polygon will be moved by mindistance
					GLPoint point = c1.getCenter();

					point.setX(point.getX() + (normal.y * Math.cos(normal.x) + EXPUNGE_TOLERANCE));
					point.setY(point.getY() + (normal.y * Math.sin(normal.x) + EXPUNGE_TOLERANCE));
				}
				else if(b.getBoundary().getCircle() != null){
					Circle c1 = a.getBoundary().getCircle();
					Circle c2 = b.getBoundary().getCircle();
					
					double normal = c2.getNormal(c1);
					
					double dis = c1.getCenter().distance(c2.getCenter());
					
					
					dis =  ((c1.getRadius() + c2.getRadius())+EXPUNGE_TOLERANCE - dis);

					c1.getCenter().setX(c1.getCenter().getX() +(dis)*Math.cos(normal));
					c1.getCenter().setY(c1.getCenter().getY() + (dis)*Math.sin(normal));
				}
				else if(b.getBoundary().getRectangle() != null){
					Circle c = a.getBoundary().getCircle();
					Rectangle r = b.getBoundary().getRectangle();
					double left = r.getLeft();
					double right = r.getRight();
					double top = r.getTop();
					double bottom = r.getBottom();
					double normal = c.getNormal(r);
					double x;
					double y;
					
					if(c.getCenter().getX() >= right){
						x = right;
					}
					else if(c.getCenter().getX() <= left){
						x = left;
					}
					else{
						x = c.getCenter().getX();
					}
					
					if(c.getCenter().getY() >= top){
						y =top;
					}
					else if(c.getCenter().getY() <= bottom){
						y = bottom;
					}
					else{
						y = c.getCenter().getY();
					}
					
					if(x == c.getCenter().getX() && y == c.getCenter().getY()){
						if(normal == 0){
							c.getCenter().setX(c.getCenter().getX() + (c.getRadius() + (right-c.getCenter().getX())));
						}
						else if(normal == Math.PI){
							c.getCenter().setX(c.getCenter().getX() - (c.getRadius() + (c.getCenter().getX()-left)));
						}
						else if(normal == Math.PI/2){
							c.getCenter().setY(c.getCenter().getY() + (c.getRadius() + (top - c.getCenter().getY())));
						}
						else{
							c.getCenter().setY(c.getCenter().getY() - (c.getRadius() + (c.getCenter().getY()-bottom)));
						}
					}
					else{
						c.getCenter().setX(x + c.getRadius()*Math.cos(normal));
						c.getCenter().setY(y + c.getRadius()*Math.sin(normal));
					}
					
					
				}
			}
			else if(a.getBoundary().getRectangle() != null){
				if(b.getBoundary().getPolygon() != null){
					Polygon p2 = b.getBoundary().getPolygon();
					Rectangle r1 = a.getBoundary().getRectangle();
					//get the normal and the min distance of the polygons stored in a pair. x=normal y=mindistance
					Pair normal = dummyPair;
					Polygon.expunge(p2, r1, normal);
					//each polygon will be moved by mindistance
					GLPoint point = r1.getCenter();		
					point.setX(point.getX() - (normal.y * Math.cos(normal.x) + EXPUNGE_TOLERANCE));
					point.setY(point.getY() - (normal.y * Math.sin(normal.x) + EXPUNGE_TOLERANCE));
				}
				else if(b.getBoundary().getCircle() != null){//broke*************************************solved
					Circle c = b.getBoundary().getCircle();
					Rectangle r = a.getBoundary().getRectangle();
					double left = r.getLeft();
					double right = r.getRight();
					double top = r.getTop();
					double bottom = r.getBottom();
					double normal = c.getNormal(r);
					double x;
					double y;
					
					if(c.getCenter().getX() >= right){
						x = right;
					}
					else if(c.getCenter().getX() <= left){
						x = left;
					}
					else{
						x = c.getCenter().getX();
					}
					
					if(c.getCenter().getY() >= top){
						y =top;
					}
					else if(c.getCenter().getY() <= bottom){
						y = bottom;
					}
					else{
						y = c.getCenter().getY();
					}
					
					if(x == c.getCenter().getX() && y == c.getCenter().getY()){
						if(normal == 0){
							
							r.getCenter().setX(r.getCenter().getX() - (c.getRadius() + (right-c.getCenter().getX())));
						}
						else if(normal == Math.PI){
							
							r.getCenter().setX(r.getCenter().getX() + (c.getRadius() + (c.getCenter().getX()-left)));
						}
						else if(normal == Math.PI/2){
							
							r.getCenter().setY(r.getCenter().getY() - (c.getRadius() + (top - c.getCenter().getY())));
						}
						else{
							
							r.getCenter().setY(r.getCenter().getY() + (c.getRadius() + (c.getCenter().getY()-bottom)));
						}
					}
					else{		
						r.getCenter().setX(r.getCenter().getX() - c.getRadius()*Math.cos(normal));
						r.getCenter().setY(r.getCenter().getY() - c.getRadius()*Math.sin(normal));
					}
					/*Circle c = b.getBoundry().getCircle();
					Rectangle r = a.getBoundry().getRectangle();
					double x;
					double y;
					double normal = c.getNormal(r);
					if(c.getCenter().x > r.getCenter().x + r.getWidth()){
						x = r.getCenter().x + r.getWidth();
					}
					else if(c.getCenter().x < r.getCenter().x){
						x = r.getCenter().x;
					}
					else{
						x = c.getCenter().x;
					}
					
					if(c.getCenter().y > r.getCenter().y){
						y = r.getCenter().y;
					}
					else if(c.getCenter().y < r.getCenter().y - r.getHeight()){
						y = r.getCenter().y - r.getHeight();
					}
					else{
						y = c.getCenter().y;
					}
					
					double dis = c.getCenter().distance(new Point(x,y));
					
					dis = (c.getRadius()+.1-dis);
					r.getCenter().x = r.getCenter().x +(dis)*Math.cos(normal);
					r.getCenter().y = r.getCenter().y + (dis)*Math.sin(normal);
*/
				}
				else if(b.getBoundary().getRectangle() != null){
					Rectangle r1 = a.getBoundary().getRectangle();
					Rectangle r2 = b.getBoundary().getRectangle();
					boolean flag = true;
					double normal = r1.getNormal(r2);
					if(normal == Math.PI || normal == 0){
						
						if(r1.getLeft() > r2.getLeft()){
							Rectangle temp = r1;
							r1 = r2;
							r2 = temp;
							flag = false;
						}
						double delta = r2.getLeft() - r1.getLeft();
						delta = (r1.getWidth() +EXPUNGE_TOLERANCE - delta);
						if(flag)r1.getCenter().setX(r1.getCenter().getX() - delta);
						else    r2.getCenter().setX(r2.getCenter().getX() + delta);
					
					}
					else{
						if(r1.getTop() > r2.getTop()){
							Rectangle temp = r1;
							r1 = r2;
							r2 = temp;
							flag = false;
						}
						double delta = r2.getTop() - r1.getTop();
						delta = (r2.getHeight() +EXPUNGE_TOLERANCE - delta);
						if(flag) r1.getCenter().setY(r1.getCenter().getY() - delta);
						else     r2.getCenter().setY(r2.getCenter().getY() + delta);
					}
				}	
			}
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
