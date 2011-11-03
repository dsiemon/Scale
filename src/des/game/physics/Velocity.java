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

/**
 * represents a velocity vector, also contains an internal acceleration and
 * 		an acceleration from external forces.
 *
 */

public class Velocity extends Vector{

	protected Acceleration acceleration;
	protected Acceleration outsideAcceleration;
	
	Velocity(){
		super();
		acceleration = new Acceleration();
		outsideAcceleration = new Acceleration();

	}
	/**
	 * initializes x and y components but not z
	 * @param a
	 * @param b
	 * @param p
	 */
	Velocity(double a,double b,GLPoint p){
		super(a,b,p);
		
		acceleration = new Acceleration(0,0,p);
		outsideAcceleration = new Acceleration();
	}
	Velocity(double a,double b,double z,GLPoint p){
		super(a,b,z,p);
		
		acceleration = new Acceleration(0,0,p);
		outsideAcceleration = new Acceleration(0,0,p);
	}
	
	/*
	 * Changes accelerations location as well.
	 */
	public void setLocation(double a,double b){
		location.setX(a);
		location.setY(b);
	}
	/*
	 * 
	 */
	public void setLocation(double x,double y,double z){
		location.setX(x);
		location.setY(y);
		location.setZ(z);
	}
	/*
	 * Changes accelerations location as well.
	 */
	public void setLocation(GLPoint location) {
		this.location = location;
		
		acceleration.setLocation(location);
	}
	
	/**
	 * sets internal acceleration in the x y plane
	 * @param m
	 * @param d
	 */
	public void setAccelerationMagDir(double m,double d){
		acceleration.xComponent = Math.cos(d)*m;
		acceleration.yComponent = Math.sin(d)*m;
	}
	/**
	 * gets the accelerations direction in the x y plane
	 * @return
	 */
	public double getAccelerationDirection(){
		double angle;
		
		if(acceleration.xComponent == 0){
			if(acceleration.yComponent < 0){
				angle = Math.PI + Math.PI/2;
			}
			else{
				angle = Math.PI/2;
			}
		}
		else{
			angle = Math.atan(acceleration.yComponent/acceleration.xComponent);
			
			if(acceleration.xComponent < 0){
				angle += Math.PI;
			}
		}
		
		return angle;
	}
	/*
	 * Generates the magnitude from the x and y components.
	 */
	public double getAccelerationMagnitude(){
		return Math.sqrt(acceleration.xComponent*acceleration.xComponent 
				+ acceleration.yComponent*acceleration.yComponent);
	}
	
	
	public double getAccelerationXComponent() {
		return acceleration.xComponent;
	}
	public void setAccelerationXComponent(double component) {
		acceleration.xComponent = component;
	}
	public double getAccelerationYComponent() {
		return acceleration.yComponent;
	}
	public void setAccelerationYComponent(double component) {
		acceleration.yComponent = component;
	}	
	public double getAccelerationZComponent() {
		return acceleration.zComponent;
	}
	public void setAccelerationZComponent(double component) {
		acceleration.zComponent = component;
	}	
	/**
	 * adds an additional acceleration to existing outside accelerations
	 * @param a
	 */
	public void addOutsideAcceleration(Acceleration a){
		outsideAcceleration.xComponent += a.xComponent;
		outsideAcceleration.yComponent += a.yComponent;
		outsideAcceleration.zComponent += a.zComponent;
	}
	/**
	 * clears accelerations that have been added to the external acceleration.
	 */
	public void clearOutsideAcceleration(){
		outsideAcceleration.xComponent = 0;
		outsideAcceleration.yComponent = 0;
		outsideAcceleration.zComponent = 0;
	}
	public double totalXAcceleration(){		
		return outsideAcceleration.xComponent + acceleration.xComponent;
	}
	public double totalYAcceleration(){
		return outsideAcceleration.yComponent + acceleration.yComponent;
	}
	public double totalZAcceleration(){
		return outsideAcceleration.zComponent + acceleration.zComponent;
	}
	/*
	 * Changes vectors position based on time.
	 * @param time in milliseconds.
	 */
	public void moveVector(double time){
		//convert to seconds
		
		time = time/1000.0d;
		
		double ax = totalXAcceleration();
		double ay = totalYAcceleration();
		double az = totalZAcceleration();
		
		xComponent = xComponent + ax*time;
		yComponent = yComponent + ay*time;
		zComponent = zComponent + az*time;
		//insure that the x,y velocity stays under the max velocity
		if(xComponent*xComponent + yComponent*yComponent >= PhysicsEngine.MAX_VELOCITY*PhysicsEngine.MAX_VELOCITY){
			double dir = getDirection();
			xComponent = PhysicsEngine.MAX_VELOCITY*Math.cos(dir);
			yComponent = PhysicsEngine.MAX_VELOCITY*Math.sin(dir); 
		}
		//insure that the z velocity statys under the max velocity
		if(zComponent >= PhysicsEngine.MAX_VELOCITY){
			zComponent = PhysicsEngine.MAX_VELOCITY;
		}

		setLocation(getLocation().getX() + time*xComponent,getLocation().getY() + time*yComponent,getLocation().getZ() + time*zComponent);
		
	}


	
}
