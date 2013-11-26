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

package des.game.scale;

import des.game.base.BaseObject;
import des.game.base.DebugLog;
import des.game.base.GameComponent;
import des.game.base.GameObject;



public class GenericDirectionalAnimationComponent extends GameComponent {
    private SpriteComponent mSprite;
    
    public GenericDirectionalAnimationComponent() {
        super();
        setPhase(ComponentPhases.ANIMATION.ordinal());
        reset();
    }
    
    @Override
    public void reset() {
        mSprite = null;
    }
    
    @Override
    public void update(float timeDelta, BaseObject parent) {
        if (mSprite != null) {
            GameObject parentObject = (GameObject) parent;
            int dir = parentObject.getCurrentDirection();
            switch(parentObject.getCurrentAction()) {
                
                case Animation.IDLE:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.IDLE, dir));
                    break;
                case Animation.MOVE:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.MOVE, dir));
                    break;
                case Animation.ATTACK:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.ATTACK, dir));
                    if(mSprite.animationFinished()){
                    	parentObject.setCurrentAction(CalculateAnimationIndex(Animation.IDLE, dir));
                    }
                    break;
                case Animation.HIT_REACT:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.HIT_REACT, dir));
                    break;
                case Animation.DEATH:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.DEATH, dir));
                    break;
                case Animation.FROZEN:
                    mSprite.playAnimation(CalculateAnimationIndex(Animation.FROZEN, dir));
                    break;
                case Animation.INVALID:
                default:  
                    mSprite.playAnimation(-1);
                    break;
            }
        }
    }
    
    public void setSprite(SpriteComponent sprite) {
        mSprite = sprite;
    }
   
    public static final class DirectionConstant {
    	public static final int INVALID = -1;
    	public static final int UP = 0;
    	public static final int LEFT = 1;
    	public static final int DOWN = 2;
    	public static final int RIGHT = 3;
    }
    
    public static final int ConvertDirectionOffset(GameObject.Direction dir) {
    	switch(dir){
    		case UP:
    			return DirectionConstant.UP;
    		case LEFT:
    			return DirectionConstant.LEFT;
    		case DOWN:
    			return DirectionConstant.DOWN;
    		case RIGHT:
    			return DirectionConstant.RIGHT;
    		default:
    			return 0;
    	}
    }
    
    public static final int CalculateAnimationIndex(int animation, int dir){
    	return animation*4 + dir;
    }
    
    public static final class Animation {
    	public static final int INVALID = -1;
        public static final int IDLE = 0;
        public static final int MOVE = 1;
        public static final int ATTACK = 2;
        public static final int HIT_REACT = 3;
        public static final int DEATH = 4;
        public static final int FROZEN = 5;
    }
}
