/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package des.game.base;

/** 
 * ObjectManagers are "group nodes" in the game graph.  They contain child objects, and updating
 * an object manager invokes update on its children.  ObjectManagers themselves are derived from
 * BaseObject, so they may be strung together into a hierarchy of objects.  ObjectManager may 
 * be specialized to implement special types of traversals (e.g. PhasedObjectManager sorts its
 * children).
 */
public class ObjectManager<T extends BaseObject> extends BaseObject {
    protected static final int DEFAULT_ARRAY_SIZE = 64;
    
    protected FixedSizeArray<T> mObjects;
    protected FixedSizeArray<T> mPendingAdditions;
    protected FixedSizeArray<T> mPendingRemovals;

    public ObjectManager() {
        super();
        mObjects = new FixedSizeArray<T>(DEFAULT_ARRAY_SIZE);
        mPendingAdditions = new FixedSizeArray<T>(DEFAULT_ARRAY_SIZE);
        mPendingRemovals = new FixedSizeArray<T>(DEFAULT_ARRAY_SIZE);
    }
    
    public ObjectManager(int arraySize) {
        super();
        mObjects = new FixedSizeArray<T>(arraySize);
        mPendingAdditions = new FixedSizeArray<T>(arraySize);
        mPendingRemovals = new FixedSizeArray<T>(arraySize);
    }
    
    @Override
    public void reset() {
        commitUpdates();
        final int count = mObjects.getCount();
        for (int i = 0; i < count; i++) {
            BaseObject object = mObjects.get(i);
            object.reset();
        }
    }

    public void commitUpdates() {
        final int additionCount = mPendingAdditions.getCount();
        if (additionCount > 0) {
            final Object[] additionsArray = mPendingAdditions.getArray();
            for (int i = 0; i < additionCount; i++) {
                T object = (T)additionsArray[i];
                mObjects.add(object);
            } 
            mPendingAdditions.clear();
        }
        
        final int removalCount = mPendingRemovals.getCount();
        if (removalCount > 0) {
            final Object[] removalsArray = mPendingRemovals.getArray();
    
            for (int i = 0; i < removalCount; i++) {
                T object = (T)removalsArray[i];
                mObjects.remove(object, true);
            } 
            mPendingRemovals.clear();
        }
    }
    
    @Override
    public void update(float timeDelta, BaseObject parent) {
        commitUpdates();
        final int count = mObjects.getCount();
        if (count > 0) {
            final Object[] objectArray = mObjects.getArray();
            for (int i = 0; i < count; i++) {
                T object = (T)objectArray[i];
                object.update(timeDelta, this);
            }
        }
    }

    public final FixedSizeArray<T> getObjects() {
        return mObjects;
    }
    
    public final int getCount() {
        return mObjects.getCount();
    }
    
    /** Returns the count after the next commitUpdates() is called. */
    public final int getConcreteCount() {
        return mObjects.getCount() + mPendingAdditions.getCount() - mPendingRemovals.getCount();
    }
    
    public final T get(int index) {
        return mObjects.get(index);
    }

    public void add(T object) {
        mPendingAdditions.add(object);
    }

    public void remove(T object) {
        mPendingRemovals.add(object);
    }
    public void clear(){
    	this.mObjects.clear();
    	this.mPendingAdditions.clear();
    	this.mPendingRemovals.clear();
    }
    public void removeAll() {
        final int count = mObjects.getCount();
        final Object[] objectArray = mObjects.getArray();
        for (int i = 0; i < count; i++) {
            mPendingRemovals.add((T)objectArray[i]);
        }
        mPendingAdditions.clear();
    }

    /** 
     * Finds a child object by its type.  Note that this may invoke the class loader and therefore
     * may be slow.
     * @param classObject The class type to search for (e.g. BaseObject.class).
     * @return
     */
    public <S> S findByClass(Class<S> classObject) {
        S object = null;
        final int count = mObjects.getCount();
        for (int i = 0; i < count; i++) {
            BaseObject currentObject = mObjects.get(i);
            if (currentObject.getClass() == classObject) {
                object = classObject.cast(currentObject);
                break;
            }
        }
        return object;
    }
    
    protected FixedSizeArray<T> getPendingObjects() {
        return mPendingAdditions;
    }

}
