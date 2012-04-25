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
import des.game.base.ObjectManager;
import des.game.drawing.TiledWorld;






/**
 * Manages information about the current level, including setup, deserialization, and tear-down.
 */
public abstract class LevelSystem extends BaseObject {
	

    protected GameFlowEvent mGameFlowEvent;

    public LevelSystem() {
        super();
        mGameFlowEvent = new GameFlowEvent();
    }
    
    @Override
    public void reset() {

    }
    
    public abstract int getTileWidth();
    public abstract int getTileHeight();
    public abstract int getWidthInTiles();
    public abstract int getHeightInTiles();
    public abstract float getLevelWidth();
    public abstract float getLevelHeight();
    public abstract TiledWorld getMapTiles();
    /**
     * Loads a level from a binary file.  The file consists of several layers, including background
     * tile layers and at most one collision layer.  Each layer is used to bootstrap related systems
     * and provide them with layer data.
     * @param stream  The input stream for the level file resource.
     * @param tiles   A tile library to use when constructing tiled background layers.
     * @param background  An object to assign background layer rendering components to.
     * @return
     */
    public abstract boolean loadLevel(Level level, ObjectManager<BaseObject> root);
    public abstract Level parseLevelId(String levelId);

    public abstract void incrementAttemptsCount();
    
    public abstract int getAttemptsCount();
    public abstract String getCurrentLevel();
    
    public void sendRestartEvent() {
        mGameFlowEvent.post(GameFlowEvent.EVENT_RESTART_LEVEL, 0,
                sSystemRegistry.contextParameters.context);
    }
    
    public void sendNextLevelEvent() {
        mGameFlowEvent.post(GameFlowEvent.EVENT_GO_TO_NEXT_LEVEL, 0,
                sSystemRegistry.contextParameters.context);
    }
    
    public void sendGameEvent(int type, int index, boolean immediate) {
        if (immediate) {
        	mGameFlowEvent.postImmediate(type, index,
                sSystemRegistry.contextParameters.context);
        } else {
        	mGameFlowEvent.post(type, index,
                    sSystemRegistry.contextParameters.context);
        }
    } 
    

}
