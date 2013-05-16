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

		//initialize the collision list
		collisionMatrix = new boolean[6][];
		
		collisionMatrix[PhysicsObject.PASSIVE_TYPE] = new boolean[]  {false,false,false,true ,false,false};
		collisionMatrix[PhysicsObject.INVISIBLE_WALL] = new boolean[]{false,false,false,true ,false,true};
		collisionMatrix[PhysicsObject.VISIBLE_WALL] = new boolean[]  {false,false,false,true ,true ,true};
		collisionMatrix[PhysicsObject.MOB] = new boolean[]           {true ,true ,true ,true,true ,true};
		collisionMatrix[PhysicsObject.PROJECTILE] = new boolean[]    {false,false,true ,true ,false,true};
		collisionMatrix[PhysicsObject.MISC] = new boolean[]    {false,true,true ,true ,true,true};
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
	 * adds a new row into the collision matrix, adding a new possible type for boundaries
	 * @param newRow collision values for other types, true if a collision with the other type is allowed, defaults to true
	 * @return
	 */
	protected static int addRow(ArrayList<Boolean> newRow){
//		//if the newRow is too small add true values
//		if(newRow.size() < collisionMatrix.size() + 1){
//			while(newRow.size() < collisionMatrix.size() + 1){
//				newRow.add(true);
//			}
//		}
//		//if the newRow is too large remove values
//		else if(newRow.size() > collisionMatrix.size() + 1){
//			while(newRow.size() > collisionMatrix.size() +1){
//				newRow.remove(newRow.size()-1);
//			}
//		}
//		
//		//now add a new column to the matrix to maintain the square shape, this vaules come from the new row
//		for(int i = 0; i < collisionMatrix.size(); i++){
//			collisionMatrix.get(i).add(newRow.get(i));
//		}
//		//now add the new row
//		collisionMatrix.add(newRow);
		//return the new type id
		return 0;//collisionMatrix.size()-1;
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
	protected static boolean[][] collisionMatrix;
	public static BoundarySet instance;
}
