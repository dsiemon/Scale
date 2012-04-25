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
import des.game.base.DebugLog;
import des.game.base.FixedSizeArray;
import des.game.scale.InputTouchEvent.TouchState;

public class InputScreen extends BaseObject{
	
	public static final int MAX_TOUCH_POINTS = 5;
	public static final int MAX_TOUCH_BUTTONS = 5;
	private InputTouchEvent touchEvents[];
	
	private FixedSizeArray<InputTouchEvent> inputQueue;
	private FixedSizeArray<InputTouchEvent> inputBuffer;
	private FixedSizeArray<InputTouchEvent> inputPool;
	
	private FixedSizeArray<InputTouchButton> touchButtons;
	private boolean[] proccessedButtonFlags;
	private boolean[] proccessedFlags;
	
	public InputScreen() {
		touchEvents = new InputTouchEvent[MAX_TOUCH_POINTS];
		inputQueue = new FixedSizeArray<InputTouchEvent>(MAX_TOUCH_POINTS);
		inputBuffer = new FixedSizeArray<InputTouchEvent>(MAX_TOUCH_POINTS);
		inputPool = new FixedSizeArray<InputTouchEvent>(MAX_TOUCH_POINTS);
		
		touchButtons = new FixedSizeArray<InputTouchButton>(MAX_TOUCH_BUTTONS);
		proccessedFlags = new boolean[MAX_TOUCH_POINTS];
		proccessedButtonFlags = new boolean[MAX_TOUCH_BUTTONS];
		
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			touchEvents[x] = new InputTouchEvent();
			inputPool.add(new InputTouchEvent());
		}
	}
	
	@Override
	public void reset() {
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			touchEvents[x].idle();
		}
		
		while(inputQueue.getCount() > 0){
			inputPool.add(inputQueue.removeLast());
		}
	}
	
	public int getQueuedEvents(){
		return this.inputQueue.getCount();
	}
	
	public FixedSizeArray<InputTouchEvent> getInputQueue(){
		return this.inputQueue;
	}
	
	public InputTouchEvent getQueuedEventById(int id){
		InputTouchEvent event = null;
		
		final int queueCount = inputQueue.getCount();
		final Object[] events = inputQueue.getArray();
		
		for (int x = 0; x < queueCount; x++) {
			InputTouchEvent cur = (InputTouchEvent)events[x];
			if(cur.id == id){
				return cur;
			}
		}
		
		return event;
	}
	
	public void flushInputQueue(){
		
		for(int i = 0; i < MAX_TOUCH_POINTS; i++){
			proccessedFlags[i] = false;
		}
		
		for(int i = 0; i < MAX_TOUCH_BUTTONS; i++){
			proccessedButtonFlags[i] = false;
		}
		
		// set buttons that are up to idle
		final int buttonCount = touchButtons.getCount();
		final Object[] buttonArray = touchButtons.getArray();
		
		for(int i = 0; i < buttonCount; i++){
			InputTouchButton b = (InputTouchButton)buttonArray[i];
			if(b.state.equals(TouchState.UP)){
				b.state = TouchState.IDLE;
				b.id = -1;
			}
		}
		
		InputTouchEvent currentEvent = null;
		
		final int queueCount = inputQueue.getCount();
		for(int i = 0; i < queueCount; i++){
			currentEvent = inputQueue.removeLast();
			final int eventId = currentEvent.id;
			final int index = this.findTouchEvent(eventId);
			
			// if an event is up then go ahead and remove it from the queue, this id may be down again already but we will handle it after the input queue
			if(currentEvent.state.equals(TouchState.UP)){
				inputPool.add(currentEvent);
			}
			// handle events that are down already
			else if(currentEvent.state.equals(TouchState.DOWN)){
				// if we could not find the event then set it to up, we cant get any more info about it since the event is gone
				if(index == -1){
					currentEvent.state = TouchState.UP;
				}
				else{
					// get the most recent info about this event and set it to processed
					InputTouchEvent touchEvent = this.getTouchEventByIndex(index);
					this.proccessedFlags[index] = true;
					
					// it should be up or down copy the event info
					if(touchEvent.state.equals(TouchState.UP)|| touchEvent.state.equals(TouchState.DOWN)){
						currentEvent.copyEvent(touchEvent);
					}
					//if the event is idle or restarted go ahead and set it to up
					else{
						currentEvent.state = TouchState.UP;
					}
				}
				
				// add the event to the next queue
				this.inputBuffer.add(currentEvent);
			}
			// if this was a new event then we will either set it to down or to up
			else if(currentEvent.state.equals(TouchState.START)){
				// if we could not find the event then set it to up, we cant get any more info about it since the event is gone
				if(index == -1){
					currentEvent.state = TouchState.UP;
				}
				else{
					// get the most recent info about this event and set it to processed
					InputTouchEvent touchEvent = this.getTouchEventByIndex(index);
					this.proccessedFlags[index] = true;
					
					// it should be up or down copy the event info
					if(touchEvent.state.equals(TouchState.UP)|| touchEvent.state.equals(TouchState.DOWN)){
						currentEvent.copyEvent(touchEvent);
					}
					//if the event is idle or restarted go ahead and set it to up
					else{
						currentEvent.state = TouchState.UP;
					}
					
					
				}
				
				// add the event to the next queue
				this.inputBuffer.add(currentEvent);
			}
		}
		
		for(int i = 0; i < InputScreen.MAX_TOUCH_POINTS; i++){
			// for each event that was not proccessed and that is not idle
			if(!proccessedFlags[i] && !this.touchEvents[i].state.equals(TouchState.IDLE)){
				
				// see if this should be captured by a button, buttons can capture touch events that are not already in the input queue. Meaning if a motion starts outside of the button it will not be able to affect it
				if(!CaptureButtonEvents(this.touchEvents[i].id,this.touchEvents[i].mDownTime,this.touchEvents[i].x, this.touchEvents[i].y, this.touchEvents[i].state)){
					// if not a button event, create a new touch event
					if(this.inputPool.getCount() > 0){
						InputTouchEvent newEvent = inputPool.removeLast();
						
						newEvent.id = this.touchEvents[i].id;
						newEvent.copyEvent(this.touchEvents[i]);
						// change the state from whatever teh new event was(up or down) to start
						newEvent.state = TouchState.START;
						
						this.inputBuffer.add(newEvent);
					}
				}
			}
		}
		
		// any buttons that did not have an event, set to up
		for(int i = 0; i < buttonCount; i++){
			InputTouchButton b = (InputTouchButton)buttonArray[i];
			if(!proccessedButtonFlags[i] && !b.state.equals(TouchState.IDLE)){
				b.state = TouchState.UP;
			}
		}
		
		if(inputQueue.getCount() + inputBuffer.getCount() + inputPool.getCount() != MAX_TOUCH_POINTS){
			DebugLog.e("touch error", "ERROR queue size: " + (inputQueue.getCount() + inputBuffer.getCount() + inputPool.getCount()));
		}
		
		// swap the queue
		FixedSizeArray<InputTouchEvent> tmp = inputQueue;
		this.inputQueue = this.inputBuffer;
		this.inputBuffer = tmp;
		
		// set up events back to idle
		this.setUpEventsToIdle();
	}
	public final void press(int id, float currentTime, float x, float y) {
		int index = this.findTouchEvent(id);
		
		if(index == -1){

			index = this.allocateIdleTouchEvent(id);
		}
		
		if(index != -1){

			touchEvents[index].press(currentTime, x, y);
		}
	}
	
	public final void release(int id, float currentTime, float x, float y) {
		int index = this.findTouchEvent(id);
		
		if(index == -1){
			index = this.allocateIdleTouchEvent(id);
		}
		
		if(index != -1){
			touchEvents[index].release(currentTime, x, y);
		}
	}
	
	public void setUpEventsToIdle(){
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			if(touchEvents[x].state.equals(TouchState.UP)){
				touchEvents[x].idle();
				touchEvents[x].id = -1;
			}
		}
	}
	public int getNumEvents(){
		int count = 0;
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			if(!touchEvents[x].state.equals(TouchState.IDLE)){
				count++;
			}
		}
		
		return count;
	}
	public InputTouchEvent getTouchEventByIndex(int index){
		return touchEvents[index];
	}
	public InputTouchEvent getTouchEventById(int id){
		InputTouchEvent event = null;
		
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			if(touchEvents[x].id == id){
				return touchEvents[x];
			}
		}
		
		return event;
	}
	public int findTouchEvent(int id){
		int index = -1;
		
		for (int x = 0; x < MAX_TOUCH_POINTS && index == -1; x++) {
			if(touchEvents[x].id == id){
				index = x;
			}
		}
		
		return index;
	}
	private int allocateIdleTouchEvent(int id){
		int index = -1;
		
		for (int x = 0; x < MAX_TOUCH_POINTS && index == -1; x++) {
			if(touchEvents[x].state.equals(TouchState.IDLE)){
				index = x;
				touchEvents[x].id = id;
			}
		}
		
		return index;
	}
	
	private boolean CaptureButtonEvents(int id, float currentTime, float x, float y,TouchState state){
		boolean rtn = false;
		
		// first check buttons that are not idle and see if the id matches
		// set buttons that are up to idle
		final int buttonCount = touchButtons.getCount();
		final Object[] buttonArray = touchButtons.getArray();
		
		for(int i = 0; i < buttonCount && !rtn; i++){
			InputTouchButton b = (InputTouchButton)buttonArray[i];
			// see if there is a button that has capture this input before
			if(!b.state.equals(TouchState.IDLE) && b.id == id){
				rtn = true;
				proccessedButtonFlags[i] = true;
				if(state.equals(TouchState.UP)){
					b.release(currentTime, x, y);
				}
				else{
					b.press(currentTime, x, y);
				}
			}
		}
		
		// if no id's found look to see if any button can capture the event
		for(int i = 0; i < buttonCount && !rtn; i++){
			InputTouchButton b = (InputTouchButton)buttonArray[i];
			if(b.state.equals(TouchState.IDLE)){
				rtn = b.CaptureEvent(id, currentTime, x, y);
				if(rtn){
					proccessedButtonFlags[i] = true;
				}
			}
		}
		
		return rtn;
	}
	
	public void AddButton(InputTouchButton button){
		this.touchButtons.add(button);
	}
}
