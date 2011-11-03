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
package des.game.physics;


import des.game.base.ObjectManager;

/**
 * This is a singleton class intended for the Physics engine class to use, also it will contain a reference to every 
 * VectorObject in use by the Physics engine class
 */
public class VectorObjectSet extends ObjectManager<VectorObject>{
	private VectorObjectSet(int size){
		super(size);
	}
	
	
	protected synchronized static void initializeSet(int size){
		if(instance == null){
			instance = new VectorObjectSet(size);
		}
	}
	public static VectorObjectSet instance;


}
