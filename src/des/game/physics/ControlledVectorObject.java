package des.game.physics;

import des.game.base.GLPoint;
/**
 * This class is an extension of the vector object class. It maintains an extra vector(cX,cY) that represent a "controlled" direction of the vector.
 * 
 * The totalVelocity is the sum of the controlled vector and the normal velocity vector. the Brake function will decrease the uncontrolled velocity.
 * @author Douglas
 *
 */
public class ControlledVectorObject extends VectorObject{
	
	protected Velocity totalVelocity;
	public double cX;
	public double cY;
	
	public ControlledVectorObject(){
		super();
		totalVelocity = new Velocity(0,0,location);
	}
	
	public void initialize(double m,GLPoint p, double xV,double yV){
		super.initialize(m, p, xV, yV);
		totalVelocity.location = p;
		cX = cY = 0d;
		
	}
	@Override 
	public void moveVector(double time){
		//if locked save the z coord and set it to the old value afterwards
		double tempZ = 0;
		if(locked){
			tempZ = location.getZ();
		}
		
		totalVelocity.moveVector(time);
		velocity.setXComponent(totalVelocity.getXComponent() - cX);
		velocity.setYComponent(totalVelocity.getYComponent() - cY);
		if(locked){
			location.setZ(tempZ);
		}
	}
	
	public void brakeVector(double time){
		double mag = velocity.getMagnitude();
		final double dir = velocity.getDirection();
		
		mag = mag - 900d*time;
		if(mag < 0){
			mag = 0;
		}
		velocity.setMagDir(mag, dir);
		
		
//		if(mag > 50d){
//			velocity.xComponent = velocity.xComponent - (velocity.xComponent*.85) * time;
//			velocity.yComponent = velocity.yComponent - (velocity.yComponent*.85) * time;
//		}
//		else if(mag > 25d){
//			final double dir = velocity.getDirection();
//			
//			velocity.setMagDir(mag - 40d*time, dir);
//		}
//		else{
//			velocity.xComponent = 0d;
//			velocity.yComponent = 0d;
//		}
		
		totalVelocity.setXComponent(velocity.getXComponent() + cX);
		totalVelocity.setYComponent(velocity.getYComponent() + cY);
	}
	
	public void setControlledComponents(double cX, double cY){
		this.cX = cX;
		this.cY = cY;
		
		totalVelocity.setXComponent(velocity.getXComponent() + cX);
		totalVelocity.setYComponent(velocity.getYComponent() + cY);
	}
	
	////////////////////////////velocity functions
	@Override 
	public void setVelocityMagDir(double m,double d){
		totalVelocity.setMagDir(m, d);
		velocity.setXComponent(totalVelocity.getXComponent() - cX);
		velocity.setYComponent(totalVelocity.getYComponent() - cY);
	}
	@Override 
	public void setVelocityXComponent(double x){
		totalVelocity.setXComponent(x);
		velocity.setXComponent(x - cX);
	}
	@Override 
	public double getVelocityXComponent(){
		return totalVelocity.getXComponent();
	}
	@Override 
	public void setVelocityYComponent(double y){
		totalVelocity.setYComponent(y);
		velocity.setYComponent(y - cY);
	}
	@Override 
	public double getVelocityYComponent(){
		return totalVelocity.getYComponent();
	}
	@Override 
	public double getVelocityDirection(){
		return totalVelocity.getDirection();
	}
	@Override 
	public double getVelocityMagnitude(){
		return totalVelocity.getMagnitude();
	}
	public double getUncontrolledX(){
		return velocity.getXComponent();
	}
	public double getUncontrolledY(){
		return velocity.getYComponent();
	}
	@Override
	public void reset() {
		super.reset();
		cX = cY = 0d;
		totalVelocity.zero();
		totalVelocity.acceleration.zero();
		totalVelocity.outsideAcceleration.zero();
		
	}
	
	public void setAccelerationMagDir(double m,double d){
		totalVelocity.setAccelerationMagDir(m, d);
	}
	public double getAccelerationDirection(){
		return totalVelocity.getAccelerationDirection();
	}
	/**
	 * Generates the magnitude from the x and y components.
	 */
	public double getAccelerationMagnitude(){
		return totalVelocity.getAccelerationMagnitude();
	}
	
	
	public double getAccelerationXComponent() {
		return totalVelocity.getAccelerationXComponent();
	}
	public void setAccelerationXComponent(double component) {
		totalVelocity.setAccelerationXComponent(component);
	}
	public double getAccelerationYComponent() {
		return totalVelocity.getAccelerationYComponent();
	}
	public void setAccelerationYComponent(double component) {
		totalVelocity.setAccelerationYComponent(component);
	}	
	public double getAccelerationZComponent() {
		return totalVelocity.getAccelerationZComponent();
	}
	public void setAccelerationZComponent(double component) {
		totalVelocity.setAccelerationZComponent(component);
	}	
	public void addOutsideAcceleration(Acceleration a){
		totalVelocity.addOutsideAcceleration(a);
	}
	public void addOutsideAcceleration(double x, double y){
		totalVelocity.addOutsideAcceleration(x,y);
	}

	public void clearOutsideAcceleration(){
		totalVelocity.clearOutsideAcceleration();
	}
	public double totalXAcceleration(){
		return totalVelocity.totalXAcceleration();
	}
	public double totalYAcceleration(){
		return totalVelocity.totalYAcceleration();
	}
	public double totalZAcceleration(){
		return totalVelocity.totalZAcceleration();
	}
}
