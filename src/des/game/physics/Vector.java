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
 * represents a basic vector, stored in x,y components
 */
public class Vector {
	protected double xComponent;
	protected double yComponent;
	protected double zComponent;
	protected GLPoint location;
	/*
	 * Sets Components to zero.
	 */
	Vector(){
		xComponent = 0;
		yComponent = 0;	
		zComponent = 0;	
		location = new GLPoint();
	}
	/*
	 * Sets Components to a and b.
	 */
	Vector(double x,double y,GLPoint p){
		xComponent = x;
		yComponent = y;
		zComponent = 0;
		location = p;
	}
	/*
	 * Sets Components to a and b.
	 */
	Vector(double x,double y,double z,GLPoint p){
		xComponent = x;
		yComponent = y;
		zComponent = z;
		location = p;
	}
	/*
	 * sets x and y components based on polar coordinates.
	 */
	public void setMagDir(double m,double d){
		xComponent = Math.cos(d)*m;
		yComponent = Math.sin(d)*m;
	}
	/**
	 * gets direction of vector in x y plane
	 * @return
	 */
	public double getDirection(){
		double angle;
		
		if(xComponent == 0){
			if(yComponent < 0){
				angle = Math.PI + Math.PI/2;
			}
			else{
				angle = Math.PI/2;
			}
		}
		else{
			angle = Math.atan(yComponent/xComponent);
			
			if(xComponent < 0){
				angle += Math.PI;
			}
		}
		
		return angle;
	}
	/*
	 * Generates the magnitude from the x and y components.
	 */
	public double getMagnitude(){
		return Math.sqrt(xComponent*xComponent + yComponent*yComponent);
	}
	
	
	public double getXComponent() {
		return xComponent;
	}
	public void setXComponent(double component) {
		xComponent = component;
	}
	public double getYComponent() {
		return yComponent;
	}
	public void setYComponent(double component) {
		yComponent = component;
	}	
	public double getZComponent() {
		return zComponent;
	}
	public void setZComponent(double component) {
		zComponent = component;
	}	

	public GLPoint getLocation() {
		return location;
	}
	public void setLocation(double a,double b){
		location.setX(a);
		location.setY(b);
	}
	public void setLocation(GLPoint location) {
		this.location = location;
	}
	
	public void zero(){
		this.xComponent = this.yComponent = this.zComponent = 0.0d;
	}

}
