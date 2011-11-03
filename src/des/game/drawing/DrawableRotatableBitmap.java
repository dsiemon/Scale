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


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import des.game.base.BaseObject;
import des.game.base.DebugLog;


/** 
 * Draws a screen-aligned bitmap to the screen.
 */
public class DrawableRotatableBitmap extends DrawableBitmap {
    

    private static boolean mUseHardwareBuffers = false;
    private float mOrientation;
    
	private static final int FLOAT_SIZE = 4;
	private static final int FIXED_SIZE = 4;
	private static final int CHAR_SIZE = 2;
    

	/** The buffer holding the vertices */
	private static FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private static FloatBuffer textureBuffer;
	/** The buffer holding the indices */
	private static ByteBuffer indexBuffer;
	
    private static int mVertBufferIndex;
    private static int mIndexBufferIndex;
    private static int mTextureCoordBufferIndex;
    
    private static float vertices[] = {
			//Vertices according to faces
    		-50.0f, -50.0f, 0.0f, //Vertex 0
    		50.0f, -50.0f, 0.0f,   //v1
    		-50.0f, 50.0f, 0.0f,  //v2
    		50.0f, 50.0f, 0.0f   //v3
    		};
    
    /** The initial texture coordinates (u, v) */	
    private static float texture[] = {    		
			    		//Mapping coordinates for the vertices
			    		0.0f, 0.0f,
			    		1.0f, 0.0f,
			    		0.0f, 1.0f,
			    		1.0f, 1.0f

    					};
    
    /** The initial indices definition */	
    private static byte indices[] = {
    					//Faces definition
			    		0,1,3, 0,3,2 			
			    		};
    
    static public void SetupBuffers(){

		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * FLOAT_SIZE);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * FLOAT_SIZE);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		//
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
		GL10 gl = OpenGLSystem.getGL();
		if (BaseObject.sSystemRegistry.contextParameters.supportsVBOs && gl != null) {
			generateHardwareBuffers(gl);
			
		}
		
    }
    DrawableRotatableBitmap(Texture texture, int width, int height, float orientation) {
        super(texture, width, height);

        mOrientation = orientation;

    }

    
	public float getmOrientation() {
		return mOrientation;
	}

	public void setmOrientation(float mOrientation) {
		this.mOrientation = mOrientation;
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
        final Texture texture = mTexture;
        
        if (gl != null && texture != null) {
            assert texture.loaded;
            
            final float snappedX = (int) x;
            final float snappedY = (int) y;
            
            final float halfWidth = mWidth*scaleX/2;
            final float halfHeight = mHeight*scaleY/2;
                 
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
                        || !texture.loaded) {
                    cull = true;
                }
            }

            if (!cull) {       

            	
                OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.name);

                // This is necessary because we could be drawing the same texture with different
                // crop (say, flipped horizontally) on the same frame.
                OpenGLSystem.setTextureCrop(mCrop);
                
                if (opacity < 1.0f) {
                    gl.glColor4f(opacity, opacity, opacity, opacity);
                }
                gl.glPushMatrix();
                gl.glLoadIdentity();
                gl.glTranslatef(snappedX*scaleX + halfWidth, snappedY*scaleY + halfHeight, 0.0f);
        		
                // change orientation, this should rotate counter clockwise on the screen
                gl.glRotatef(mOrientation, 0.0f, 0.0f, -1f);
                
                
	                // setup the vertex array
	                // minx
	                vertices[0] = vertices[6] = -halfWidth;
	                // maxx
	                vertices[3] = vertices[9] = halfWidth;
	                
	                //miny
	                vertices[7] = vertices[10] = -halfHeight;
	                //maxy
	                vertices[1] = vertices[4] = halfHeight;
	                
	                //priority 
	                vertices[2] = vertices[5] = vertices[8] = vertices[11] = getPriority();
	        		//
	
	        		vertexBuffer.put(vertices);
	        		vertexBuffer.position(0);
	        	if(!mUseHardwareBuffers){
	        		//Enable the vertex and texture state
	        		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
	        		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	        		
	        		//Draw the vertices as triangles, based on the Index Buffer information
	        		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
                }
                else {
                    GL11 gl11 = (GL11)gl;
                    // draw using hardware buffers
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                    
                    final int vertexSize = vertexBuffer.capacity() * FLOAT_SIZE; 
                    gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
                            vertexBuffer, GL11.GL_DYNAMIC_DRAW);
                    
                    gl11.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
                    
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
                    gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);
                    
                    gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
                    gl11.glDrawElements(GL11.GL_TRIANGLES, indices.length,
                            GL11.GL_UNSIGNED_BYTE, 0);
                    
                    gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                    gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);


                }
        		
        		gl.glPopMatrix();
        		
                if (opacity < 1.0f) {
                    gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
    }

    public static void generateHardwareBuffers(GL10 gl) {
        if (!mUseHardwareBuffers) {
        	DebugLog.i("rotate", "Using Hardware Buffers");
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                
                // Allocate and fill the vertex buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mVertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                final int vertexSize = vertices.length * FLOAT_SIZE; 
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
                        vertexBuffer, GL11.GL_DYNAMIC_DRAW);
                
                
                // Unbind the array buffer.
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                
                // Allocate and fill the texture coordinate buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mTextureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 
                        mTextureCoordBufferIndex);
                final int texCoordSize = 
                    texture.length * FLOAT_SIZE;
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texCoordSize, 
                		textureBuffer, GL11.GL_STATIC_DRAW);    
                
                // Unbind the array buffer.
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                
                // Allocate and fill the index buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mIndexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 
                        mIndexBufferIndex);
                // A char is 2 bytes.
                final int indexSize = indices.length;
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, indexBuffer, 
                        GL11.GL_STATIC_DRAW);
                
                // Unbind the element array buffer.
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
                
                mUseHardwareBuffers = true;
                
                assert mVertBufferIndex != 0;
                assert mTextureCoordBufferIndex != 0;
                assert mIndexBufferIndex != 0;
                assert gl11.glGetError() == 0;
                
            
            }
        }
    }
    
    /**
     * Deletes the hardware buffers allocated by this object (if any).
     */
    public static void releaseHardwareBuffers(GL10 gl) {
        if (mUseHardwareBuffers) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                buffer[0] = mVertBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mTextureCoordBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mIndexBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
            }
            
            invalidateHardwareBuffers();
        }
    }
    
    /** 
     * When the OpenGL ES device is lost, GL handles become invalidated.
     * In that case, we just want to "forget" the old handles (without
     * explicitly deleting them) and make new ones.
     */
    public static  void invalidateHardwareBuffers() {
        mVertBufferIndex = 0;
        mIndexBufferIndex = 0;
        mTextureCoordBufferIndex = 0;
        mUseHardwareBuffers = false;
    }

}
