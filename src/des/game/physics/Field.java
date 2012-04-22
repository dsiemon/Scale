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


import des.game.base.GLPoint;
import des.game.base.GameComponent;
import des.game.boundary.Boundary;
import des.game.base.FieldComponent;

public class Field extends GameComponent{
	public Boundary area;
	public Field(){
		super();
		super.setPhase(GameComponent.ComponentPhases.PHYSICS_FIELD.ordinal());
	}

	public Field(FieldComponent fieldBehavior){
		super();
		this.fieldBehavior = fieldBehavior;
	}

	/**
	 * Gets location of the field.
	 * @return The point were the field is.
	 */
	public GLPoint getLocation(){
		return fieldBehavior.getLocation();
	}
/**
 * gets the area of the fieldBehavior of this object
 * @return
 */
	public Boundary getArea(){
		return area;
	}
	/**
	 * Super classes will determine how the field effects the area. the physics object may or may 
	 *  	not be intersecting with this field's area.
	 * @param vector if the location is in the field then it will be effected.
	 * @return returns true when vector is in the area
	 */
	public boolean handleCollision(PhysicsObject object, int time){
		return fieldBehavior.handleObject(object, time);
	}

	/**
	 * adds this object to the FieldSet.
	 */
	public void add(){
		FieldSet.instance.add(this);
	}
	/**
	 * sets rate at which field strength decreases
	 * @param f
	 */
	public void setFieldStrength(FieldComponent f){
		fieldBehavior = f;
	}
	public void setArea(Boundary b){
		area = b;
	}
	public FieldComponent getFieldStrength(){
		return fieldBehavior;
	}


	public void remove(){
		FieldSet.instance.remove(this);
	}
	public FieldComponent fieldBehavior;
	

	@Override
	public void reset() {
		fieldBehavior = null;
		area = null;
		
	}
	
}
