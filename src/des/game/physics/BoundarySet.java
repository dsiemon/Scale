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

import java.util.ArrayList;

import des.game.base.BaseObject;
import des.game.base.ObjectManager;


public class BoundarySet extends ObjectManager<PhysicsObject>{
	private BoundarySet(int size){
		super(size);

	}
	@Override
    public void update(float timeDelta, BaseObject parent) {
        commitUpdates();
    }
/**
 * performs an insertion sort on the set. after the physics engine moves the vectors the order of the 
 * 	boundaries is assumed to change minimally, so the set should be already be near sorted when this is
 *  called in the physics engine.
 */
	public void sort(){
		final int count = this.mObjects.getCount();
		final Object[] physicsArray = this.mObjects.getArray();
		for(int i = 1; i < count; i++){
			for(int j = i-1; j >= 0 && ((PhysicsObject)physicsArray[j+1]).getBoundary().getXProjection().lessThan(((PhysicsObject)physicsArray[j]).getBoundary().getXProjection()); j--){
				Object temp = physicsArray[j];
				physicsArray[j]=physicsArray[j+1];
				physicsArray[j+1]=temp;
			}
		}
	}
	public void updateBoxes(){
		final int count = this.mObjects.getCount();
		final Object[] physicsArray = this.mObjects.getArray();
		for(int i = 0; i < count; i++){
			final PhysicsObject physicsObject = (PhysicsObject)physicsArray[i];
			physicsObject.getBoundary().setBox();
		}
	}

	/**
	 * checks the value of the collisionMatrix at i and j
	 * @param i the first type, i,j = j,i
	 * @param j the second type
	 * @return value of the boolean reflects whether or not a collision is allowed for those types
	 */
	protected static boolean checkMatrix(int i, int j){
		boolean flag = false;
		try{
			//set the flag to the value of the matrix
			flag = collisionMatrix[i][j];
		}
		catch(IndexOutOfBoundsException e){
			
		}
		return flag;
	}

	protected synchronized static void initializeSet(int size){
		if(instance == null){
			instance = new BoundarySet(size);
		}
	}
	public static boolean[][] collisionMatrix;
	public static BoundarySet instance;
}
