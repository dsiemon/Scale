/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package des.game.base;

import des.game.physics.PhysicsObject;



/**
 * GameObject defines any object that resides in the game world (character, background, special
 * effect, enemy, etc).  It is a collection of GameComponents which implement its behavior;
 * GameObjects themselves have no intrinsic behavior.  GameObjects are also "bags of data" that
 * components can use to share state (direct component-to-component communication is discouraged).
 */
public class GameObject extends PhasedObjectManager {

    // These fields are managed by components.
    
    //these 3 fields are cached from the physics object
    public Vector2 mPosition;
    
    /**
     * This value represents the objects actual velocity 
     */
    public Vector2 velocity;
    /**
     * This value is the objects desired velocity as well as it's orientation
     */
    public Vector2 targetVelocity;   
    
    
    public boolean destroyOnDeactivation;
    public boolean haltOnDeactivation;
    
    
    public float width;
    public float height;
    
    
    public enum ActionType {
        INVALID,
        IDLE,
        MOVE,
        ATTACK,
        HIT_REACT,
        DEATH,
        FROZEN
    }
    
    public enum Direction {
        INVALID,
        NONE,
        LEFT,
        DOWN,
        RIGHT,
        UP,
        MULTI;
    }
    
    private int mCurrentAction;
    private int mCurrentState;
    
    public enum Team {
        NONE,
        PLAYER,
        ENEMY
    }
    
    public int team;
    
    
    public PhysicsObject physcisObject;
    public GameObjectAttributes attributes;
    
    public GameObject() {
        super();

        mPosition = new Vector2();
        
        velocity = new Vector2(0, 0);
        targetVelocity = new Vector2(1,1);
        reset();
    }
    
    @Override
    public void update(float timeDelta, BaseObject parent){
    	//DebugLog.d("gameobject", "game object update");
    	
    	super.update(timeDelta, parent);
    }
    
    @Override
    public void reset() {
        removeAll();
        commitUpdates();
        
        mPosition.zero();

        velocity.set(1.0f, 1.0f);
        targetVelocity.set(1.0f, 1.0f);
        mCurrentAction = -1;
        mCurrentState = -1;
        destroyOnDeactivation = false;
        haltOnDeactivation = true;

        team = -1;
        width = 0.0f;
        height = 0.0f;

        attributes = null;
        
        physcisObject = null;

    }
    
    // Utility functions
    public void haltMotion(){
    	this.physcisObject.haltMotion();
    }

    public final Vector2 getPosition() {
        return mPosition;
    }

    public final void setPosition(Vector2 position) {
        mPosition.set(position);
    }
    
    public final float getCenteredPositionX() {
        return mPosition.x + (width / 2.0f);
    }
    
    public final float getCenteredPositionY() {
        return mPosition.y + (height / 2.0f);
    }

    public final Vector2 getVelocity() {
        return velocity;
    }

    public final void setVelocity(Vector2 velocity) {
        velocity.set(velocity);
    }

    public final Vector2 getTargetVelocity() {
        return targetVelocity;
    }

    public final void setTargetVelocity(Vector2 targetVelocity) {
        targetVelocity.set(targetVelocity);
    }
    
    public final int getCurrentAction() {
        return mCurrentAction;
    }
    
    public final void setCurrentAction(int type) {
        mCurrentAction = type;
    }
    
    public final int getCurrentState() {
        return mCurrentState;
    }
    
    public final void setCurrentState(int type) {
        mCurrentState = type;
    }
}
