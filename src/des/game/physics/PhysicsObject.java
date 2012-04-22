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



import des.game.base.DebugLog;
import des.game.base.GLPoint;
import des.game.base.GameComponent;
import des.game.boundary.Boundary;

public class PhysicsObject extends GameComponent{
	public static final int PASSIVE_TYPE = 0;
	public static final int INVISIBLE_WALL = 1;
	public static final int VISIBLE_WALL = 2;
	public static final int MOB = 3;
	public static final int PROJECTILE = 4;
	public static final int MISC = 5;
	public int type;
	public VectorObject vector;
	public Field field;
	public Boundary boundary;
	public GLPoint location;
	public CollisionBehavior collisionBehavior;
	public boolean active;

	public boolean hasVector;

	
	public PhysicsObject(){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS_OBJECT.ordinal());
		active = true;
		type = MOB;
		location = new GLPoint();
		
	}
	/**
	 * 
	 * @param b Boundary of this object may be null.
	 * @param v VectorObject of this object may be null.
	 * @param f Field of this object may be null.
	 * @param p Point of this object may not be null.
	 */
	public PhysicsObject(Boundary b,VectorObject v,Field f, GLPoint p){
		super();
        boundary = b; 
        vector = v;
        field = f;
      
		location = p;
		type = MOB;
	}
	
    public void initializeFromTemplate(GameComponent other, float x,float y,float orientation,float velocity,float lifetime){
    	PhysicsObject comp = (PhysicsObject)other;
    	this.location.setX(x);
    	this.location.setY(y);
    	this.location.setZ(0);
    	this.type = comp.type;
    }
    
    public void propagateLocation(){
    	if(vector != null){
    		this.vector.setLocation(this.location);
    	}
    	if(boundary != null){
    		this.boundary.propagateLocation(this.location);
    	}
    	if(field != null){
    		field.area.propagateLocation(this.location);
    		
    		if(field.fieldBehavior != null){
    			field.fieldBehavior.location = this.location;
    		}
    	}
    }
	/**
	 * Gets a reference to the VectorObject of this object.
	 * @return A reference to the VectorObject
	 */
	public VectorObject getVector() {
		return vector;
	}
	public void setVector(VectorObject vector) {
		this.vector = vector;
	}
	/**
	 * Gets a reference to the Field of this object.
	 * @return A reference to the Field
	 */
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	/**
	 * Gets a reference to the Boundary of this object.
	 * @return A reference to the Boundary
	 */
	public Boundary getBoundary() {
		return boundary;
	}
	public void setBoundary(Boundary boundary) {
		this.boundary = boundary;
	}
	public void add(){
		active = true;
		PhysicsObjectSet.instance.add(this);
		if(boundary != null) BoundarySet.instance.add(this);
		if(vector != null) vector.add();
		if(field != null) field.add();
	}


	/** 
	 * Allows sub-classes to choose how to handle a collision. called by Physics Engine after
	 * a collision.
	 * @param other PhysicsObject that has collided with this object
	 */
	public void handleCollision(PhysicsObject other){
		CollisionBehavior cb = getCollisionBehavior();
		
		if(cb != null){
			cb.handleCollision(other.getCollisionBehavior());
		}
	}
	/**
	 * Value of type used in the collision matrix, serves as an index in the matrix
	 * @return value of type
	 */
	public int getType(){
		return type;
	}
	/**
	 * sets the value for type, should not be less than 0 or greater than or equal to the size
	 * 		of the collision matrix
	 * @param t
	 */
	public void setType(int t){
		if(t < 0 || t >= PhysicsEngine.getCollisionMatrixSize()){
			t = MOB;
		}
		type = t;
	}
	/**
	 * gets reference to location.
	 */
	public GLPoint getLocation(){
		return location;
	}

	public CollisionBehavior getCollisionBehavior() {
		return collisionBehavior;
	}
	public void setCollisionBehavior(CollisionBehavior collisionBehavior) {
		this.collisionBehavior = collisionBehavior;
	}
	public void haltMotion(){
		if(this.vector != null){
			this.vector.velocity.xComponent = this.vector.velocity.yComponent = 0d;
		}
	}
	public void remove(){
		active = false;
		PhysicsObjectSet.instance.remove(this);
		if(boundary != null) BoundarySet.instance.remove(this);
		if(vector != null) vector.remove();
		if(field != null) field.remove();
	}
	public void removeBoundary(){
		if(boundary != null) BoundarySet.instance.remove(this);
	}
	@Override
	public void reset() {
		if(active){
			this.remove();
		}
		type = MOB;
		vector = null;
		field = null;
		boundary = null;
		
		collisionBehavior = null;
		
	}
}
