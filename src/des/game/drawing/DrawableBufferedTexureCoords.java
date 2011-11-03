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

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import des.game.base.DebugLog;

import android.content.res.XmlResourceParser;
import java.util.HashMap;
public class DrawableBufferedTexureCoords {
	private static final float GL_MAGIC_OFFSET = 0.375f;
//	final float u = (0 + GL_MAGIC_OFFSET) * texelWidth;
//    final float v = (0 + GL_MAGIC_OFFSET) * texelHeight;
//    final float u2 = ((0 + 32 - GL_MAGIC_OFFSET) * texelWidth);
//    final float v2 = ((0 + 32 - GL_MAGIC_OFFSET) * texelHeight);
	
//	texture[0][0] = texture[2][0] = u;
//    texture[1][0] = texture[3][0] = u2;
//    texture[0][1] = texture[1][1] = v;
//    texture[2][1] = texture[3][1] = v2;
	private static float BUFFER_WIDTH;
	private static float BUFFER_HEIGHT;
	private static HashMap<String, Texture> textureMap;
//	public static float ball[][] = {{GL_MAGIC_OFFSET*(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{GL_MAGIC_OFFSET*(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)}};
//	public static Texture ball_texture = new Texture(ball,32,32);
//	public static float spindle_1[][] = {{      GL_MAGIC_OFFSET *(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{      GL_MAGIC_OFFSET *(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)}};
//	public static float spindle_2[][] = {{(32 + GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(64-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(32 + GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)},{(64-GL_MAGIC_OFFSET)*(1.0f/BUFFER_WIDTH),(32-GL_MAGIC_OFFSET)*(1.0f/BUFFER_HEIGHT)}};
//	public static Texture spindle_1_texture = new Texture(spindle_1,32,32);
//	public static Texture spindle_2_texture = new Texture(spindle_2,32,32);
	
	public static void loadTextureCoords(XmlResourceParser parser){
		try {
			parser.next(); 
		    int eventType = parser.getEventType();
	        float texelWidth = 0;;
	        float texelHeight= 0;
	        textureMap = new HashMap<String,Texture>();
		    while (eventType != XmlPullParser.END_DOCUMENT)
		    {
		    	if(eventType == XmlPullParser.START_TAG){
		    		if(parser.getName().compareTo("image") == 0){
		    			BUFFER_WIDTH = Integer.parseInt(parser.getAttributeValue(null, "width"));
		    			BUFFER_HEIGHT = Integer.parseInt(parser.getAttributeValue(null, "height"));
		    			
		    	        texelWidth = 1.0f / BUFFER_WIDTH;
		    	        texelHeight = 1.0f / BUFFER_HEIGHT;
		    		}
		    		else if(parser.getName().compareTo("coordinates") == 0){
		    			
		    			String name = parser.getAttributeValue(null, "name");
		    			final float x = Integer.parseInt(parser.getAttributeValue(null, "x"));
		    			final float y = Integer.parseInt(parser.getAttributeValue(null, "y"));
		    			final int width = Integer.parseInt(parser.getAttributeValue(null, "width"));
		    			final int height = Integer.parseInt(parser.getAttributeValue(null, "height"));
		    			DebugLog.e("loadTextureCoords", "name: " + name);
		    			final float u = (x + GL_MAGIC_OFFSET) * texelWidth;
		    		    final float v = (y + GL_MAGIC_OFFSET) * texelHeight;
		    		    final float u2 = ((x + width - GL_MAGIC_OFFSET) * texelWidth);
		    		    final float v2 = ((y + height - GL_MAGIC_OFFSET) * texelHeight);
		    		    final float[][] uvs =  { { u, v2 }, { u2, v2 }, { u, v }, { u2, v } };
		    		    //final float[][] uvs =  { { u, v }, { u2, v }, { u, v2 }, { u2, v2 } };
		    		    Texture texture = new Texture(uvs, width, height);
		    		    textureMap.put(name, texture);
		    		}
		    		
		    		
		    	}
		    	eventType = parser.next();
		    }
		}
		catch (XmlPullParserException e) {
			DebugLog.e("loadTextureCoords", "xpp failure");
		} catch (IOException e) {
			DebugLog.e("loadTextureCoords", "IO exception");
		}
		catch (NumberFormatException e){
			DebugLog.e("loadTextureCoords", "number format");
			e.printStackTrace();
			throw e;
		}
	}
	
	public static Texture getTexture(String name){
		return textureMap.get(name);
	}
}
