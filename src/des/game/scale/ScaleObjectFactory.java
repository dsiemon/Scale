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
package des.game.scale;

import java.util.Comparator;


import des.game.base.BaseObject;
import des.game.base.DebugLog;
import des.game.base.FixedSizeArray;
import des.game.base.GameComponent;
import des.game.base.GameComponentPool;
import des.game.base.GameObject;
import des.game.base.GameObjectAttributes;
import des.game.base.TObjectPool;
import des.game.boundary.Boundary;
import des.game.boundary.Circle;
import des.game.boundary.Polygon;
import des.game.boundary.Rectangle;
import des.game.physics.Field;
import des.game.physics.PhysicsObject;
import des.game.physics.VectorObject;




public abstract class ScaleObjectFactory extends BaseObject{
	protected final static int MAX_GAME_OBJECTS = 384;
    protected final static ComponentPoolComparator sComponentPoolComparator = new ComponentPoolComparator();
    protected FixedSizeArray<FixedSizeArray<BaseObject>> mStaticData;
    protected FixedSizeArray<GameComponentPool> mComponentPools;
    protected GameComponentPool mPoolSearchDummy;
    protected GameObjectPool mGameObjectPool;
    
    
    

    
    public ScaleObjectFactory(int objectTypeCount) {
        super();
        
        mGameObjectPool = new GameObjectPool(MAX_GAME_OBJECTS);
        

        mStaticData = new FixedSizeArray<FixedSizeArray<BaseObject>>(objectTypeCount);
        
        for (int x = 0; x < objectTypeCount; x++) {
            mStaticData.add(null);
        }
        

       
        // TODO: I wish there was a way to do this automatically, but the ClassLoader doesn't seem
        // to provide access to the currently loaded class list.  There's some discussion of walking
        // the actual class file objects and using forName() to instantiate them, but that sounds
        // really heavy-weight.  For now I'll rely on (sucky) manual enumeration.
//        class ComponentClass {
//            public Class<?> type;
//            public int poolSize;
//            public ComponentClass(Class<?> classType, int size) {
//                type = classType;
//                poolSize = size;
//            }
//        }

        
        
        
    }
    
    protected void setupComponentPools(ComponentClass[] componentTypes){
    	mComponentPools = new FixedSizeArray<GameComponentPool>(componentTypes.length, sComponentPoolComparator);
        for (int x = 0; x < componentTypes.length; x++) {
            ComponentClass component = componentTypes[x];
            mComponentPools.add(new GameComponentPool(component.type, component.poolSize));
        }
        mComponentPools.sort(true);
        
        mPoolSearchDummy = new GameComponentPool(Object.class, 1);
    }
    
    @Override
    public void reset() {
        
    }
    
    protected GameComponentPool getComponentPool(Class<?> componentType) {
        GameComponentPool pool = null;
        mPoolSearchDummy.objectClass = componentType;
        final int index = mComponentPools.find(mPoolSearchDummy, false);
        if (index != -1) {
            pool = mComponentPools.get(index);
        }
        return pool;
    }
    
    protected GameComponent allocateComponent(Class<?> componentType) {
        GameComponentPool pool = getComponentPool(componentType);
        assert pool != null;
        GameComponent component = null;
        if (pool != null) {
            component = pool.allocate();
        }
        return component;
    }
    
    protected void releaseComponent(GameComponent component) {
        GameComponentPool pool = getComponentPool(component.getClass());
        assert pool != null;
        if (pool != null) {
            component.reset();
            component.shared = false;
            pool.release(component);
        }
    }
    
    protected boolean componentAvailable(Class<?> componentType, int count) {
    	boolean canAllocate = false;
        GameComponentPool pool = getComponentPool(componentType);
        assert pool != null;
        if (pool != null) {
        	canAllocate = pool.getAllocatedCount() + count < pool.getSize();
        }
        return canAllocate;
    }
    
    protected void releasePhysicsObject(PhysicsObject physicsObject){
    	physicsObject.remove();
    	// release each piece of the object
    	
    	// the vector
    	if(physicsObject.vector != null){
	    	physicsObject.vector.reset();
	    	getComponentPool(VectorObject.class).release(physicsObject.vector);
    	}
    	// the boundary
    	if(physicsObject.boundary != null){
    		
    		if(physicsObject.boundary.getCircle() != null){
    			physicsObject.boundary.getCircle().reset();
    			getComponentPool(Circle.class).release(physicsObject.boundary.getCircle());
    		}
    		if(physicsObject.boundary.getRectangle() != null){
    			physicsObject.boundary.getRectangle().reset();
    			getComponentPool(Rectangle.class).release(physicsObject.boundary.getRectangle());
    		}
    		if(physicsObject.boundary.getPolygon() != null){
    			physicsObject.boundary.getPolygon().reset();
    			getComponentPool(Polygon.class).release(physicsObject.boundary.getPolygon());
    		}
    		
    		physicsObject.boundary.reset();
    		getComponentPool(Boundary.class).release(physicsObject.boundary);
    	}
    	
    	// the field
    	if(physicsObject.field != null){
    		if(physicsObject.field.area.getCircle() != null){
    			physicsObject.field.area.getCircle().reset();
    			getComponentPool(Circle.class).release(physicsObject.field.area.getCircle());
    		}
    		if(physicsObject.field.area.getRectangle() != null){
    			physicsObject.field.area.getRectangle().reset();
    			getComponentPool(Rectangle.class).release(physicsObject.field.area.getRectangle());
    		}
    		if(physicsObject.field.area.getPolygon() != null){
    			physicsObject.field.area.getPolygon().reset();
    			getComponentPool(Polygon.class).release(physicsObject.field.area.getPolygon());
    		}
    		
    		physicsObject.field.area.reset();
    		getComponentPool(Boundary.class).release(physicsObject.field.area);
    		
    		physicsObject.field.reset();
    		getComponentPool(Field.class).release(physicsObject.field);
    	}
    	
    	// the physics object
    	physicsObject.reset();
    	getComponentPool(PhysicsObject.class).release(physicsObject);
    }
    public abstract void preloadEffects();
    
    public void destroy(GameObject object) {
        object.commitUpdates();
        final int componentCount = object.getCount();
        for (int x = 0; x < componentCount; x++) {
            GameComponent component = (GameComponent)object.get(x);
            if (!component.shared) {
                releaseComponent(component);
            }
        }
        object.removeAll();
        object.commitUpdates();
        
        if(object.physcisObject != null){
        	this.releasePhysicsObject(object.physcisObject);
        }
        if(object.attributes != null){
        	object.attributes.reset();
    		getComponentPool(GameObjectAttributes.class).release(object.attributes);
        }
        
        mGameObjectPool.release(object);
    }
    
    protected FixedSizeArray<BaseObject> getStaticData(int ordinal) {
        return mStaticData.get(ordinal);
    }
    
    protected void setStaticData(int ordinal, FixedSizeArray<BaseObject> data) {
        int index = ordinal;
        assert mStaticData.get(index) == null;
        
        final int staticDataCount = data.getCount();

        for (int x = 0; x < staticDataCount; x++) {
            BaseObject entry = data.get(x);
            if (entry instanceof GameComponent) {
                ((GameComponent) entry).shared = true;
            }
        }
        
        mStaticData.set(index, data);
    }
    
    protected void addStaticData(int ordinal, GameObject object, SpriteComponent sprite) {
        FixedSizeArray<BaseObject> staticData = getStaticData(ordinal);
        assert staticData != null;

        if (staticData != null) {
            final int staticDataCount = staticData.getCount();
            
            for (int x = 0; x < staticDataCount; x++) {
                BaseObject entry = staticData.get(x);
                if (entry instanceof GameComponent && object != null) {
                    object.add((GameComponent)entry);
                } else if (entry instanceof SpriteAnimation && sprite != null) {
                    sprite.addAnimation((SpriteAnimation)entry);
                }
            }
        }
    }
    
    public void clearStaticData() {
        final int typeCount = mStaticData.getCount();
        for (int x = 0; x < typeCount; x++) {
            FixedSizeArray<BaseObject> staticData = mStaticData.get(x);
            if (staticData != null) {
                final int count = staticData.getCount();
                for (int y = 0; y < count; y++) {
                    BaseObject entry = staticData.get(y);
                    if (entry != null) {
                        if (entry instanceof GameComponent) {
                            releaseComponent((GameComponent)entry);
                        } 
                    }
                }
                staticData.clear();
                mStaticData.set(x, null);
            }
        }
    }
    
    public void sanityCheckPools() {
        final int outstandingObjects = mGameObjectPool.getAllocatedCount();
        if (outstandingObjects != 0) {
            DebugLog.d("Sanity Check", "Outstanding game object allocations! (" 
                    + outstandingObjects + ")");
            assert false;
        }
        
        final int componentPoolCount = mComponentPools.getCount();
        for (int x = 0; x < componentPoolCount; x++) {
            final int outstandingComponents = mComponentPools.get(x).getAllocatedCount();
            
            if (outstandingComponents != 0) {
                DebugLog.d("Sanity Check", "Outstanding " 
                        + mComponentPools.get(x).objectClass.getSimpleName()
                        + " allocations! (" + outstandingComponents + ")");
                //assert false;
            }
        }
    }
    /** Comparator for game objects objects. */
    protected final static class ComponentPoolComparator implements Comparator<GameComponentPool> {
        public int compare(final GameComponentPool object1, final GameComponentPool object2) {
            int result = 0;
            if (object1 == null && object2 != null) {
                result = 1;
            } else if (object1 != null && object2 == null) {
                result = -1;
            } else if (object1 != null && object2 != null) {
                result = object1.objectClass.hashCode() - object2.objectClass.hashCode();
            }
            return result;
        }
    }
    
    public class GameObjectPool extends TObjectPool<GameObject> {

        public GameObjectPool() {
            super();
        }
        
        public GameObjectPool(int size) {
            super(size);
        }
        
        @Override
        protected void fill() {
            for (int x = 0; x < getSize(); x++) {
                getAvailable().add(new GameObject());
            }
        }

        @Override
        public void release(Object entry) {
            ((GameObject)entry).reset();
            super.release(entry);
        }

    }


}
