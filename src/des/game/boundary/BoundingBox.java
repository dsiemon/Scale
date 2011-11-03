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
/**
 * This class represents a projection of a polygon onto the x or y axis
 * @author Doug
 *
 */
public class BoundingBox implements Comparable<BoundingBox>{
	/**
	 * Compares boxes based on start
	 */
	public int compareTo(BoundingBox other){
		if(start < other.start)      return -1;
		else if(start > other.start) return 1;
		else                         return 0;
	}
	/**
	 * @param other is compared to this boundingBox.
	 * @return true when this start is less than other.start.
	 */
	public boolean lessThan(BoundingBox other){
		return start < other.start;
	}
	/**
	 * 
	 * @return value of start
	 */
	public double getStart() {
		return start;
	}
	/**
	 * sets value of start.
	 * @param start new value of start
	 */
	public void setStart(double start) {
		this.start = start;
	}
	/**
	 * 
	 * @return value of end
	 */
	public double getEnd() {
		return end;
	}
	/**
	 * sets value of end
	 * @param end new value of end
	 */
	public void setEnd(double end) {
		this.end = end;
	}
	/** 
	 * compares BoundingBoxes to test for overlap
	 * @param other compared to this BoundingBox
	 * @return true when either start or end is between the points
	 *         of the other box.
	 */
	public boolean overlap(BoundingBox other){
		boolean flag = false;
		if(start < other.start){
			if(other.start <= end){
				flag = true;
			}
		}
		else if(start <= other.end){
			
			flag = true;
			
		}
		return flag;
	}
	
	public boolean overlap(double other){
		return other <= end && other >= start;
	}
	private double start;
	private double end;
}
