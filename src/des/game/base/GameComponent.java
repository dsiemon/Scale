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

/**
 * A game component implements a single feature of a game object.  Components are run once per frame
 * when their parent object is active.  Updating a game object is equivalent to updating all of its
 * components.  Note that a game object may contain more than one instance of the same type of
 * component.
 */
public abstract class GameComponent extends PhasedObject {
    // Defines high-level buckets within which components may choose to run.
    public enum ComponentPhases {
    	
    	PHYSICS_OBJECT,
    	PHYSICS_VECTOR,
    	PHYSICS_BOUNDARY,
    	PHYSICS_BOUNDARY_PART,
    	PHYSICS_FIELD,
        PHYSICS,                // impulse velocities are summed
        POST_PHYSICS,           // inertia, friction, and bounce
    	THINK,                  // decisions are made
    	THINK_TWO,
    	THINK_THREE,
        ANIMATION,              // animations are selected
        PRE_DRAW,               // drawing state is initialized
        DRAW,                   // drawing commands are scheduled.
        FRAME_END,              // final cleanup before the next update
    }
    
    public boolean shared;
    
    public GameComponent() {
        super();
        shared = false;
    }
    
    /**
     * This is used for loading game components with reflection, subclasses can override this function to copy initialization data from a definition copy loaded from xml
     * @param other
     * @param x
     * @param y
     * @param orientation
     * @param velocity
     * @param lifetime
     */
    public void initializeFromTemplate(GameComponent other, float x,float y,float orientation,float velocity,float lifetime){
    	
    
    }
    
}
