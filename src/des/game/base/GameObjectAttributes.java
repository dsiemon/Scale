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
package des.game.base;

public class GameObjectAttributes extends GameComponent{
	public float maxHealth;
	public float health;
	public int armor;
	public boolean invulnerable;
	public boolean damageThisFrame;
	private boolean damageFlag;
	boolean status[];
	float resistances[];
	float weaknesses[];

	public GameObjectAttributes(){
		super();
		
		status = new boolean[StatusTypes.COUNT];
		resistances = new float[ElementalTypes.COUNT];
		weaknesses = new float[ElementalTypes.COUNT];
		this.reset();
		this.setPhase(GameComponent.ComponentPhases.UPDATE_ATTRIBUTES.ordinal());
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent){
		if(health > maxHealth) health = maxHealth;
		
		damageThisFrame = damageFlag;
		damageFlag = false;
		
		resetEffects();
	}
	
	@Override
	public void reset() {
		this.resetEffects();
		
		maxHealth = 1f;
		health = 1f;
		armor = 0;
		invulnerable = false;
		damageFlag = damageThisFrame = false;
	}
	
	public void damage(float damage, int type, int pierce){

		if(invulnerable) return;
		int tmpArmor = armor - pierce;
		
		if(tmpArmor < 0) tmpArmor = 0;
		
		float tmpDamage = damage * (1 + weaknesses[type]) * (1 - resistances[type]) - tmpArmor;

		if(tmpDamage > 0){
			damageFlag = true;
			health -= damage;
		}
	}
	
	public void heal(float mag){
		// allow over healing until update is called, that way a heal and a damage on the same frame will behave consistently
		health += mag;
	}
	
	public void resetEffects(){
		for(int i =0; i < status.length; ++i){
			status[i] = false;
		}
		for(int i =0; i < resistances.length; ++i){
			resistances[i] = 0f;
		}
		for(int i =0; i < weaknesses.length; ++i){
			weaknesses[i] = 0f;
		}
	}
	public class ElementalTypes{
		public static final int NORMAL = 0;
		public static final int FIRE = 1;
		public static final int WATER = 2;
		public static final int LIFE = 3;
		public static final int LIGHT = 4;
		public static final int SHADOW = 5;
		
		public static final int COUNT = 6;
	}
	
	public class StatusTypes{
		public static final int FROZEN = 0;
		public static final int BURNING = 1;
		
		public static final int COUNT = 2;
	}

}
