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


import des.game.base.GLPoint;
import des.game.base.GameComponent;

public class VectorObject extends GameComponent{
	
	protected GLPoint location;
	public double mass;
	protected Velocity velocity;
	protected boolean locked;
	protected boolean pendingRemove;
	public VectorObject(){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS_VECTOR.ordinal());
		locked = true;
		pendingRemove = false;
		mass = 1;
		velocity = new Velocity(0,0,location);
	}

	/**
	 * creates a new vector object with mass m and a velocity of (xV,yV,0)
	 */
	public VectorObject(double m,GLPoint p, double xV,double yV){
		super();
		if(m <= 0) m = 1;
		if(m > PhysicsEngine.MAX_MASS) m = PhysicsEngine.MAX_MASS;
		mass = m;
		location = p;
		velocity = new Velocity(xV,yV,location);
		pendingRemove = false;
			
	}
	/**
	 * creates a new vector object with mass m and a velocity of (xV,yV,zV)
	 */
	public VectorObject(double m,GLPoint p, double xV,double yV,double zV){
		super();
		if(m <= 0) m = 1;
		if(m > PhysicsEngine.MAX_MASS) m = PhysicsEngine.MAX_MASS;
		mass = m;
		location = p;
		velocity = new Velocity(xV,yV,zV,location);
		pendingRemove = false;
			
	}
    public void initializeFromTemplate(GameComponent other, float x,float y,float orientation,float velocity,float lifetime){
    	VectorObject comp = (VectorObject)other;
    	
    	this.mass = comp.mass;
    	this.setVelocityMagDir(velocity, orientation);
    }
	public void initialize(double m,GLPoint p, double xV,double yV){
		if(m <= 0) m = 1;
		if(m > PhysicsEngine.MAX_MASS) m = PhysicsEngine.MAX_MASS;
		mass = m;
		location = p;
		pendingRemove = false;
		
		velocity.location = p;
		velocity.xComponent = xV;
		velocity.yComponent = yV;
		
		velocity.acceleration.location = p;
		velocity.outsideAcceleration.location = p;
		

		
	}
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		if(mass <= 0) mass = 1;
		if(mass > PhysicsEngine.MAX_MASS) mass = PhysicsEngine.MAX_MASS;
		this.mass = mass;
	}
	public GLPoint getLocation() {
		return location;
	}
	public void setLocation(GLPoint location) {
		pendingRemove = false;
		
		this.location = location;
		velocity.location = location;

		
		velocity.acceleration.location = location;
		velocity.outsideAcceleration.location = location;
	}
	public void setLocation(double x,double y){
		location.setX(x);
		location.setY(y);
	}
	public void setLocation(double x,double y,double z){
		location.setX(x);
		location.setY(y);
		location.setZ(z);
	}
	private Velocity getVelocity() {
		return velocity;
	}	
	public void moveVector(double time){
		//if locked save the z coord and set it to the old value afterwards
		double tempZ = 0;
		if(locked){
			tempZ = location.getZ();
		}
		
		velocity.moveVector(time);
		if(locked){
			location.setZ(tempZ);
		}
	}
	////////////////////////////velocity functions
	public void setVelocityMagDir(double m,double d){
		velocity.setMagDir(m, d);
	}
	public void setVelocityXComponent(double x){
		velocity.setXComponent(x);
	}
	public double getVelocityXComponent(){
		return velocity.getXComponent();
	}
	public void setVelocityYComponent(double y){
		velocity.setYComponent(y);
	}
	public double getVelocityYComponent(){
		return velocity.getYComponent();
	}
	public void setVelocityZComponent(double z){
		velocity.setZComponent(z);
	}
	public double getVelocityZComponent(){
		return velocity.getZComponent();
	}
	public double getVelocityDirection(){
		return velocity.getDirection();
	}
	public double getVelocityMagnitude(){
		return velocity.getMagnitude();
	}
	
	public void setAccelerationMagDir(double m,double d){
		velocity.setAccelerationMagDir(m, d);
	}
	public double getAccelerationDirection(){
		return velocity.getAccelerationDirection();
	}
	/**
	 * Generates the magnitude from the x and y components.
	 */
	public double getAccelerationMagnitude(){
		return velocity.getAccelerationMagnitude();
	}
	
	
	public double getAccelerationXComponent() {
		return velocity.getAccelerationXComponent();
	}
	public void setAccelerationXComponent(double component) {
		velocity.setAccelerationXComponent(component);
	}
	public double getAccelerationYComponent() {
		return velocity.getAccelerationYComponent();
	}
	public void setAccelerationYComponent(double component) {
		velocity.setAccelerationYComponent(component);
	}	
	public double getAccelerationZComponent() {
		return velocity.getAccelerationZComponent();
	}
	public void setAccelerationZComponent(double component) {
		velocity.setAccelerationZComponent(component);
	}	
	public void addOutsideAcceleration(Acceleration a){
		velocity.addOutsideAcceleration(a);
	}
	public void addOutsideAcceleration(double x, double y){
		velocity.addOutsideAcceleration(x,y);
	}

	public void clearOutsideAcceleration(){
		velocity.clearOutsideAcceleration();
	}
	public double totalXAcceleration(){
		return velocity.totalXAcceleration();
	}
	public double totalYAcceleration(){
		return velocity.totalYAcceleration();
	}
	public double totalZAcceleration(){
		return velocity.totalZAcceleration();
	}
	////////////////////////////

	/**
	 * adds this object to the vectorObjectSet.
	 */
	public void add(){
		VectorObjectSet.instance.add(this);
		
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	
	public void remove(){
		VectorObjectSet.instance.remove(this);
	}

	@Override
	public void reset() {
		
		mass = 1;
		locked = true;
		velocity.zero();
		velocity.acceleration.zero();
		velocity.outsideAcceleration.zero();
		
	}


}
