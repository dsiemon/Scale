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

/**
 * This class defines the behavior that a field will have.
 *Each behavior must at least define a location and an area of
 * effect
 */
public abstract class FieldBehavior {
	protected GLPoint location;
	protected Boundary area;

	/**
	 * sets the location of the field
	 * @return
	 */
	public GLPoint getLocation() {
		return location;
	}
/**
 * sets the location of the field
 * @param location
 */
	public void setLocation(GLPoint location) {
		if(location != null){
			this.location = location;
		}
	}
/**
 * gets the area that the field affects
 * @return the boundary that defines the area of the field
 */
	public Boundary getArea() {
		return area;
	}
/**
 * sets the area that the field will affect
 * @param area a boundary that defines the region that the field affects
 */
	public void setArea(Boundary area) {
		if(area != null){
			this.area = area;
		}
	}
/**
 * defines how the fieldbehavior will effect an object
 * @param object
 * @return
 */
	public double getRadius(){
		double radius = 0;
		if(area.getCircle()!=null){
			radius = area.getCircle().getRadius();
		}
		else if(area.getRectangle()!= null){
			radius = (area.getRectangle().getWidth()+area.getRectangle().getHeight())/4;
		}
		return radius;
	}
	public abstract boolean handleObject(PhysicsObject object);
	public abstract boolean isAttractive();
	public abstract void setMagnitude(double m);
	public abstract double getMagnitude();
}
