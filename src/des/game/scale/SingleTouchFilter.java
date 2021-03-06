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

import android.view.MotionEvent;

public class SingleTouchFilter extends TouchFilter {

	public void updateTouch(MotionEvent event) {
		ContextParameters params = sSystemRegistry.contextParameters;
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		sSystemRegistry.inputSystem.touchUp(0, event.getRawX() * (1.0f / params.viewScaleX), 
    				event.getRawY() * (1.0f / params.viewScaleY));
    	} else {
    		sSystemRegistry.inputSystem.touchDown(0, event.getRawX() * (1.0f / params.viewScaleX),
    				event.getRawY() * (1.0f / params.viewScaleY));
    	}
    }
	@Override
	public void reset() {
	}

}
