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

import des.game.base.GLPoint;
import des.game.base.GameComponent;

/**
 * This class represents a 2D boundary that can take the shape of
 *  	a circle, rectangle, or convex polygon. rectangles have a 
 *  	performance advantage over polygons, and circles have one
 *  	over rectangles.
 *  
 *  These boundaries are capable of detecting intersections and of
 *  	calculating normals of collision.
 *
 */
public class Boundary extends GameComponent{
	 
	private Rectangle rectangle;
	private Circle circle;
	private Polygon polygon;
	protected BoundingBox xProjection;
	protected BoundingBox yProjection;
	protected BoundingBox zProjection;
	protected double height;
	
	public Boundary(){
		super();
		xProjection = new BoundingBox();
		yProjection = new BoundingBox();
		zProjection = new BoundingBox();
		height = 1;
	}
	/**
	 * creates a boundary with a polygon
	 * @param p
	 */
	public Boundary(Polygon p){
		super();
		polygon = p;
		circle = polygon.getBoundingCircle();
		xProjection = new BoundingBox();
		yProjection = new BoundingBox();
		zProjection = new BoundingBox();
		height = 1;
	}
	/**
	 * creates a boundary with a circle
	 * @param c
	 */
	public Boundary(Circle c){
		super();
		if(c == null){
			c = new Circle();
		}
		circle = c;

		xProjection = new BoundingBox();
		yProjection = new BoundingBox();
		zProjection = new BoundingBox();
		height = 1;
	}
	/**
	 * creates a boundary with a rectangle
	 * @param r
	 */
	public Boundary(Rectangle r){
		super();
		if(r == null){
			r = new Rectangle();
		}
		rectangle = r;
		xProjection = new BoundingBox();
		yProjection = new BoundingBox();
		zProjection = new BoundingBox();
		height = 1;
	}
	/**
	 * sets the x and y projections of the boundary based on its current location
	 */
	public void setBox(){
		if(polygon != null){
			xProjection.setStart(polygon.getMinX());
			xProjection.setEnd(polygon.getMaxX());
			yProjection.setStart(polygon.getMinY());
			yProjection.setEnd(polygon.getMaxY());
			zProjection.setStart(polygon.getCenter().getZ());
			zProjection.setStart(polygon.getCenter().getZ() + height);
		}
		else if(circle != null){
			xProjection.setStart(circle.getCenter().getX() - circle.getRadius());
			xProjection.setEnd(circle.getCenter().getX() + circle.getRadius());
			yProjection.setStart(circle.getCenter().getY() - circle.getRadius());
			yProjection.setEnd(circle.getCenter().getY() + circle.getRadius());
			zProjection.setStart(circle.getCenter().getZ());
			zProjection.setStart(circle.getCenter().getZ() + height);
		}
		else if(rectangle != null){
			xProjection.setStart(rectangle.getLeft());
			xProjection.setEnd(rectangle.getRight());
			yProjection.setStart(rectangle.getBottom());
			yProjection.setEnd(rectangle.getTop());
			zProjection.setStart(rectangle.getCenter().getZ());
			zProjection.setStart(rectangle.getCenter().getZ() + height);
		}
	}
	

	public Rectangle getRectangle() {
		return rectangle;
	}
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
		
		circle = null;
		polygon = null;
	}
	public Circle getCircle() {
		return circle;
	}
	public void setCircle(Circle circle) {
		this.circle = circle;
		polygon = null;
		rectangle = null;
	}
	public Polygon getPolygon(){
		return polygon;
	}
	public void setPolygon(Polygon polygon){
		this.polygon = polygon;
		circle = null;
		rectangle = null;
	}
	/**
	 * detects intersections(collisions) of boundaries
	 * @param other
	 * @return true when boundaries are intersecting
	 */
	public boolean collision(Boundary other){
		boolean flag = false;
		if(polygon != null){
			if(other.polygon != null){
				flag = polygon.collision(other.polygon);
			}
			else if(other.circle != null){
				flag = polygon.collision(other.circle);
			}
			else if(other.rectangle != null){
				flag = polygon.collision(other.rectangle);
			}
		}
		else if(circle != null){
			if(other.polygon != null){
				flag = other.polygon.collision(circle);
			}
			else if(other.circle != null){
				flag = circle.collision(other.circle);
			}
			else if(other.rectangle != null){
				flag = circle.collision(other.rectangle);
			}
		}
		else if(rectangle != null){
			if(other.polygon != null){
				flag = other.polygon.collision(rectangle);
			}
			else if(other.circle != null){
				flag = other.circle.collision(rectangle);
			}
			else if(other.rectangle != null){
				flag = rectangle.collision(other.rectangle);
			}	
		}
		return flag;
	}
	/**
	 * returns the normal of collision between two boundaries. the angle is 
	 * 	relative to the calling boundary
	 * @param other
	 * @return counter-clockwise angle in radians 
	 */
	public double getNormal(Boundary other){
		double normal = 0;
		if(polygon != null){
			if(other.polygon != null){
				normal = polygon.normal(other.polygon);
			}
			else if(other.circle != null){
				normal = polygon.normal(other.circle);
			}
			else if(other.rectangle != null){
				normal  = polygon.normal(other.rectangle);
			}
		}
		else if(circle != null){
			if(other.polygon != null){
				normal = other.polygon.normal(circle);
				//reverse direction to make angle relative to this object
				normal += Math.PI;
			}
			else if(other.circle != null){
				normal = circle.getNormal(other.circle);
			}
			else if(other.rectangle != null){
				normal = circle.getNormal(other.rectangle);
			}
		}
		else if(rectangle != null){
			if(other.polygon != null){
				normal = other.polygon.normal(rectangle);
				//reverse direction to make angle relative to this object
				normal += Math.PI;
			}
			else if(other.circle != null){
				normal = other.circle.getNormal(rectangle);
				//reverse direction to make angle relative to this object
				normal += Math.PI;
			}
			else if(other.rectangle != null){
				normal = rectangle.getNormal(other.rectangle);
			}	
		}
		
		return normal;
	}
	/**
	 * detects intersections(collisions) of boundaries
	 * @param other
	 * @return true when boundaries are intersecting
	 */
	public boolean collision(GLPoint other){
		boolean flag = false;
		if(polygon != null){
			flag = polygon.collision(other);
			
		}
		else if(circle != null){
			flag = circle.collision(other);

		}
		else if(rectangle != null){
			flag = rectangle.collision(other);
		}
		return flag;
	}
	public boolean collision(double startX,double startY,double endX,double endY){
		return false;
	}
	public BoundingBox getXProjection(){
		return xProjection;
	}
	public BoundingBox getYProjection(){
		return yProjection;
	}
	
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		if(height < 0){
			height = 1;
		}
		this.height = height;
	}
	
	/**
	 * two boundaries are coplanar if their z projections overlap, or if either has a height of 0
	 * @param other
	 * @return
	 */
	public boolean isCoplanar(Boundary other){
		boolean rtn = true;
		
//		if(this.height > 0 && other.height > 0){
//			rtn = this.zProjection.overlap(other.zProjection);
//		}
		
		return rtn;
	}
	public boolean isCoplanar(double other){
		boolean rtn = true;
		
		if(this.height > 0 ){
			rtn = this.zProjection.overlap(other);
		}
		
		return rtn;
	}
	public GLPoint getLocation(){
		GLPoint rtn = null;
		if(circle != null){
			rtn = circle.getCenter();
		}
		else if(rectangle != null){
			rtn = rectangle.getCenter();
		}
		else if(polygon != null){
			rtn = polygon.getCenter();
		}
		return rtn;
	}
	@Override
	public void reset() {
		circle = null;
		rectangle = null;
		polygon = null;
	}
}
