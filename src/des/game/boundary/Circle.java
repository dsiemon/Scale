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


public class Circle extends GameComponent{
	/**
	 * Default Constructor sets radius to zero.
	 */
	public Circle(){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS_BOUNDARY_PART.ordinal());
		radius = 1;
	}
	/**
	 * Sets center and radius, uses object reference passed in for center
	 * @param p
	 * @param r
	 */
	public Circle(GLPoint center,double radius){
		super();
		this.center = center;
		this.radius = radius;
	}
	
	public void initialize(GLPoint center,double radius){
		this.center = center;
		this.radius = radius;
	}
	
	/**
	 * Determines whether or not two circles intersect each other.
	 * @param other
	 * @return returns true when the circles intersect.
	 */
	public boolean collision(Circle other){
		double dx =(center.getX()-other.center.getX());
		double dy = (center.getY()-other.center.getY());
		double r = radius + other.radius;
		return dx*dx +  dy*dy < r*r;
	}
	public boolean collision(GLPoint other){
		double dx =(center.getX()-other.getX());
		double dy = (center.getY()-other.getY());
		return dx*dx + dy*dy < radius*radius;
	}
	public boolean collision(double x, double y){
		double dx =(center.getX()-x);
		double dy = (center.getY()-y);
		return dx*dx + dy*dy < radius*radius;
	}
	public boolean collision(double startX,double startY,double endX,double endY){
		return false;
	}
	public boolean collision(Rectangle other){
		boolean flag = false;
		double left = other.getLeft();
		double right = other.getRight();
		double top = other.getTop();
		double bottom = other.getBottom();
		
		double x = center.getX();
		double y = center.getY();
		if(x >= left && x <= right &&
		   y >= bottom && y <= top)
		{
			flag = true;
		}
		else if(x >= left && x <= right)
		{
			if(y <= bottom && collision(x,bottom))
			{
				flag = true;
			}
			else if(y >= top && collision(x,top))
			{
				flag = true;
			}
		}
		else if(y >= bottom &&  y <= top)
		{
			if(x <= left && collision(left,y))
			{
				flag = true;
			}
			else if(x >= right && collision(right,y))
			{
				flag = true;
			}			
		}
		else if(collision(left,top) || collision(left,bottom) || 
				collision(right,top) || collision(right,bottom))
		{
			flag = true;
		}
		return flag;
	}
	public GLPoint getCenter() {
		return center;
	}
	public void setCenter(GLPoint p) {
		center = p;
	}
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}	
	public void setRadius(float radius) {
		this.radius = radius;
	}	
	public double getNormal(Circle other){
		double x = other.center.getX() - center.getX();
		double y = other.center.getY() - center.getY();
		double angle;
		
		if(x == 0){
			if( y > 0){
				angle = Math.PI/2;
			}
			else{
				angle = 3*Math.PI/2;
			}
		}
		else{
			angle = Math.atan(y/x);
			if(x < 0){
				angle += Math.PI;
			}
		}
		
		return angle;
	}
	public double getNormal(GLPoint other){
		double angle;
		double x = center.getX() - other.getX();
		double y = center.getY() - other.getY();
		
		
		if(x == 0){
			if( y > 0){
				angle = Math.PI/2;
			}
			else{
				angle = 3*Math.PI/2;
			}
		}
		else{
			angle = Math.atan(y/x);
			if(x < 0){
				angle += Math.PI;
			}
		}
		
		return angle;
	}
	public double getNormal(double a,double b){
		double angle;
		double x = center.getX() - a;
		double y = center.getY() - b;
		
		
		if(x == 0){
			if( y > 0){
				angle = Math.PI/2;
			}
			else{
				angle = 3*Math.PI/2;
			}
		}
		else{
			angle = Math.atan(y/x);
			if(x < 0){
				angle += Math.PI;
			}
		}
		
		return angle;
	}
	public double getNormal(Rectangle other){
		
		double leftA = other.getLeft();
		double rightA = other.getRight();
		double topA = other.getTop();
		double bottomA = other.getBottom();
		double angle;
		
		double y = center.getY();
		double x = center.getX();
		if(y <= topA && y >=bottomA){
			if(x >= leftA + other.getWidth()/2){
				angle = 0;
			}
			else{
				angle = Math.PI;
			}
		}
		else if(x <= rightA && x >= leftA){
			if(y >= topA - other.getHeight()/2){
				angle = Math.PI/2;
			}
			else{
				angle = 3*Math.PI/2;
				
			}
		}
		else{
			if(y >= topA){
				if(x >= rightA){
					angle = getNormal(rightA,topA);
				}
				else{
					angle = getNormal(leftA,topA);
				}
			}
			else{
				if(x >= rightA){
					angle = getNormal(rightA,bottomA);
				}
				else{
					angle = getNormal(leftA,bottomA);
				}
			}
		}
				

		
		return angle;
	}
	private GLPoint center;
	public double radius;
	
}
