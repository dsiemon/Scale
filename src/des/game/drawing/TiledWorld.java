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

package des.game.drawing;



import des.game.base.AllocationGuard;


/**
 * TiledWorld manages a 2D map of tile indexes that define a "world" of tiles.  These may be 
 * foreground or background layers in a scrolling game, or a layer of collision tiles, or some other
 * type of tile map entirely.  The TiledWorld maps xy positions to tile indices and also handles
 * deserialization of tilemap files.
 */
public class TiledWorld extends AllocationGuard {
    private int[][] mTilesArray;
    private int mRowCount;
    private int mColCount;

    
    public TiledWorld(int cols, int rows) {
        super();
        mTilesArray = new int[cols][rows];
        mRowCount = rows;
        mColCount = cols;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                mTilesArray[x][y] = -1;
            }
        }
        


    }

    public void setTile(int x, int y, int value){
    	mTilesArray[x][y] = value;
    }

    public void setTileRange(int x, int y, int width, int height, int value){
    	final int xMax = x+width;
    	final int yMax = y+height;
    	
    	for(int i = x; i < xMax; i++){
    		for(int j = y; j < yMax; j++){
    			mTilesArray[i][j] = value;
    		}
    	}
    }
    public int getTile(int x, int y) {
        int result = -1;
        if (x >= 0 && x < mColCount && y >= 0 && y < mRowCount) {
            result = mTilesArray[x][y];
        }
        return result;
    }


    
    public void calculateSkips() {
        int emptyTileCount = 0;
        for (int y = mRowCount - 1; y >= 0; y--) {
            for (int x = mColCount - 1; x >= 0; x--) {
                if (mTilesArray[x][y] < 0) {
                    emptyTileCount++;
                    mTilesArray[x][y] = -emptyTileCount;
                } else {
                    emptyTileCount = 0;
                }
            }
        }
    }

    public final int getWidth() {
        return mColCount;
    }

    public final int getHeight() {
        return mRowCount;
    }
    
    public final int[][] getTiles() {
        return mTilesArray;
    }

}
