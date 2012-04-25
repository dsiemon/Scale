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
package des.game.drawing;


import javax.microedition.khronos.opengles.GL10;

import des.game.base.Vector2;

public class DrawableBufferedBitmap extends DrawableObject{



    private float mOrientation;
    private boolean rotatable;
    protected int mWidth;
    protected int mHeight;
    protected int mCrop[];
    protected int mViewWidth;
    protected int mViewHeight;
    protected float mOpacity;
    protected float[] colors = new float[4];
    protected boolean useColor = false;
    public static final float GL_MAGIC_OFFSET = 0.375f;
    private static float vertices[][] = {{0,0,0},{0,0,0},{0,0,0},{0,0,0}};
    private float textureCoord[][];

    

    public DrawableBufferedBitmap(int width, int height, float orientation) {
        super();

        mOrientation = orientation;

    }
    
    
	public void setTexture(Texture texture){
		// just get the texture coords since that is all we need
		textureCoord = texture.textureCoords;
	}
    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }
	public float getmOrientation() {
		return mOrientation;
	}

	public void setmOrientation(float mOrientation) {
		this.mOrientation = mOrientation;
	}
	public void setRotatable(boolean rotatable){
		this.rotatable = rotatable;
	}
	public void reset() {
        
        mViewWidth = 0;
        mViewHeight = 0;
        mOpacity = 1.0f;
        rotatable = false;
        this.useColor = false;
        
    }
	public void setTextureCoord(float[][] textureCoord) {
		this.textureCoord = textureCoord;
	}
    public void setViewSize(int width, int height) {
        mViewHeight = height;
        mViewWidth = width;
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
    /**
     * Draw the bitmap at a given x,y position, expressed in pixels, with the
     * lower-left-hand-corner of the view being (0,0).
     * 
     * @param gl  A pointer to the OpenGL context
     * @param x  The number of pixels to offset this drawable's origin in the x-axis.
     * @param y  The number of pixels to offset this drawable's origin in the y-axis
     * @param scaleX The horizontal scale factor between the bitmap resolution and the display resolution.
     * @param scaleY The vertical scale factor between the bitmap resolution and the display resolution.
     */
    @Override
    public void draw(float x, float y, float scaleX, float scaleY) {
        GL10 gl = OpenGLSystem.getGL();

        
        if (gl != null ) {

            final float snappedX = (int) x;
            final float snappedY = (int) y;
            final float scaledX = x* scaleX;
            final float scaledY = y*scaleY;
            final float scaledWidth = mWidth*scaleX;
            final float scaledHeight = mHeight*scaleX;
                 
            final float opacity = mOpacity;
            final float width = mWidth;
            final float height = mHeight;
            final float viewWidth = mViewWidth;
            final float viewHeight = mViewHeight;
            
            boolean cull = false;
            if (viewWidth > 0) {
                if (snappedX + width < 0.0f 
                		|| snappedX > viewWidth 
                        || snappedY + height < 0.0f
                        || snappedY > viewHeight 
                        || opacity == 0.0f
                        ) {
                    cull = true;
                }
            }

            if (!cull) {       
            	final DrawableBuffer db = DrawableBuffer.activeBuffer;
        		
            	if(rotatable){
            		final float halfWidth = scaledWidth/2;
                    final float halfHeight = scaledHeight/2;
                    
            		final float ox = scaledX + halfWidth;
            		final float oy = scaledY + halfHeight;
            		
//            		p'x = cos(theta) * (px-ox) - sin(theta) * (py-oy) + ox
//            		p'y = sin(theta) * (px-ox) + cos(theta) * (py-oy) + oy
            		
            		final float cosTheta = (float)Math.cos(mOrientation);
	                final float sinTheta = (float)Math.sin(mOrientation);
            		final float xCos = cosTheta*halfWidth;
            		final float xSin = sinTheta*halfWidth;
            		final float yCos = cosTheta*halfHeight;
            		final float ySin = sinTheta*halfHeight;
            		
            		vertices[0][0] = -xCos + ySin + ox;
            		vertices[0][1] = -xSin - yCos + oy;
            		
            		vertices[1][0] = xCos + ySin + ox;
            		vertices[1][1] = xSin - yCos + oy;
            		
            		vertices[2][0] = -xCos - ySin + ox;
            		vertices[2][1] = -xSin + yCos + oy;
            		
            		vertices[3][0] = xCos - ySin + ox;
            		vertices[3][1] = xSin + yCos + oy;
            		            
	                
	                //priority 
	                vertices[0][2] = vertices[1][2] = vertices[2][2] = vertices[3][2] = getPriority();
	                
	                
	                
	                
	                
            	}
            	else{
	                // setup the vertex array 0,1,3, 0,3,2 
	                // minx
	                vertices[0][0] = vertices[2][0] =  scaledX;
	                // maxx
	                vertices[1][0] = vertices[3][0] = scaledWidth + scaledX;
	                
	                //miny
	                vertices[0][1] = vertices[1][1] =  scaledY;
	                //maxy
	                vertices[2][1] = vertices[3][1] = scaledHeight + scaledY;
	                
	                //priority 
	                vertices[0][2] = vertices[1][2] = vertices[2][2] = vertices[3][2] = getPriority();
            	}
	               
               if(useColor){
            	   db.set(vertices, this.textureCoord, colors);
               }
               else{
            	   db.set(vertices, this.textureCoord);
               }
	            
	        
            }
        }
    }

    @Override
    public boolean visibleAtPosition(Vector2 position) {
        boolean cull = false;
        if (mViewWidth > 0) {
            if (position.x + mWidth < 0 || position.x > mViewWidth 
                    || position.y + mHeight < 0 || position.y > mViewHeight) {
                cull = true;
            }
        }
        return !cull;
    }

}
