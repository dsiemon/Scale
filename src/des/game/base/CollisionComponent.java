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


import des.game.physics.CollisionBehavior;
import des.game.physics.PhysicsObject;
import des.game.physics.VectorObject;


public class CollisionComponent extends GameComponent implements CollisionBehavior{
	
	// this compoment wraps this particular physicsObject
	public PhysicsObject physicsObject;
	public GameObject parent;
	
	public CollisionComponent(){
		super();
		super.setPhase(GameComponent.ComponentPhases.POST_PHYSICS.ordinal());
	}
	public CollisionComponent(PhysicsObject physicsObject){
		super();
		super.setPhase(GameComponent.ComponentPhases.POST_PHYSICS.ordinal());
		
		this.setPhysicsObject(physicsObject, null);
	}
	@Override
	public void reset(){
		super.reset();
		this.physicsObject = null;
		this.parent = null;
	}
	
	@Override
    public void update(float timeDelta, BaseObject parent) {

		final GameObject gameObject = (GameObject)parent;
		
		Vector2 position = gameObject.getPosition();
		position.x = (float)this.physicsObject.getLocation().getX();
    	position.y = (float)this.physicsObject.getLocation().getY();
    	
    	if(this.physicsObject.getVector() != null){
	    	final VectorObject v = this.physicsObject.getVector();
	    	final Vector2 velocity = gameObject.velocity;
	    	velocity.x = (float)v.getVelocityXComponent();
	    	velocity.y = (float)v.getVelocityYComponent();
	    	
	    	gameObject.targetVelocity.x = velocity.x;
	    	gameObject.targetVelocity.y = velocity.y;
    	}
	}
	
	/**
	 * Will store collisions that happen then update the parent when update is called
	 * 
	 * @param the behavior of an object that was collided with, may be null
	 */
	@Override
	public void handleCollision(CollisionBehavior other) {
		
		
	}
	
	public void setPhysicsObject(PhysicsObject physicsObject, GameObject parent){
		this.physicsObject = physicsObject;
		this.parent = parent;
		physicsObject.collisionBehavior = this;
	}

}
