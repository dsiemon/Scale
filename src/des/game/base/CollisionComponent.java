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

import android.util.Log;

import des.game.physics.CollisionBehavior;
import des.game.physics.PhysicsObject;
import des.game.physics.Velocity;

public class CollisionComponent extends GameComponent implements CollisionBehavior{
	
	// this compoment wraps this particular physicsObject
	public PhysicsObject physicsObject;
	public CollisionComponent(){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS.ordinal());
	}
	public CollisionComponent(PhysicsObject physicsObject){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS.ordinal());
		
		this.setPhysicsObject(physicsObject);
	}
	@Override
	public void reset(){
		super.reset();
	}
	
	@Override
    public void update(float timeDelta, BaseObject parent) {

		final GameObject gameObject = (GameObject)parent;
		
		Vector2 position = gameObject.getPosition();
		position.x = (float)this.physicsObject.getLocation().getX();
    	position.y = (float)this.physicsObject.getLocation().getY();
    	
    	if(this.physicsObject.getVector() != null){
	    	final Velocity v = this.physicsObject.getVector().getVelocity();
	    	final Vector2 velocity = gameObject.velocity;
	    	velocity.x = (float)v.getXComponent();
	    	velocity.y = (float)v.getYComponent();
	    	
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
	
	public void setPhysicsObject(PhysicsObject physicsObject){
		this.physicsObject = physicsObject;
		
		physicsObject.collisionBehavior = this;
	}

}
