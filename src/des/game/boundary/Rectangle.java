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



public class Rectangle extends GameComponent{
	private GLPoint center;
	public double height;
	public double width;
	private ProjectionLine[] edges;
	private Pair[] points;
	public Rectangle(){
		super.setPhase(GameComponent.ComponentPhases.PHYSICS_BOUNDARY_PART.ordinal());
		height = 1;
		width = 1;
		this.allocateMemory();

	}
	public Rectangle(GLPoint c, double w,double h){
		center = c;
		height = h;
		width = w;
		this.allocateMemory();
		setUpProjections();
	}
	
	public void initialize(GLPoint c, double w,double h){
		center = c;
		height = h;
		width = w;

		setUpProjections();
	}
	public double getNormal(Rectangle other){
		
		double leftA,leftB;
		double rightA,rightB;
		double topA,topB;
		double bottomA,bottomB;
		double normal;
		leftA = getLeft() ;
		rightA = getRight();
		topA = getTop();
		bottomA = getBottom();
		
		leftB = other.getLeft() ;
		rightB = other.getRight();
		topB = other.getTop() ;
		bottomB = other.getBottom();
		
		double dX = rightA - leftB;
		double dY = topA - bottomB;
		
		if(dX > rightB - leftA){
			dX = rightB - leftA;
		}
		if(dY > topB - bottomA){
			dY = topB - bottomA;
		}
		
		if(dY > dX){
			if(rightA > rightB){
				normal = 0;
			}
			else{
				normal = Math.PI;
			}
		}
		else{
			if(topA > topB){
				normal = Math.PI/2;
			}
			else{
				normal = 3*Math.PI/2;
			}
		}
		
		return normal;
	}
	public boolean collision(Rectangle other){
		boolean flag = true;
		
		double leftA,leftB;
		double rightA,rightB;
		double topA,topB;
		double bottomA,bottomB;
		
		leftA = getLeft() ;
		rightA = getRight();
		topA = getTop();
		bottomA = getBottom();
		
		leftB = other.getLeft() ;
		rightB = other.getRight();
		topB = other.getTop() ;
		bottomB = other.getBottom();
		
	    if( bottomA >= topB )
	    {
	    	 flag = false;
	    }
	    else if( topA <= bottomB )
	    {
	    	 flag = false;
	    }
	    else if( rightA <= leftB )
	    {
	    	 flag = false;
	    }
	    else if( leftA >= rightB )
	    {
	        flag =  false;
	    }

		return flag;
	}
	public boolean collision(GLPoint other){
		boolean flag = true;
		double x = other.getX();
		double y = other.getY();
		
	    if(y <= getBottom() || y >= getTop() || x <= getLeft() || x >= getRight()){
	    	 flag = false;
	    }
		return flag;
	}

	public Pair[] getPoints(){
		return points;
	}
	public ProjectionLine[] getEdges(){
		return edges;
	}
	public GLPoint getCenter() {
		return center;
	}
	public void setCenterX(double x) {
		this.center.setX(x);
	}
	public void setCenterY(double y) {
		this.center.setY(y);
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
		setUpProjections();
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
		setUpProjections();
	}
	public void setHeightAndWidth(double height, double width){
		this.width = width;
		this.height = height;
		this.setUpProjections();
	}
	public double getLeft(){
		return center.getX();
	}
	public double getRight(){
		return center.getX() + width;
	}
	public double getTop(){
		return center.getY();
	}
	public double getBottom(){
		return center.getY() - height;
	}
	public void setCenter(GLPoint c){
		this.center = c;
	}
	private void allocateMemory(){
		edges = new ProjectionLine[4];
		points = new Pair[4];
		
		edges[0] = new ProjectionLine();
		edges[1] = new ProjectionLine();
		edges[2] = new ProjectionLine();
		edges[3] = new ProjectionLine();
		edges[0].edge = new Pair();
		edges[1].edge = new Pair();
		edges[2].edge = new Pair();
		edges[3].edge = new Pair();
		points[0] = new Pair();
		points[1] = new Pair();
		points[2] = new Pair();
		points[3] = new Pair();
	}
	private void setUpProjections(){
		points[0].x = 0; points[0].y = 0;
		points[1].x = width; points[1].y = 0;
		points[2].x = width; points[2].y = -height;
		points[3].x = 0; points[3].y = -height;

		Pair p1;
		Pair p2;
		for (int i = 0; i < 4; i++) {
			
			//set up the edge
			p1 = points[i];
			if (i + 1 >= points.length) {
				p2 = points[0];
			} else {
				p2 = points[i + 1];
			}
			
			edges[i].edge.x = -(p1.y - p2.y);
			edges[i].edge.y = p1.x - p2.x;

			//save the projections of each edge
			// To project a point on an axis use the dot product
			double d = edges[i].edge.x*(points[0].x) + edges[i].edge.y*(points[0].y);
			double min = d;//min
			double max = d;//max
			edges[i].min = points[0];
			edges[i].max = points[0];
			for (int j = 1; j < points.length; j++) {
				d = edges[i].edge.x*(points[j].x) + edges[i].edge.y*(points[j].y);
				if (d < min) {
					edges[i].min = points[j];
					min = d;
				} else {
					if (d > max) {
						edges[i].max = points[j];
						max = d;
					}
				}
			}
		}
	}
	
}


