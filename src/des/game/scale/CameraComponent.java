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
package des.game.scale;


import des.game.base.BaseObject;
import des.game.base.CollisionComponent;
import des.game.base.FixedSizeArray;
import des.game.base.GameObject;
import des.game.base.Vector2;
import des.game.physics.PhysicsObject;
import des.game.physics.VectorObject;

import des.game.scale.InputTouchEvent.TouchState;

public class CameraComponent extends CollisionComponent {
	
	public float px;
	public float py;
	boolean touchEventStarted;
	float lastTouchX;
	float lastTouchY;
	int touchId1;
	int touchId2;
	
	public final static float SNAP = 0;
	public final static float STICK = 0;
	
	public CameraComponent(){
		super();
		px = py = lastTouchX = lastTouchY = 0;
		touchEventStarted = false;
	}
	public CameraComponent(PhysicsObject physicsObject){
		super(physicsObject);
		px = py = lastTouchX = lastTouchY = 0;
		touchEventStarted = false;
	}

	@Override
	public void reset(){
		super.reset();
		
		px = py = lastTouchX = lastTouchY = 0;
		touchEventStarted = false;
	}
	@Override
    public void update(float timeDelta, BaseObject parent) {

		final GameObject gameObject = (GameObject)parent;
		
		final InputScreen screen = BaseObject.sSystemRegistry.inputSystem.getScreen();
		
//		//////////////////////////////////////
//		FixedSizeArray<InputTouchEvent> queue2 = screen.getInputQueue();
//		
//		final int queueCount2 = queue2.getCount();
//		String report = "";
//		
//		for(int i = 0; i < queueCount2; i++){
//			report += "[";
//			InputTouchEvent cur = queue2.get(i);
//			report += "#" + cur.id + " " + cur.state + " x:" + (int)cur.x+ "y:" + (int)cur.y;
//			report += "]  ";
//		}
//		
//		if(queueCount2 != 0){
//			DebugLog.e("touch", report);
//		}
//		/////////////////////////////////////
		
		
		if(screen.getQueuedEvents() == 2){
			
			if(touchEventStarted){
				InputTouchEvent event1 = screen.getQueuedEventById(touchId1);
				InputTouchEvent event2 = screen.getQueuedEventById(touchId2);
				
				if(event1 != null && event2 != null && event1.state.equals(TouchState.DOWN) && event2.state.equals(TouchState.DOWN)){
					float touchX = (event1.x+ event2.x)/2;
					float touchY = (event1.y+ event2.y)/2;
					
					this.physicsObject.getLocation().setX(this.physicsObject.getLocation().getX() - (touchX - lastTouchX));
					this.physicsObject.getLocation().setY(this.physicsObject.getLocation().getY() - (touchY - lastTouchY));
					
					lastTouchX = touchX;
					lastTouchY = touchY;
				}
				else{
					touchEventStarted = false;
				}
			}
			else{
				touchId1 = touchId2 = -1;
				touchEventStarted = true;
				
				float touchX = 0;
				float touchY = 0;
				int count = 0;
				FixedSizeArray<InputTouchEvent> queue = screen.getInputQueue();
				
				final int queueCount = queue.getCount();
				
				for(int i = 0; i < queueCount; i++){
					InputTouchEvent event = queue.get(i);
					if(event.state.equals(InputTouchEvent.TouchState.DOWN) || event.state.equals(InputTouchEvent.TouchState.START)){
						touchX += event.x;
						touchY += event.y;
						
						if(count == 0){
							touchId1 = event.id;
							count++;
						}
						else if(count == 1){
							count++;
							touchId2 = event.id;
						}
					}
				}
				
				touchX = touchX/2;
				touchY = touchY/2;
				
				lastTouchX = touchX;
				lastTouchY = touchY;
			}
			

		}
		else{
			//DebugLog.i("touch", "camera " + screen.getNumTriggered(timeDelta));
			touchEventStarted = false;
		}
		
		final Vector2 position = gameObject.getPosition();
		px = position.x;
		py = position.y;
		
		float focalPositionX = (float)this.physicsObject.getLocation().getX();
		float focalPositionY = (float)this.physicsObject.getLocation().getY();
    	

    	

        final float height = sSystemRegistry.contextParameters.gameHeight;
        final LevelSystem level = sSystemRegistry.levelSystem;
        if (level != null) {
            final float worldPixelHeight = Math.max(level.getLevelHeight(), sSystemRegistry.contextParameters.gameHeight);
            final float topEdge = focalPositionY + (height / 2.0f);
            final float bottomEdge = focalPositionY - (height / 2.0f);
            
            
            if (topEdge >= worldPixelHeight - SNAP) {
                focalPositionY = (worldPixelHeight - (height / 2.0f)) + STICK;
            } else if (bottomEdge <= SNAP) {
                focalPositionY = height / 2.0f - STICK;
            }
            
            
        }
    	
        final float width = sSystemRegistry.contextParameters.gameWidth;
        if (level != null) {
            final float worldPixelWidth = Math.max(level.getLevelWidth(), width);
            final float rightEdge = focalPositionX + (width / 2.0f);
            final float leftEdge = focalPositionX - (width / 2.0f);
    
            if (rightEdge >= worldPixelWidth - SNAP) {
                focalPositionX = (worldPixelWidth - (width / 2.0f)) + STICK;
            } else if (leftEdge <= SNAP) {
                focalPositionX = width / 2.0f - STICK;
            }
        }
        
        this.physicsObject.getLocation().setX(focalPositionX);
        this.physicsObject.getLocation().setY(focalPositionY);
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
}
