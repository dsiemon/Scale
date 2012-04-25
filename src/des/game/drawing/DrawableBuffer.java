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
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.Comparator;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import des.game.base.DebugLog;

public class DrawableBuffer{
	private static final int FLOAT_SIZE = 4;
	private static final int CHAR_SIZE = 2;
	public static DrawableBuffer activeBuffer;

	public static final float GL_MAGIC_OFFSET = 0.375f;
	public static final int MAX_BUFFERS = 5;
	public static final int BUFFER_SIZE = 100;
	private static final int mIndexCount = BUFFER_SIZE*6;
	public static final DrawableBufferComparator drawableBufferComparator = new DrawableBufferComparator();
	public int startLayer;
	public int endLayer;
	public boolean useColor;
	private Texture texture;
    private FloatBuffer mFloatVertexBuffer;
    private FloatBuffer mFloatTexCoordBuffer;
    private FloatBuffer mFloatColorBuffer;
    private CharBuffer mIndexBuffer;
    private boolean mUseHardwareBuffers;
    private int mVertBufferIndex;
    private int mColorBufferIndex;
    private int mIndexBufferIndex;
    private int mTextureCoordBufferIndex;
    
    
    private int currentBufferLocation;
    
	public DrawableBuffer(Texture texture, int startLayer, int endLayer, boolean color){
		
		this.useColor = color;
		this.startLayer = startLayer;
		this.endLayer = endLayer;
		
		this.texture = texture;



		
		mUseHardwareBuffers = false;
		mFloatVertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * BUFFER_SIZE*4 * 3).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    mFloatTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * BUFFER_SIZE*4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    if(useColor){
	    	mFloatColorBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * BUFFER_SIZE*4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    }
	    mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * BUFFER_SIZE * 6).order(ByteOrder.nativeOrder()).asCharBuffer();
	    
	    
        /*
         * Initialize triangle list mesh.
         *
         *     [c]------[d]   [2]------[3] ...
         *      |    /   |       |    /   |
         *      |   /    |       |   /    |
         *      |  /     |       |  /     |
         *     [a]-----[b] [w+2]----[w+3]...
         *   
         *
         */
	    
	    // set the index buffer
	    int i = 0;
	    int vertexCount = 0;
	    while( i < mIndexCount){
	    	
            char a = (char) vertexCount++;
            char b = (char) vertexCount++;
            char c = (char) vertexCount++;
            char d = (char) vertexCount++;
            
            mIndexBuffer.put(i++, a);
            mIndexBuffer.put(i++, b);
            mIndexBuffer.put(i++, d);

            mIndexBuffer.put(i++, a);
            mIndexBuffer.put(i++, d);
            mIndexBuffer.put(i++, c);
	    }
	    
	    currentBufferLocation = 0;
	}
	public Texture getTexture(){
		return texture;
	}
	public void startDrawing(){
		currentBufferLocation = 0;
		activeBuffer = this;
	}
	public void bindBuffers(GL10 gl){
		
		
		OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.name);
		if (!mUseHardwareBuffers) {
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFloatVertexBuffer);
    
            
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mFloatTexCoordBuffer);
            if(useColor){
            	gl.glColorPointer(4, GL10.GL_FLOAT, 0, mFloatColorBuffer);
            }
            
        } else {
            GL11 gl11 = (GL11)gl;
            // draw using hardware buffers
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
            final int vertexSize = mFloatVertexBuffer.capacity() * FLOAT_SIZE; 
            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
            		mFloatVertexBuffer, GL11.GL_DYNAMIC_DRAW);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
            final int texSize = mFloatTexCoordBuffer.capacity() * FLOAT_SIZE; 
            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texSize, 
            		mFloatTexCoordBuffer, GL11.GL_DYNAMIC_DRAW);
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);
            
            if(useColor){
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mColorBufferIndex);
	            final int colorSize = mFloatColorBuffer.capacity() * FLOAT_SIZE; 
	            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, colorSize, 
	            		mFloatColorBuffer, GL11.GL_DYNAMIC_DRAW);
	            gl11.glColorPointer(4, GL10.GL_FLOAT, 0, 0);
            }
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
        }
	}
    public void draw(GL10 gl) {
    	OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.name);
    	gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    	//DebugLog.i("Drawable Buffer", "draw: " + currentBufferLocation);
        if (!mUseHardwareBuffers) {
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFloatVertexBuffer);
    
            
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mFloatTexCoordBuffer);
                if(useColor){
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mFloatColorBuffer);
                }
    
            gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount,
                    GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        } else {
            GL11 gl11 = (GL11)gl;
            
            mFloatVertexBuffer.position(0);
            mFloatTexCoordBuffer.position(0);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
            final int vertexSize = mFloatVertexBuffer.capacity() * FLOAT_SIZE;
            gl11.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, vertexSize, mFloatVertexBuffer);
//            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize,
//            		mFloatVertexBuffer, GL11.GL_DYNAMIC_DRAW);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureCoordBufferIndex);
            final int texSize = mFloatTexCoordBuffer.capacity() * FLOAT_SIZE; 
            gl11.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, texSize, mFloatTexCoordBuffer);
//            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texSize, 
//            		mFloatTexCoordBuffer, GL11.GL_DYNAMIC_DRAW);
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);
            
            if(useColor){
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mColorBufferIndex);
	            final int colorSize = mFloatColorBuffer.capacity() * FLOAT_SIZE; 
	            gl11.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, colorSize, mFloatColorBuffer);
	//            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, colorSize, 
	//            		mFloatColorBuffer, GL11.GL_DYNAMIC_DRAW);
	            gl11.glColorPointer(4, GL10.GL_FLOAT, 0, 0);
            }
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferIndex);
            
            // draw using hardware buffers
  
            
            
            gl11.glDrawElements(GL11.GL_TRIANGLES, currentBufferLocation*6,
                    GL11.GL_UNSIGNED_SHORT, 0);
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            activeBuffer = null;

        }
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
	public void set(float[][] positions, float[][] uvs) {
        if(currentBufferLocation < BUFFER_SIZE){
        	
            final int currentVertexLocation = currentBufferLocation * 4;
        	setVertex(currentVertexLocation, 		positions[0][0], positions[0][1], positions[0][2], uvs[0][0], uvs[0][1]);
            setVertex(currentVertexLocation+1, 	positions[1][0], positions[1][1], positions[1][2], uvs[1][0], uvs[1][1]);
            setVertex(currentVertexLocation+2, 	positions[2][0], positions[2][1], positions[2][2], uvs[2][0], uvs[2][1]);
            setVertex(currentVertexLocation+3, positions[3][0], positions[3][1], positions[3][2], uvs[3][0], uvs[3][1]);
            
            currentBufferLocation++;
        }
    }
	private void setVertex(int vertex, float x, float y, float z, float u, float v) {
		   final int posIndex = vertex * 3;
		   final int texIndex = vertex * 2;
		   final int colorIndex = vertex*4;
		    mFloatVertexBuffer.put(posIndex, x);
		    mFloatVertexBuffer.put(posIndex + 1, y);
		    mFloatVertexBuffer.put(posIndex + 2, z);

		    mFloatTexCoordBuffer.put(texIndex, u);
		    mFloatTexCoordBuffer.put(texIndex + 1, v);
		    if(useColor){
		    mFloatColorBuffer.put(colorIndex, 1);
		    mFloatColorBuffer.put(colorIndex + 1, 1);
		    mFloatColorBuffer.put(colorIndex + 2, 1);
		    mFloatColorBuffer.put(colorIndex + 3, 1);
		    }
	}
	public void set(float[][] positions, float[][] uvs, float[] color) {
        if(currentBufferLocation < BUFFER_SIZE){
        	
            final int currentVertexLocation = currentBufferLocation * 4;
        	setVertex(currentVertexLocation, 		positions[0][0], positions[0][1], positions[0][2], uvs[0][0], uvs[0][1], color[0], color[1], color[2], color[3]);
            setVertex(currentVertexLocation+1, 	positions[1][0], positions[1][1], positions[1][2], uvs[1][0], uvs[1][1], color[0], color[1], color[2], color[3]);
            setVertex(currentVertexLocation+2, 	positions[2][0], positions[2][1], positions[2][2], uvs[2][0], uvs[2][1], color[0], color[1], color[2], color[3]);
            setVertex(currentVertexLocation+3, positions[3][0], positions[3][1], positions[3][2], uvs[3][0], uvs[3][1], color[0], color[1], color[2], color[3]);
            
            currentBufferLocation++;
        }
    }
	private void setVertex(int vertex, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
		   final int posIndex = vertex * 3;
		   final int texIndex = vertex * 2;
		   final int colorIndex = vertex*4;
		    mFloatVertexBuffer.put(posIndex, x);
		    mFloatVertexBuffer.put(posIndex + 1, y);
		    mFloatVertexBuffer.put(posIndex + 2, z);

		    mFloatTexCoordBuffer.put(texIndex, u);
		    mFloatTexCoordBuffer.put(texIndex + 1, v);
		    if(useColor){
		    mFloatColorBuffer.put(colorIndex, r);
		    mFloatColorBuffer.put(colorIndex + 1, g);
		    mFloatColorBuffer.put(colorIndex + 2, b);
		    mFloatColorBuffer.put(colorIndex + 3, a);
		    }
	}
	
	
    public boolean usingHardwareBuffers() {
        return mUseHardwareBuffers;
    }
     
    /** 
     * When the OpenGL ES device is lost, GL handles become invalidated.
     * In that case, we just want to "forget" the old handles (without
     * explicitly deleting them) and make new ones.
     */
    public void invalidateHardwareBuffers() {
        mVertBufferIndex = 0;
        mIndexBufferIndex = 0;
        mTextureCoordBufferIndex = 0;
        mColorBufferIndex = 0;
        mUseHardwareBuffers = false;
    }
    
    /**
     * Deletes the hardware buffers allocated by this object (if any).
     */
    public void releaseHardwareBuffers(GL10 gl) {
        if (mUseHardwareBuffers) {
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                buffer[0] = mVertBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mTextureCoordBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mColorBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
                
                buffer[0] = mIndexBufferIndex;
                gl11.glDeleteBuffers(1, buffer, 0);
            }
            
            invalidateHardwareBuffers();
        }
    }
    
    /** 
     * Allocates hardware buffers on the graphics card and fills them with
     * data if a buffer has not already been previously allocated.  Note that
     * this function uses the GL_OES_vertex_buffer_object extension, which is
     * not guaranteed to be supported on every device.
     * @param gl  A pointer to the OpenGL ES context.
     */
    public void generateHardwareBuffers(GL10 gl) {
        if (!mUseHardwareBuffers) {
        	DebugLog.i("Drawable Buffer", "Using Hardware Buffers");
            if (gl instanceof GL11) {
                GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                
                // Allocate and fill the vertex buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mVertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertBufferIndex);
                final int vertexSize = mFloatVertexBuffer.capacity() * FLOAT_SIZE; 
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
                        mFloatVertexBuffer, GL11.GL_DYNAMIC_DRAW);
                
                // Allocate and fill the texture coordinate buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mTextureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 
                        mTextureCoordBufferIndex);
                final int texCoordSize = 
                    mFloatTexCoordBuffer.capacity() * FLOAT_SIZE;
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texCoordSize, 
                        mFloatTexCoordBuffer, GL11.GL_DYNAMIC_DRAW);    
                
             // Allocate and fill the color buffer.
                if(useColor){
	                gl11.glGenBuffers(1, buffer, 0);
	                mColorBufferIndex = buffer[0];
	                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 
	                		mColorBufferIndex);
	                final int colorSize = 
	                    mFloatColorBuffer.capacity() * FLOAT_SIZE;
	                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, colorSize, 
	                        mFloatColorBuffer, GL11.GL_DYNAMIC_DRAW);    
                }
                // Unbind the array buffer.
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                
                // Allocate and fill the index buffer.
                gl11.glGenBuffers(1, buffer, 0);
                mIndexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 
                        mIndexBufferIndex);
                // A char is 2 bytes.
                final int indexSize = mIndexBuffer.capacity() * 2;
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, mIndexBuffer, 
                        GL11.GL_STATIC_DRAW);
                
                // Unbind the element array buffer.
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
                
                mUseHardwareBuffers = true;
                
                assert mVertBufferIndex != 0;
                assert !useColor || mColorBufferIndex != 0;
                assert mTextureCoordBufferIndex != 0;
                assert mIndexBufferIndex != 0;
                assert gl11.glGetError() == 0;
                
            
            }
        }
    }
    

	protected static class DrawableBufferComparator implements Comparator<DrawableBuffer> {

		@Override
		public int compare(DrawableBuffer object1, DrawableBuffer object2) {
			if(object1.startLayer < object2.startLayer){
				return -1;
			}
			else if(object1.startLayer > object2.startLayer){
				return 1;
			}
			
			return 0;
		}
		
	}
}
