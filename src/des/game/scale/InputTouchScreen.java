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
import des.game.base.Vector2;

public class InputTouchScreen extends BaseObject {
	
	private int MAX_TOUCH_POINTS = 5;
	private InputXY mTouchPoints[];
	
	public InputTouchScreen() {
		mTouchPoints = new InputXY[MAX_TOUCH_POINTS];
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			mTouchPoints[x] = new InputXY();
		}
	}
	
	@Override
	public void reset() {
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			mTouchPoints[x].reset();
		}
	}

	public final void press(int index, float currentTime, float x, float y) {
		assert (index >= 0 && index < MAX_TOUCH_POINTS);
		if (index < MAX_TOUCH_POINTS) {
			mTouchPoints[index].press(currentTime, x, y);
		}
	}
	
	public final void release(int index) {
		if (index < MAX_TOUCH_POINTS) {
			mTouchPoints[index].release();
		}
	}
	
	public void resetAll() {
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			mTouchPoints[x].reset();
		}
	}
	
	public boolean getTriggered(int index, float time) {
		boolean triggered = false;
		if (index < MAX_TOUCH_POINTS) {
			triggered = mTouchPoints[index].getTriggered(time);
		}
		return triggered;
	}
	
	public boolean getPressed(int index) {
		boolean pressed = false;
		if (index < MAX_TOUCH_POINTS) {
			pressed = mTouchPoints[index].getPressed();
		}
		return pressed;
	}
	
	public final void setVector(int index, Vector2 vector) {
		if (index < MAX_TOUCH_POINTS) {
			mTouchPoints[index].setVector(vector);
		}
	}
	
	public final float getX(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = mTouchPoints[index].getX();
		}
		return magnitude;
	}
	
	public final float getY(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = mTouchPoints[index].getY();
		}
		return magnitude;
	}
	
	public final float getLastPressedTime(int index) {
		float time = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			time = mTouchPoints[index].getLastPressedTime();
		}
		return time;
	}
	
	public InputXY findPointerInRegion(float regionX, float regionY, float regionWidth, float regionHeight) {
		InputXY touch = null;
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			final InputXY pointer = mTouchPoints[x];
			if (pointer.getPressed() && 
					getTouchedWithinRegion(pointer.getX(), pointer.getY(), regionX, regionY, regionWidth, regionHeight)) {
				touch = pointer;
				break;
			}
		}
		return touch;
	}
	
	private final boolean getTouchedWithinRegion(float x, float y, float regionX, float regionY, float regionWidth, float regionHeight) {
		 return (x >= regionX &&
				 y >= regionY &&
				 x <= regionX + regionWidth &&
				 y <= regionY + regionHeight);
	}

	public boolean getTriggered(float gameTime) {
		boolean triggered = false;
		for (int x = 0; x < MAX_TOUCH_POINTS && !triggered; x++) {
			triggered = mTouchPoints[x].getTriggered(gameTime);
		}
		return triggered;
	}

	public int getNumTriggered(float gameTime){
		int triggered = 0;
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			if(mTouchPoints[x].getTriggered(gameTime)){
				triggered++;
			}
		}
		return triggered;
	}
}
