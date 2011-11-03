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

public class Polygon  {
	public static final int MAX_VERT = 5;
	
	private Circle circle;
	private Pair[] points;
	private ProjectionLine[] edges;
	private GLPoint center;
	
	
	private double maxX;
	private double minX;
	
	private double maxY;
	private double minY;
	
	private Pair dummyA;
	private Pair dummyB;
	private Pair dummyC;
	/**
	 * default polygon
	 */
	public Polygon() {
		center = new GLPoint();
		Pair[] temp = {new Pair(0,0),new Pair(0,1),new Pair(1,0)};
		points = temp;
		
		setCircleAndBounds();

	}
	public Polygon(GLPoint center,Pair[] points){
		assert points.length <= Polygon.MAX_VERT : "Max polygon=5";
		
		if(center == null){
			center = new GLPoint();
		}
		this.center = center;
		
		if(points == null || points.length < 3 || points.length > Polygon.MAX_VERT){
			Pair[] temp = {new Pair(0,0),new Pair(0,1),new Pair(1,0)};
			points = temp;
		}
		this.points = points;
		
		setCircleAndBounds();
	}
	/**
	 * creates a triangle
	 * @param center location of polygon
	 * @param x0 coordinates of triangle relative to center
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public Polygon(GLPoint center,double x0, double y0,double x1, double y1,double x2, double y2){
		if(center == null){
			center = new GLPoint();
		}
		this.center = center;
		
		Pair[] temp = {new Pair(x0,y0),new Pair(x1,y1),new Pair(x2,y2)};
		points = temp;
		
		setCircleAndBounds();
	}
	/**
	 * creates a triangle
	 * @param center location of polygon
	 * @param x0 coordinates of triangle relative to center
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public Polygon(GLPoint center,double x0, double y0,double x1, double y1,double x2, double y2,double x3, double y3){
		if(center == null){
			center = new GLPoint();
		}
		this.center = center;
		
		Pair[] temp = {new Pair(x0,y0),new Pair(x1,y1),new Pair(x2,y2),new Pair(x3,y3)};
		points = temp;
		
		setCircleAndBounds();
	}
	public GLPoint getCenter(){
		return center;
	}
	public Pair[] getPoints(){
		return points;
	}
	public Circle getBoundingCircle(){
		return circle;
	}
	private void setCircleAndBounds(){
		dummyA = new Pair();
		dummyB = new Pair();
		dummyC = new Pair();
		double maxDistance = 0;
		
		boolean first = true;
		
		for(int i = 0; i < points.length; i++){
			if(first){
				first = false;
				minX = maxX = points[i].x;
				minY = maxY = points[i].y;
			}
			
			double temp = points[i].distance(0,0);
			if(temp > maxDistance){
				maxDistance = temp;
			}
			if(points[i].x < minX){
				minX = points[i].x;
			}
			else if(points[i].x > maxX){
				maxX = points[i].x;
			}
			
			if(points[i].y < minY){
				minY = points[i].y;
			}
			else if(points[i].y > maxY){
				maxY = points[i].y;
			}
		}
		
		circle = new Circle(center,maxDistance);
		setUpProjections();
		
	}
	private void setUpProjections(){
		
		edges = new ProjectionLine[points.length];
		Pair p1;
		Pair p2;
		for (int i = 0; i < points.length; i++) {
			edges[i] = new ProjectionLine();
			//set up the edge
			p1 = points[i];
			if (i + 1 >= points.length) {
				p2 = points[0];
			} else {
				p2 = points[i + 1];
			}
			edges[i].edge = new Pair(-(p1.y - p2.y),p1.x - p2.x );
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
	/**
	 * projects a polygon onto an arbitrary axis
	 * @param axis polygon is projected onto this
	 * @param polygon
	 * @param interval projectin is stored in this, x = min, y = max
	 */
	static private void projectPolygon(Pair axis, Polygon polygon, Pair interval) {
		// To project a point on an axis use the dot product
		double d = axis.x*(polygon.points[0].x + polygon.center.getX()) + axis.y*(polygon.points[0].y + polygon.center.getY());
		interval.x = d;//min
		interval.y = d;//max
		for (int i = 1; i < polygon.points.length; i++) {
			d = axis.x*(polygon.points[i].x + polygon.center.getX()) + axis.y*(polygon.points[i].y + polygon.center.getY());
			if (d < interval.x) {
				interval.x = d;
			} else {
				if (d > interval.y) {
					interval.y = d;
				}
			}
		}
	}
	static private void projectRectangle(Pair axis, Rectangle rectangle, Pair interval) {
		// To project a point on an axis use the dot product
		Pair[] points = rectangle.getPoints();
		double d = axis.x*(points[0].x + rectangle.getCenter().getX()) + axis.y*(points[0].y +rectangle.getCenter().getY());
		interval.x = d;//min
		interval.y = d;//max
		for (int i = 1; i < points.length; i++) {
			d = axis.x*(points[i].x + rectangle.getCenter().getX()) + axis.y*(points[i].y + rectangle.getCenter().getY());
			if (d < interval.x) {
				interval.x = d;
			} else {
				if (d > interval.y) {
					interval.y = d;
				}
			}
		}
	}
	static private void projectCircle(Pair axis, Circle circle, Pair interval) {
		double x = circle.getCenter().getX();
		double y = circle.getCenter().getY();
		double r = circle.getRadius();
		// To project a point on an axis use the dot product
		double minx;
		double miny;
		double maxx; 
		double maxy;
		double slope = 0;
		if(axis.x != 0){
			 slope = axis.y/axis.x;
			minx = r*Math.sqrt(1/(1+slope*slope));
			miny = slope*minx;
			maxx = -minx;
			maxy = -miny;
		}
		else{
			miny = r;
			maxx = minx = 0;
			maxy = -miny;
			
		}
		minx = minx + x;
		miny = miny + y;
		maxx = maxx + x;
		maxy = maxy + y;

		interval.x = axis.x*(minx) + axis.y*(miny);//min
		interval.y = axis.x*(maxx) + axis.y*(maxy);//max
		
		if(interval.x > interval.y){
			double temp = interval.x;
			interval.x = interval.y;
			interval.y = temp;
			
		}
	}
	public double normal(Polygon other){
	
		boolean firstTime = true;
		double minAx = points[0].x;
		double minAy = points[0].y;
		double minBx = points[0].x;
		double minBy = points[0].y;

		double minDistance = 0;
		//check this polygons points against the other's edges
		//find the point  that is closes to an edge
		for(int i = 0; i < points.length; i++){

			double cx = points[i].x + center.getX();
			double cy = points[i].y + center.getY();
			
			for(int j = 0; j < other.points.length; j++){
				//for each edge
	
				double ax = other.points[j].x + other.center.getX();
				double ay = other.points[j].y + other.center.getY();

				double bx = other.points[(j+1)%other.points.length].x + other.center.getX();
				double by = other.points[(j+1)%other.points.length].y + other.center.getY();
				

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		            //double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(firstTime || distanceSeg < minDistance){

		            	minAx = cx;
		            	minAy = cy;
		            	minBx = px;
		            	minBy = py;
		            	minDistance = distanceSeg;
		            	firstTime = false;
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(firstTime || dista < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = ax;
		            	minBy = ay;
		           
		            	minDistance = dista;
		            	firstTime = false;
		            }
		            if(firstTime || distb < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = bx;
		            	minBy = by;
		            	minDistance = distb;
		            	firstTime = false;
		            }
		        }
			}
		}
		//check the other polygon's points against this one's edges
		for(int i = 0; i < other.points.length; i++){
			double cx = other.points[i].x + other.center.getX();
			double cy = other.points[i].y + other.center.getY();
			
			for(int j = 0; j < points.length; j++){
				double ax = points[j].x + center.getX();
				double ay = points[j].y + center.getY();
				double bx = points[(j+1)%points.length].x + center.getX();
				double by = points[(j+1)%points.length].y + center.getY();
				

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
	            //if the point is in between the ends of the segment
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(distanceSeg < minDistance){

		            	minAx = px;
		            	minAy = py;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distanceSeg;
		            
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(dista < minDistance){
		            	minAx = ax;
		            	minAy = ay;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = dista;
		            }
		            if(distb < minDistance){
		      
		            	minAx = bx;
		            	minAy = by;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distb;
		            }
		        }
			}
		}
		
		//calculate normal from mina and minb which are the two points of the polygons that are closest
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;

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
	public double normal(Circle other){
		
		boolean firstTime = true;
		double minAx = points[0].x;
		double minAy = points[0].y;
		double minBx = points[0].x;
		double minBy = points[0].y;

		double minDistance = 0;
		

		//check the circle's center against this one's edges
		double cx = other.getCenter().getX();
		double cy = other.getCenter().getY();

		for(int j = 0; j < points.length; j++){
			double ax = points[j].x + center.getX();
			double ay = points[j].y + center.getY();
			double bx = points[(j+1)%points.length].x + center.getX();
			double by = points[(j+1)%points.length].y + center.getY();
			

	        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
	        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
	        double r = r_numerator / r_denomenator;
            double px;
            double py;
	        if(r >= 0 && r <= 1){
	            px = ax + r*(bx-ax);
	            py = ay + r*(by-ay);
	        //   
	        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

	            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
	            if(firstTime || distanceSeg < minDistance){
	            	firstTime = false;
	            	minAx = px;
	            	minAy = py;
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = distanceSeg;
	            
	            }
	        }
	        else{
                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
	            if(firstTime || dista < minDistance){
	            	minAx = ax;
	            	minAy = ay;
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = dista;
	            	firstTime = false;
	            }
	            if(firstTime || distb < minDistance){
	            	minAx = bx;
	            	minAy = by;
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = distb;
	            	firstTime = false;
	            }
	        }
		}
		
		
		//calculate normal from mina and minb
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;

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
	public double normal(Rectangle other){
		
		boolean firstTime = true;
		double minAx = points[0].x;
		double minAy = points[0].y;
		double minBx = points[0].x;
		double minBy = points[0].y;

		double minDistance = 0;
		Pair[] rectPoints = other.getPoints();
		//check this polygons points against the other's edges
		for(int i = 0; i < points.length; i++){
			double cx = points[i].x + center.getX();
			double cy = points[i].y + center.getY();
		
			for(int j = 0; j < rectPoints.length; j++){
				double ax = rectPoints[j].x + other.getCenter().getX();
				double ay = rectPoints[j].y + other.getCenter().getY();
				double bx = rectPoints[(j+1)%rectPoints.length].x + other.getCenter().getX();
				double by = rectPoints[(j+1)%rectPoints.length].y + other.getCenter().getY();


		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		            //double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(firstTime || distanceSeg < minDistance){

		            	minAx = cx;
		            	minAy = cy;
		            	minBx = px;
		            	minBy = py;
		 
		            	minDistance = distanceSeg;
		            	firstTime = false;

		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(firstTime || dista < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = ax;
		            	minBy = ay;
		            	minDistance = dista;
		            	firstTime = false;
		            }
		            if(firstTime || distb < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = bx;
		            	minBy = by;
		            	
		            	minDistance = distb;
		            	firstTime = false;
		            }
		        }
			}
		}
		//check the other polygon's points against this one's edges
		for(int i = 0; i < rectPoints.length; i++){
			double cx = rectPoints[i].x + other.getCenter().getX();
			double cy = rectPoints[i].y + other.getCenter().getY();
			
			for(int j = 0; j < points.length; j++){
				double ax = points[j].x + center.getX();
				double ay = points[j].y + center.getY();
				double bx = points[(j+1)%points.length].x + center.getX();
				double by = points[(j+1)%points.length].y + center.getY();

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(distanceSeg < minDistance){

		            	minAx = px;
		            	minAy = py;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distanceSeg;
		            
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(dista < minDistance){
		            	minAx = ax;
		            	minAy = ay;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = dista;
		            }
		            if(distb < minDistance){
		            	minAx = bx;
		            	minAy = by;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distb;
		            }
		        }
			}
		}

		//calculate normal from mina and minb
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;
		
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
	public boolean collision(Polygon other){
		boolean flag = false;
		if(circle.collision(other.circle)){
			flag = true;

			int edgeCountA = edges.length;
			int edgeCountB = other.edges.length;
			//float minIntervalDistance = float.PositiveInfinity;
			//Vector translationAxis = new Vector();
			Pair edge;
			Pair intA = this.dummyA;

			Pair intB = this.dummyB;


			// Loop through all the edges of both polygons
			for (int edgeIndex = 0; edgeIndex < edgeCountA && flag; edgeIndex++) {
				
				edge = edges[edgeIndex].edge;

				//TODO optimize
				intA.x = edges[edgeIndex].edge.x*(edges[edgeIndex].min.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].min.y+center.getY());
				intA.y = edges[edgeIndex].edge.x*(edges[edgeIndex].max.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].max.y+center.getY());
				projectPolygon(edge, other, intB);

				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA, intB) >= 0){
					flag = false;
				}
			}
			// Loop through all the edges of both polygons
			for (int edgeIndex = 0; edgeIndex < edgeCountB && flag; edgeIndex++) {

					edge = other.edges[edgeIndex ].edge;
				

				
				projectPolygon(edge, this, intA);
				intB.x = other.edges[edgeIndex].edge.x*(other.edges[edgeIndex].min.x+other.center.getX()) + other.edges[edgeIndex].edge.y*(other.edges[edgeIndex].min.y+other.center.getY());
				intB.y = other.edges[edgeIndex].edge.x*(other.edges[edgeIndex].max.x+other.center.getX()) + other.edges[edgeIndex].edge.y*(other.edges[edgeIndex].max.y+other.center.getY());

				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA,intB) >= 0){
					flag = false;
				}
			}
		}
		return flag;
	}
		// Calculate the distance between [minA, maxA] and [minB, maxB]
		// The distance will be negative if the intervals overlap
	private double intervalDistance(Pair intA, Pair intB) {
		if (intA.x < intB.x) {
			return intB.x - intA.y;
		} else {
			return intA.x - intB.y;
		}
	}

	public boolean collision(Circle other){
		boolean flag = false;
		if(circle.collision(other)){
			flag = true;

			int edgeCountA = edges.length;
			
			//float minIntervalDistance = float.PositiveInfinity;
			//Vector translationAxis = new Vector();
			Pair edge;
			Pair intA = dummyA;
			Pair intB = dummyB;
			// Loop through all the edges of both polygons
			for (int edgeIndex = 0; edgeIndex < edgeCountA && flag; edgeIndex++) {
				
				edge = edges[edgeIndex].edge;

				//TODO optimize
				intA.x = edges[edgeIndex].edge.x*(edges[edgeIndex].min.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].min.y+center.getY());
				intA.y = edges[edgeIndex].edge.x*(edges[edgeIndex].max.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].max.y+center.getY());
				projectCircle(edge, other, intB);

				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA, intB) >= 0){
					flag = false;
					
				}
			}
			//check the circles edge
			if(flag){
				double x = other.getCenter().getX();
				double y = other.getCenter().getY();
				double dx = x - (points[0].x + center.getX());
				double dy = y - (points[0].y + center.getY());
				double minDist = dx*dx + dy*dy;
				Pair minPair = points[0];

				for(int i = 1; i < points.length; i++){
					dx = x - (points[i].x + center.getX());
					dy = y - (points[i].y + center.getY());
					double temp = dx*dx + dy*dy;
					
					if(temp < minDist){
						minDist = temp;
						minPair = points[i];
					}
			}
				
				edge = dummyC;
				edge.x = x - (minPair.x  + center.getX());
				edge.y = y - (minPair.y  + center.getY());
				
				projectCircle(edge, other, intB);
				projectPolygon(edge,this,intA);
				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA, intB) >= 0){
					flag = false;
				
				}
			}
		}
		return flag;
	}
	public boolean collision(Rectangle other){
		boolean flag = false;
		if(circle.collision(other)){
			flag = true;

			int edgeCountA = edges.length;
			
			//float minIntervalDistance = float.PositiveInfinity;
			//Vector translationAxis = new Vector();
			Pair edge;
			Pair intA = dummyA;
			Pair intB = dummyB;
			// Loop through all the edges of both polygons
			for (int edgeIndex = 0; edgeIndex < edgeCountA && flag; edgeIndex++) {
				
				edge = edges[edgeIndex].edge;

				//TODO optimize
				intA.x = edges[edgeIndex].edge.x*(edges[edgeIndex].min.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].min.y+center.getY());
				intA.y = edges[edgeIndex].edge.x*(edges[edgeIndex].max.x+center.getX()) + edges[edgeIndex].edge.y*(edges[edgeIndex].max.y+center.getY());
				projectRectangle(edge, other, intB);

				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA, intB) >= 0){
					flag = false;
				}
			}
			ProjectionLine[] otherEdges = other.getEdges();
			// Loop through all the edges of both polygons
			for (int edgeIndex = 0; edgeIndex < 4 && flag; edgeIndex++) {
					
					edge = otherEdges[edgeIndex ].edge;
				

				
				projectPolygon(edge, this, intA);
				intB.x = otherEdges[edgeIndex].edge.x*(otherEdges[edgeIndex].min.x+other.getCenter().getX()) + otherEdges[edgeIndex].edge.y*(otherEdges[edgeIndex].min.y+other.getCenter().getY());
				intB.y = otherEdges[edgeIndex].edge.x*(otherEdges[edgeIndex].max.x+other.getCenter().getX()) + otherEdges[edgeIndex].edge.y*(otherEdges[edgeIndex].max.y+other.getCenter().getY());

				// Check if the polygon projections are currentlty intersecting
				if (intervalDistance(intA, intB) >= 0){
					flag = false;
				}
			}
		}
		return flag;
	}

	///////////////////////////////////////////
	public double getMaxX() {
		return maxX + center.getX();
	}
	public double getMinX() {
		return minX + center.getX();
	}

	public double getMaxY() {
		return maxY + center.getY();
	}

	public double getMinY() {
		return minY + center.getY();
	}

	public boolean collision(GLPoint other){
	   double y = other.getY();
	   double x = other.getX();
	   
	   double angle=0;
	   Pair p1 = dummyA,p2 = dummyB;

	   for (int i=0;i<points.length;i++) {
	      p1.x = points[i].x + center.getX()- x;
	      p1.y = points[i].y + center.getY() - y;
	      p2.x = points[(i+1)%points.length].x + center.getX() - x;
	      p2.y = points[(i+1)%points.length].y + center.getY() - y;
	      angle += Angle2D(p1.x,p1.y,p2.x,p2.y);
	   }
	   return Math.abs(angle) == 2*Math.PI;
	}

	/*
	   Return the angle between two vectors on a plane
	   The angle is from vector 1 to vector 2, positive anticlockwise
	   The result is between -pi -> pi
	*/
	private double Angle2D(double x1, double y1, double x2, double y2){
	   double dtheta,theta1,theta2;

	   theta1 = Math.atan2(y1,x1);
	   theta2 = Math.atan2(y2,x2);
	   dtheta = theta2 - theta1;
	   while (dtheta > Math.PI)
	      dtheta -= Math.PI*2;
	   while (dtheta < -Math.PI)
	      dtheta += Math.PI*2;

	   return dtheta;
	}
	
	public static void expunge(Polygon p1, Polygon p2, Pair rtnValue){
		boolean firstTime = true;
		double minAx = p1.points[0].x;
		double minAy = p1.points[0].y;
		double minBx = p1.points[0].x;
		double minBy = p1.points[0].y;
		double minDistance = 0;
		//check this polygons points against the other's edges
		//find the point  that is closes to an edge
		for(int i = 0; i < p1.points.length; i++){
			double cx =p1.points[i].x + p1.center.getX() ;
			double cy = p1.points[i].y + p1.center.getY();
			
			for(int j = 0; j < p2.points.length; j++){
				//for each edge
				double ax = p2.points[j].x + p2.center.getX();
				double ay = p2.points[j].y + p2.center.getY();
				double bx = p2.points[(j+1)%p2.points.length].x + p2.center.getX();
				double by = p2.points[(j+1)%p2.points.length].y + p2.center.getY();
				
				

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		            //double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(firstTime || distanceSeg < minDistance){

		            	minAx = cx;
		            	minAy = cy;
		            	minBx = px;
		            	minBy = py;
		            	
		            	minDistance = distanceSeg;
		            	firstTime = false;
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(firstTime || dista < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBy = ay;
		            	minBx = ax;
		            	minDistance = dista;
		            	firstTime = false;
		            }
		            if(firstTime || distb < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = bx;
		            	minBy = by;
		            	minDistance = distb;
		            	firstTime = false;
		            }
		        }
			}
		}
		//check the other polygon's points against this one's edges
		for(int i = 0; i < p2.points.length; i++){
			double cx = p2.points[i].x + p2.center.getX();
			double cy = p2.points[i].y + p2.center.getY();
			
			for(int j = 0; j < p1.points.length; j++){
				double ax = p1.points[j].x + p1.center.getX();
				double ay =p1.points[j].y + p1.center.getY() ;
				double bx = p1.points[(j+1)%p1.points.length].x + p1.center.getX();
				double by = p1.points[(j+1)%p1.points.length].y + p1.center.getY();
				

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
	            //if the point is in between the ends of the segment
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(distanceSeg < minDistance){

		            	minAx = px;
		            	minAy = py;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distanceSeg;
		            
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(dista < minDistance){
		            	minAx = ax;
		            	minAy = ay;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = dista;
		            }
		            if(distb < minDistance){
		            	minAx = bx;
		            	minAy = by;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distb;
		            }
		        }
			}
		}
		
		//calculate normal from mina and minb which are the two points of the polygons that are closest
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;

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
		
		rtnValue.x = angle;
		rtnValue.y = Math.sqrt(minDistance);
		
	}
	public static void expunge(Polygon p1, Circle other, Pair rtnValue){
		
		boolean firstTime = true;
		double minAx = p1.points[0].x;
		double minAy = p1.points[0].y;
		double minBx = p1.points[0].x;
		double minBy = p1.points[0].y;
		double minDistance = 0;
		

		//check the circle's center against this one's edges
		double cx = other.getCenter().getX();
		double cy = other.getCenter().getY();
		
		for(int j = 0; j < p1.points.length; j++){
			double ax = p1.points[j].x + p1.center.getX();
			double ay =p1.points[j].y + p1.center.getY() ;
			double bx = p1.points[(j+1)%p1.points.length].x + p1.center.getX();
			double by = p1.points[(j+1)%p1.points.length].y + p1.center.getY();

			

	        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
	        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
	        double r = r_numerator / r_denomenator;
            double px;
            double py;
	        if(r >= 0 && r <= 1){
	            px = ax + r*(bx-ax);
	            py = ay + r*(by-ay);
	        //   
	        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

	            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
	            if(firstTime || distanceSeg < minDistance){
	            	firstTime = false;
	            	minAx = px;
	            	minAy = py;
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = distanceSeg;
	            
	            }
	        }
	        else{
                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
	            if(firstTime || dista < minDistance){
	            	minAx = ax;
	            	minAy = ay;
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = dista;
	            	firstTime = false;
	            }
	            if(firstTime || distb < minDistance){
	            	minAx = bx;
	            	minAy = by;
	            	
	            	minBx = cx;
	            	minBy = cy;
	            	minDistance = distb;
	            	firstTime = false;
	            }
	        }
		}
		
		
		//calculate normal from mina and minb
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;

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
		rtnValue.x = angle;
		rtnValue.y = other.getRadius() - Math.sqrt(minDistance);
	}
	public static void expunge(Polygon p1,Rectangle other, Pair rtnValue){
		boolean firstTime = true;
		double minAx = p1.points[0].x;
		double minAy = p1.points[0].y;
		double minBx = p1.points[0].x;
		double minBy = p1.points[0].y;
		double minDistance = 0;
		Pair[] rectPoints = other.getPoints();
		//check this polygons points against the other's edges
		for(int i = 0; i < p1.points.length; i++){
			double cx = p1.points[i].x + p1.center.getX();
			double cy = p1.points[i].y + p1.center.getY();
			for(int j = 0; j < rectPoints.length; j++){
				double ax = rectPoints[j].x + other.getCenter().getX();
				double ay = rectPoints[j].y + other.getCenter().getY();
				double bx = rectPoints[(j+1)%rectPoints.length].x + other.getCenter().getX();
				double by = rectPoints[(j+1)%rectPoints.length].y + other.getCenter().getY();
				

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		            //double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(firstTime || distanceSeg < minDistance){

		            	minAx = cx;
		            	minAy = cy;
		            	minBx = px;
		            	minBy = py;

		            	minDistance = distanceSeg;
		            	firstTime = false;

		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(firstTime || dista < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = ax;
		            	minBy = ay;
		            	minDistance = dista;
		            	firstTime = false;
		            }
		            if(firstTime || distb < minDistance){
		            	minAx = cx;
		            	minAy = cy;
		            	minBx = bx;
		            	minBy = by;
		            	minDistance = distb;
		            	firstTime = false;
		            }
		        }
			}
		}
		//check the other polygon's points against this one's edges
		for(int i = 0; i < rectPoints.length; i++){
			double cx = rectPoints[i].x + other.getCenter().getX();
			double cy = rectPoints[i].y + other.getCenter().getY();
			
			for(int j = 0; j < p1.points.length; j++){
				double ax = p1.points[j].x + p1.center.getX();
				double ay = p1.points[j].y + p1.center.getY();
				double bx = p1.points[(j+1)%p1.points.length].x + p1.center.getX();
				double by = p1.points[(j+1)%p1.points.length].y + p1.center.getY();

		        double r_numerator = (cx-ax)*(bx-ax) + (cy-ay)*(by-ay);
		        double r_denomenator = (bx-ax)*(bx-ax) + (by-ay)*(by-ay);
		        double r = r_numerator / r_denomenator;
	            double px;
	            double py;
		        if(r >= 0 && r <= 1){
		            px = ax + r*(bx-ax);
		            py = ay + r*(by-ay);
		        //   
		        //    double s =  ((a.y-c.y)*(b.x-a.x)-(a.x-c.x)*(b.y-a.y) ) / r_denomenator;

		            double distanceSeg = (px - cx)*(px - cx) + (py - cy)*(py - cy);
		            if(distanceSeg < minDistance){

		            	minAx = px;
		            	minAy = py;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distanceSeg;
		            
		            }
		        }
		        else{
	                double dista = (cx-ax)*(cx-ax) + (cy-ay)*(cy-ay);
	                double distb = (cx-bx)*(cx-bx) + (cy-by)*(cy-by);
		            if(dista < minDistance){
		            	minAx = ax;
		            	minAy = ay;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = dista;
		            }
		            if(distb < minDistance){
		            	minAx = bx;
		            	minAy = by;
		            	minBx = cx;
		            	minBy = cy;
		            	minDistance = distb;
		            }
		        }
			}
		}
		//calculate normal from mina and minb
//////////////////////////////////////
		double x = minBx - minAx;
		double y = minBy - minAy;
		
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
		rtnValue.x = angle;
		rtnValue.y = minDistance;
	}
}
