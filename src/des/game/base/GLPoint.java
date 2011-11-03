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
package des.game.base;


public class GLPoint extends AllocationGuard{
	public GLPoint(){
		super();
		x = y = z =0;
		
	}
	public GLPoint(double x, double y,double z){
		super();
		this.z = z;
		this.x = x;
		this.y = y;
	}
	public double getZ(){
		return z;
	}
	public double getY(){
		return y;
	}
	public double getX(){
		return x;
	}
	public void setZ(double z){
		this.z = z;
	}
	public void setY(double y){
		this.y = y;
	}
	public void setX(double x){
		this.x = x;
	}
	/**
	 * Calculates distance between two points in the x y plane
	 * @param other
	 * @return distance between other and this point.
	 */
	public double distance(GLPoint other){
		return Math.sqrt((x - other.x)*(x - other.x)+(y - other.y)*(y - other.y));
	}
	
	public double distanceSquared(GLPoint other){
		return (x - other.x)*(x - other.x)+(y - other.y)*(y - other.y);
	}
	
	private double z;
	private double x;
	private double y;
}
