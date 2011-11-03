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
import des.game.boundary.Boundary;

public class ConstantRate extends FieldBehavior{
	private double magnitude;
	/**
	 * create a new linear 
	 * @param magnitude
	 * @param area
	 * @param location
	 */
	public ConstantRate(double magnitude, Boundary area,GLPoint location){
		this.magnitude = magnitude;
		this.area = area;
		this.location = location;
	}
	/**
	 * 
	 */
	public boolean handleObject(PhysicsObject object){
		boolean flag = false;
		if(object.getBoundary() != null){
			flag = area.collision(object.getBoundary());
		}
		else{
			flag = area.collision(object.getVector().getLocation());
		}
		
		if(flag){
			double fx = location.getX();
			double fy = location.getY();
			double vx = object.getVector().getLocation().getX();
			double vy = object.getVector().getLocation().getY();
	
		 
		 	double angle;
			double xComponent = fx-vx;
			double yComponent = fy-vy  ;
			Acceleration a = new Acceleration();
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
			a.setMagDir(magnitude, angle);
			object.getVector().addOutsideAcceleration(a);
			 
		}
		return flag;
	}
	public boolean isAttractive(){
		return magnitude > 0;
	}
	public void setMagnitude(double m){
		magnitude = m;
	}
	public double getMagnitude(){
		return magnitude;
	}

}
