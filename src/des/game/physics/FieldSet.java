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


import des.game.base.BaseObject;
import des.game.base.ObjectManager;


public class FieldSet extends ObjectManager<Field>{
	private FieldSet(int size){
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
		final Object[] setArray = this.mObjects.getArray();
		for(int i = 1; i < count; i++){
			for(int j = i-1; j >= 0 && ((Field)setArray[j+1]).getArea().getXProjection().lessThan(((Field)setArray[j]).getArea().getXProjection()); j--){
				Object temp = setArray[j];
				setArray[j]=setArray[j+1];
				setArray[j+1]=temp;
			}
		}
	}
	
	public void updateBoxes(){
		final int count = this.mObjects.getCount();
		final Object[] fieldArray = this.mObjects.getArray();
		for(int i = 0; i < count; i++){
			final Field field = (Field)fieldArray[i];
			field.area.setBox();
		}
	}
	
	public static void initialize(int size){
		if(FieldSet.instance == null){
			instance = new FieldSet(size);
		}
	}
	
	public static FieldSet instance;
}
