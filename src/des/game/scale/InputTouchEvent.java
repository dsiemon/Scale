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

public class InputTouchEvent {
	public enum TouchState{
		START,DOWN,UP,IDLE;
	}
	public TouchState state;
	public float mLastPressedTime;
	public float mDownTime;
	public int id;
	public float x;
	public float y;

	
	public InputTouchEvent(){
		state = TouchState.IDLE;
		id = -1;
	}
	public void copyEvent(InputTouchEvent other){
		this.state = other.state;
		this.mDownTime = other.mDownTime;
		this.mLastPressedTime = other.mLastPressedTime;
		this.x = other.x;
		this.y = other.y;
	}
	public void press(float currentTime, float x, float y) {
		mLastPressedTime = currentTime;
		this.x = x;
		this.y = y;
		
		// if it was not already down set the down time
		if(!state.equals(TouchState.DOWN)){
			mDownTime = currentTime;
		}
		
		state = TouchState.DOWN;
	}
	
	public void release(float currentTime, float x, float y) {
		mLastPressedTime = currentTime;
		this.x = x;
		this.y = y;
		state = TouchState.UP;

	}
	
	public void idle(){
		state = TouchState.IDLE;
	}

}
