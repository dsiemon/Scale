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

import javax.microedition.khronos.opengles.GL10;

import des.game.base.BaseObject;

public class TiledVertexGrid extends BaseObject {
	private static final float GL_MAGIC_OFFSET = 0.375f;
    private Grid mTileMap;
    private TiledWorld mWorld;
    private int mTileWidth;
    private int mTileHeight;
    
    private int mWidth;
    private int mHeight;
    private Texture mTexture;
    protected float[] colors = new float[4];
    protected boolean useColor = false;
    
    private float mWorldPixelWidth;
    private float mWorldPixelHeight;
    
    private int mTilesPerRow;
    private int mTilesPerColumn;
    
    private Boolean mGenerated;
    
    public TiledVertexGrid(Texture texture, int width, int height, int tileWidth, int tileHeight) {
        super();
        mTileWidth = tileWidth;
        mTileHeight = tileHeight;
        mWidth = width;
        mHeight = height;
        mTexture = texture;
        mGenerated = false;
    }
    
    @Override
    public void reset() {
    }

    public void setWorld(TiledWorld world) {
        mWorld = world;
        
    }
    public void setColor(float r, float g, float b, float a){
    	colors[0] = r;
    	colors[1] = g;
    	colors[2] = b;
    	colors[3] = a;
    	
    	this.useColor = true;
    }
    
    public void setColorOff(){
    	this.useColor = false;
    }
    private Grid generateGrid(int width, int height, int startTileX, int startTileY, float scaleX, float scaleY) {
        final int tileWidth = mTileWidth;
        final int tileHeight = mTileHeight;
        final float tileWidthScale = mTileWidth * scaleX;
        final float tileHeightScale = mTileHeight * scaleY;
        final int tilesAcross = width / tileWidth;
        final int tilesDown = height / tileHeight;
        final Texture texture = mTexture;
        final float texelWidth = 1.0f / texture.width;
        final float texelHeight = 1.0f / texture.height;
        final int textureTilesAcross = texture.width / tileWidth;
        final int textureTilesDown = texture.height / tileHeight;
        final int tilesPerWorldColumn = mWorld.getHeight();
        final int totalTextureTiles = textureTilesAcross * textureTilesDown;
        // Check to see if this entire grid is empty tiles.  If so, we don't need to do anything.
        boolean entirelyEmpty = true;
        for (int tileY = 0; tileY < tilesDown && entirelyEmpty; tileY++) {
            for (int tileX = 0; tileX < tilesAcross && entirelyEmpty; tileX++) {
                int tileIndex = mWorld.getTile(startTileX + tileX, 
                        (tilesPerWorldColumn - 1 - (startTileY + tileY)));
                if (tileIndex >= 0) {
                    entirelyEmpty = false;
                    break;
                }
            }
        }
        
        Grid grid = null;
        if (!entirelyEmpty) {
            grid = new Grid(tilesAcross, tilesDown, false);
            for (int tileY = 0; tileY < tilesDown; tileY++) {
                for (int tileX = 0; tileX < tilesAcross; tileX++) {
                    final float offsetX = tileX * tileWidthScale;
                    final float offsetY = tileY * tileHeightScale;
                    // MODIFIED flip column order
                    int tileIndex = mWorld.getTile(startTileX + tileX, 
                            (/*tilesPerWorldColumn - 1 - */(startTileY + tileY)));
                    if (tileIndex < 0) {
                        tileIndex = totalTextureTiles - 1; // Assume that the last tile is empty.
                    }
                    int textureOffsetX = (tileIndex % textureTilesAcross) * tileWidth;
                    int textureOffsetY = (tileIndex / textureTilesAcross) * tileHeight;
                    if (textureOffsetX < 0 || 
                            textureOffsetX > texture.width - tileWidth ||
                            textureOffsetY < 0 ||
                            textureOffsetY > texture.height - tileHeight) {
                        textureOffsetX = 0;
                        textureOffsetY = 0; 
                    }
                    final float u = (textureOffsetX + GL_MAGIC_OFFSET) * texelWidth;
                    final float v = (textureOffsetY + GL_MAGIC_OFFSET) * texelHeight;
                    final float u2 = ((textureOffsetX + tileWidth - GL_MAGIC_OFFSET) * texelWidth);
                    final float v2 = ((textureOffsetY + tileHeight - GL_MAGIC_OFFSET) * texelHeight);
                    
                    final float[] p0 = { offsetX, offsetY, 0.0f };
                    final float[] p1 = { offsetX + tileWidthScale, offsetY, 0.0f };
                    final float[] p2 = { offsetX, offsetY + tileHeightScale, 0.0f };
                    final float[] p3 = { offsetX + tileWidthScale, offsetY + tileHeightScale, 0.0f };
                    final float[] uv0 = { u, v2 };
                    final float[] uv1 = { u2, v2 };
                    final float[] uv2 = { u, v };
                    final float[] uv3 = { u2, v };
                    
                    final float[][] positions = { p0, p1, p2, p3 };
                    final float[][] uvs = { uv0, uv1, uv2, uv3 };

                    grid.set(tileX, tileY, positions, uvs);
                    
                }
            }
        }
        return grid;
    }
   
    public void draw(float x, float y, float scrollOriginX, float scrollOriginY, float scaleX, float scaleY) {
        TiledWorld world = mWorld;
        GL10 gl = OpenGLSystem.getGL();
      
        if (!mGenerated && world != null && gl != null && mTexture != null) {
            final int tilesAcross = mWorld.getWidth();
            final int tilesDown = mWorld.getHeight();
            
            mWorldPixelWidth = mWorld.getWidth() * mTileWidth;
            mWorldPixelHeight = mWorld.getHeight() * mTileHeight;
            mTilesPerRow = tilesAcross;
            mTilesPerColumn = tilesDown;
            
            
            BufferLibrary bufferLibrary = sSystemRegistry.bufferLibrary;
            
            Grid grid = generateGrid((int)mWorldPixelWidth, (int)mWorldPixelHeight, 0, 0, scaleX, scaleY);
            mTileMap = grid; 
            mGenerated = true;
            if (grid != null) {
                bufferLibrary.add(grid);
                if (sSystemRegistry.contextParameters.supportsVBOs) {
                	grid.generateHardwareBuffers(gl);
                }
            }
                       
        }
        
        final Grid tileMap = mTileMap;
        if (tileMap != null) {
            final Texture texture = mTexture;
            if (gl != null && texture != null) {

                int originX = (int) ((x - scrollOriginX)*scaleX);
                int originY = (int) ((y - scrollOriginY)*scaleY);

                
                final float worldPixelWidth = mWorldPixelWidth;
                
                final float percentageScrollRight = 
                    scrollOriginX != 0.0f ? scrollOriginX / worldPixelWidth : 0.0f;
                final float tileSpaceX = percentageScrollRight * mTilesPerRow;
                final int leftTile = (int)tileSpaceX;

                // calculate the top tile index
                final float worldPixelHeight = mWorldPixelHeight;
                
                final float percentageScrollUp = 
                    scrollOriginY != 0.0f ? scrollOriginY / worldPixelHeight : 0.0f;
                final float tileSpaceY = percentageScrollUp * mTilesPerColumn;
                final int bottomTile = (int)tileSpaceY;

                // calculate any sub-tile slop that our scroll position may require.
                final int horizontalSlop = ((tileSpaceX - leftTile) * mTileWidth) > 0 ? 1 : 0;
                final int verticalSlop = ((tileSpaceY - bottomTile) * mTileHeight) > 0 ? 1 : 0;

                
                OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.name);
                tileMap.beginDrawingStrips(gl, true);

                final int horzTileCount = (int)Math.ceil((float)mWidth / mTileWidth);
                final int vertTileCount = (int)Math.ceil((float)mHeight / mTileHeight);
                // draw vertex strips
                final int startX = leftTile;
                final int startY = bottomTile;
                final int endX = startX + horizontalSlop + horzTileCount;
                final int endY = startY + verticalSlop +  vertTileCount;
                
                gl.glPushMatrix();
                gl.glLoadIdentity();
                gl.glTranslatef(
                        originX, 
                        originY, 
                        0.0f);
                if(useColor){
                	gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
                }
                final int indexesPerTile = 6;
                final int indexesPerRow = mTilesPerRow * indexesPerTile;
                final int startOffset = (startX * indexesPerTile);
                final int count = (endX - startX) * indexesPerTile;
                for (int tileY = startY; tileY < endY && tileY < mTilesPerColumn; tileY++) {
                	final int row = tileY * indexesPerRow;
                	tileMap.drawStrip(gl, true, row + startOffset, count);
                }
                if(useColor){
                	gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                }
                gl.glPopMatrix();
              
                Grid.endDrawing(gl);
                
            }
        }
    }

}
