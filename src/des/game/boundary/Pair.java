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
package des.game.boundary;

import des.game.base.AllocationGuard;

public class Pair extends AllocationGuard{
	public double x;
	public double y;
	
	public Pair(){
		super();
		x=y=0;
	}
	public Pair(Pair other){
		super();
		x = other.x;
		y = other.y;
	}
	public Pair(double x,double y){
		super();
		this.x = x;
		this.y = y;
	}
	public static double dotProduct(double x1, double y1, double x2, double y2 ){
		return 0;
	}
	/**
	 * Calculates distance between two points in the x y plane
	 * @param other
	 * @return distance between other and this point.
	 */
	public double distance(Pair other){
		return Math.sqrt((x - other.x)*(x - other.x)+(y - other.y)*(y - other.y));
	}
	public double distance(double a, double b){
		return Math.sqrt((x - a)*(x - a)+(y - b)*(y - b));
	}
	public double dotProduct(Pair other){
		return x*other.x + y*other.y;
	}
	public double dotProduct(double x, double y){
		return x*this.x + y*this.y;
	}
	public void normalize() {
		double magnitude = Math.sqrt(x*x + y+y);
		x = x / magnitude;
		y = y / magnitude;
	}
}
