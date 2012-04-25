package des.game.base;


import des.game.physics.PhysicsObject;

public class FieldComponent extends GameComponent {
	public GLPoint location;
	
	public FieldComponent(){
		super.setPhase(GameComponent.ComponentPhases.POST_PHYSICS.ordinal());
	}
	
	@Override
	public void reset(){
		super.reset();
	}
	
	@Override
    public void update(float timeDelta, BaseObject parent) {
	
	}
	
	public boolean handleObject(PhysicsObject object, int time){
		
		return false;
	}
	
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

}
