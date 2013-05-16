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

package des.game.scale;


import des.game.base.AllocationGuard;
import des.game.base.BaseObject;
import des.game.base.DebugLog;
import des.game.base.FixedSizeArray;
import des.game.base.ObjectManager;
import des.game.base.VectorPool;
import des.game.drawing.BufferLibrary;
import des.game.drawing.DrawableBuffer;
import des.game.drawing.DrawableFactory;
import des.game.drawing.OpenGLSystem;
import des.game.drawing.TextureLibrary;
import des.game.physics.BoundarySet;
import des.game.physics.FieldSet;
import des.game.physics.PhysicsEngine;
import des.game.physics.PhysicsObjectSet;
import des.game.physics.VectorObjectSet;





import android.content.Context;

import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;


/**
 * High-level setup object for the AndouKun game engine.
 * This class sets up the core game engine objects and threads.  It also passes events to the
 * game thread from the main UI thread.
 */
public abstract class Game extends AllocationGuard {
	protected GameThread mGameThread;
    protected Thread mGame;
    protected ObjectManager<BaseObject> mGameRoot; 
    
    protected GameRenderer mRenderer;
    protected GLSurfaceView mSurfaceView;
    protected boolean mRunning;
    protected boolean mBootstrapComplete;

    protected boolean mGLDataLoaded;
    protected ContextParameters mContextParameters;
    protected TouchFilter mTouchFilter;
     
    protected Level currentLevel;
    protected Level pendingLevel;
    public Game() {
        super();
        mRunning = false;
        mBootstrapComplete = false;
        mGLDataLoaded = false;
        mContextParameters = new ContextParameters();
    }
    
    protected abstract void extentionBootstrap();
    protected abstract LevelSystem createLevelSystem();
    protected abstract ScaleObjectFactory createObjectFactory();
    /** 
     * Creates core game objects and constructs the game engine object graph.  Note that the
\
     * game does not actually begin running after this function is called (see start() below).
     * Also note that textures are not loaded from the resource pack by this function, as OpenGl
     * isn't yet available.
     * @param context
     */
    public void bootstrap(Context context, int viewWidth, int viewHeight, int gameWidth, int gameHeight, int difficulty) {
        if (!mBootstrapComplete) {
            mRenderer = new GameRenderer(context, this, gameWidth, gameHeight);
    
            // Create core systems
            BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);
            
            
         
            
            ContextParameters params = mContextParameters;
            params.viewWidth = viewWidth;
            params.viewHeight = viewHeight;
            params.gameWidth = gameWidth;
            params.gameHeight = gameHeight;
            params.viewScaleX = (float)viewWidth / gameWidth;
            params.viewScaleY = (float)viewHeight / gameHeight;
            params.context = context;
            params.difficulty = difficulty;
            BaseObject.sSystemRegistry.contextParameters = params;
            
            final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            	mTouchFilter = new SingleTouchFilter();
            } else {
            	mTouchFilter = new MultiTouchFilter();
            }
    
            // Short-term textures are cleared between levels.
            TextureLibrary shortTermTextureLibrary = new TextureLibrary();
            BaseObject.sSystemRegistry.shortTermTextureLibrary = shortTermTextureLibrary;
            
            // Long-term textures persist between levels.
            TextureLibrary longTermTextureLibrary = new TextureLibrary();
            BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;
            
            // The buffer library manages hardware VBOs.
            BaseObject.sSystemRegistry.bufferLibrary = new BufferLibrary();
            BaseObject.sSystemRegistry.drawableBuffer = new FixedSizeArray<DrawableBuffer>(DrawableBuffer.MAX_BUFFERS, DrawableBuffer.drawableBufferComparator);
           

            
            
            
            BaseObject.sSystemRegistry.soundSystem = new SoundSystem();
            
            // The root of the game graph.
            MainLoop gameRoot = new MainLoop();
            mGameRoot = gameRoot;
            InputSystem input = new InputSystem();
            BaseObject.sSystemRegistry.inputSystem = input;
            BaseObject.sSystemRegistry.registerForReset(input);
            
            WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE); 
            int rotationIndex = windowMgr.getDefaultDisplay().getOrientation(); 
            input.setScreenRotation(rotationIndex);

            InputGameInterface inputInterface = new InputGameInterface();
            gameRoot.add(inputInterface);
            BaseObject.sSystemRegistry.inputGameInterface = inputInterface;
            

            PhysicsEngine.initialize(100, 100, 100, 100);
            BaseObject.sSystemRegistry.physicsEngine = PhysicsEngine.instance;
            gameRoot.add(PhysicsEngine.instance);

            GameObjectManager gameManager = new GameObjectManager(params.viewWidth * 2);
            BaseObject.sSystemRegistry.gameObjectManager = gameManager;
            
            ScaleObjectFactory objectFactory = this.createObjectFactory();
            BaseObject.sSystemRegistry.gameObjectFactory = objectFactory;
            
            

            
            BaseObject.sSystemRegistry.channelSystem = new ChannelSystem();
            BaseObject.sSystemRegistry.registerForReset(BaseObject.sSystemRegistry.channelSystem);
            
            CameraSystem camera = new CameraSystem();
            
    
            BaseObject.sSystemRegistry.cameraSystem = camera;
            BaseObject.sSystemRegistry.registerForReset(camera);
            
            LevelSystem level = this.createLevelSystem();
            BaseObject.sSystemRegistry.levelSystem = level;
            
            this.extentionBootstrap();
            
            BaseObject.sSystemRegistry.drawableBuffer.sort(false);
            
            gameRoot.add(gameManager);
    
            // Camera must come after the game manager so that the camera target moves before the camera 
            // centers.
            
            gameRoot.add(camera);

    
            // More basic systems.

            
            
            RenderSystem renderer = new RenderSystem();
            BaseObject.sSystemRegistry.renderSystem = renderer;
            BaseObject.sSystemRegistry.vectorPool = new VectorPool();
            BaseObject.sSystemRegistry.drawableFactory = new DrawableFactory();
           
    
            BaseObject.sSystemRegistry.vibrationSystem = new VibrationSystem();
            objectFactory.preloadEffects();
            
            mGameThread = new GameThread(mRenderer);
            mGameThread.setGameRoot(mGameRoot);
            currentLevel = null;
         
            mBootstrapComplete = true;
        }
    }
    
    
    public synchronized void stopLevel() {
    	DebugLog.i("Level", "stop level");
        stop();
        GameObjectManager manager = BaseObject.sSystemRegistry.gameObjectManager;
        manager.destroyAll();
        manager.commitUpdates();
        
        PhysicsObjectSet.instance.clear();
		BoundarySet.instance.clear();
		VectorObjectSet.instance.clear();
		FieldSet.instance.clear();
		
        //TODO: it's not strictly necessary to clear the static data here, but if I don't do it
        // then two things happen: first, the static data will refer to junk Texture objects, and
        // second, memory that may not be needed for the next level will hang around.  One solution
        // would be to break up the texture library into static and non-static things, and
        // then selectively clear static game components based on their usefulness next level,
        // but this is way simpler.
        ScaleObjectFactory factory = BaseObject.sSystemRegistry.gameObjectFactory;
        factory.clearStaticData();
        factory.sanityCheckPools();
        
        // Reset the level
        //BaseObject.sSystemRegistry.levelSystem.reset();
        
        // Ensure sounds have stopped.
        BaseObject.sSystemRegistry.soundSystem.stopAll();
        
        // Reset systems that need it.
        BaseObject.sSystemRegistry.reset();
        
        // Dump the short-term texture objects only.
        mSurfaceView.flushTextures(BaseObject.sSystemRegistry.shortTermTextureLibrary);
        BaseObject.sSystemRegistry.shortTermTextureLibrary.removeAll(); 
        mSurfaceView.flushBuffers(BaseObject.sSystemRegistry.bufferLibrary);
        BaseObject.sSystemRegistry.bufferLibrary.removeAll();
    }
    
    public synchronized void requestNewLevel() {
    	// tell the Renderer to call us back when the
    	// render thread is ready to manage some texture memory.
    	mRenderer.requestCallback();
    }
    
    public synchronized void restartLevel() {
    	DebugLog.i("Level", "restart level");
        DebugLog.d("AndouKun", "Restarting...");
        Level level = currentLevel;
        stop();
        
        // Destroy all game objects and respawn them.  No need to destroy other systems.
        GameObjectManager manager = BaseObject.sSystemRegistry.gameObjectManager;
        manager.destroyAll();
        manager.commitUpdates();
        
        // Ensure sounds have stopped.
        BaseObject.sSystemRegistry.soundSystem.stopAll();
        
        // Reset systems that need it.
        BaseObject.sSystemRegistry.reset();
        
        LevelSystem levelSystem = BaseObject.sSystemRegistry.levelSystem;
        
        levelSystem.loadLevel(level, this.mGameRoot);


        start();
    }
    
    protected synchronized void goToLevel() {
    	DebugLog.i("Level", "go to level");
    	DebugLog.d("Game", "goToLevel");
        ContextParameters params = BaseObject.sSystemRegistry.contextParameters;
        BaseObject.sSystemRegistry.levelSystem.loadLevel(currentLevel,
                 mGameRoot);
        
        Context context = params.context;
        mRenderer.setContext(context);
        mSurfaceView.loadTextures(BaseObject.sSystemRegistry.longTermTextureLibrary);
        mSurfaceView.loadTextures(BaseObject.sSystemRegistry.shortTermTextureLibrary);
        mSurfaceView.loadBuffers(BaseObject.sSystemRegistry.bufferLibrary);
        
        mGLDataLoaded = true;
        

        
        
        TimeSystem time = BaseObject.sSystemRegistry.timeSystem;
        time.reset();
        

        
        start();
    }

    /** Starts the game running. */
    public void start() {
        if (!mRunning) {
            assert mGame == null;
            // Now's a good time to run the GC.
            Runtime r = Runtime.getRuntime();
            r.gc();
            DebugLog.d("AndouKun", "Start!");
            mGame = new Thread(mGameThread);
            mGame.setName("Game");
            mGame.start();
            mRunning = true;
            AllocationGuard.sGuardActive = false;
        } else {
            mGameThread.resumeGame();
        }
    }
    
    public void stop() {
        if (mRunning) {
            DebugLog.d("AndouKun", "Stop!");
            if (mGameThread.getPaused()) {
                mGameThread.resumeGame();
            }
            mGameThread.stopGame();
            try {
                mGame.join();
            } catch (InterruptedException e) {
                mGame.interrupt();
            }
            mGame = null;
            mRunning = false;
            //mCurrentLevel = null;
            AllocationGuard.sGuardActive = false;
        }
    }

    public boolean onTrackballEvent(MotionEvent event) {
        if (mRunning) {
        	if (event.getAction() == MotionEvent.ACTION_MOVE) {
        		BaseObject.sSystemRegistry.inputSystem.roll(event.getRawX(), event.getRawY());
        	} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
        		onKeyDownEvent(KeyEvent.KEYCODE_DPAD_CENTER);
        	} else if (event.getAction() == MotionEvent.ACTION_UP) {
        		onKeyUpEvent(KeyEvent.KEYCODE_DPAD_CENTER);
        	}
        }
        return true;
    }
    
    public boolean onOrientationEvent(float x, float y, float z) {
        if (mRunning) {
        	BaseObject.sSystemRegistry.inputSystem.setOrientation(x, y, z);
        }
        return true;
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        if (mRunning) {
        	mTouchFilter.updateTouch(event);
        }
        return true;
    }
    
    
    
    public boolean onKeyDownEvent(int keyCode) {
        boolean result = false;
        if (mRunning) {
            BaseObject.sSystemRegistry.inputSystem.keyDown(keyCode);
        }
        return result;
    }
    
    public boolean onKeyUpEvent(int keyCode) {
        boolean result = false;
        if (mRunning) {
        	BaseObject.sSystemRegistry.inputSystem.keyUp(keyCode);
        }
        return result;
    }

    public GameRenderer getRenderer() {
        return mRenderer;
    }  
    
    public void onPause() {
    	if (mRunning) {
    		mGameThread.pauseGame(); 
    	}
    }
    
    public void onResume(Context context, boolean force) {
		DebugLog.d("Game", "onResume");
    	if (force && mRunning) {
    		mGameThread.resumeGame();
    	} else {
	        mRenderer.setContext(context);
	        // Don't explicitly resume the game here.  We'll do that in
	        // the SurfaceReady() callback, which will prevent the game
	        // starting before the render thread is ready to go.
	        BaseObject.sSystemRegistry.contextParameters.context = context;
    	}
    }
    
    public void onSurfaceReady() {
        DebugLog.d("AndouKun", "Surface Ready");

        if (pendingLevel != null && (currentLevel == null || pendingLevel.compareTo(currentLevel) != 0)) {
            if (mRunning) {
                stopLevel();
            }
            currentLevel = pendingLevel;
            goToLevel();
        } else 
        if (mGameThread.getPaused() && mRunning) {
            mGameThread.resumeGame();
        } 
        
    }
    
    public void setSurfaceView(GLSurfaceView view) {
        mSurfaceView = view;
    }
    
    public void onSurfaceLost() {
        DebugLog.d("AndouKun", "Surface Lost");
        
        BaseObject.sSystemRegistry.shortTermTextureLibrary.invalidateAll();
        BaseObject.sSystemRegistry.longTermTextureLibrary.invalidateAll();
        BaseObject.sSystemRegistry.bufferLibrary.invalidateHardwareBuffers();

        mGLDataLoaded = false;
    }
    
    public void onSurfaceCreated() {
        DebugLog.d("AndouKun", "Surface Created");
        
        // TODO: this is dumb.  SurfaceView doesn't need to control everything here.
        // GL should just be passed to this function and then set up directly.
        
        if (!mGLDataLoaded && mGameThread.getPaused() && mRunning) {
        	
            mSurfaceView.loadTextures(BaseObject.sSystemRegistry.longTermTextureLibrary);
            mSurfaceView.loadTextures(BaseObject.sSystemRegistry.shortTermTextureLibrary);
            mSurfaceView.loadBuffers(BaseObject.sSystemRegistry.bufferLibrary);
            mGLDataLoaded = true;
        }  
    }
    
    public void setPendingLevel(Level level) {
        this.pendingLevel = level;
    }

	public void setSoundEnabled(boolean soundEnabled) {
		BaseObject.sSystemRegistry.soundSystem.setSoundEnabled(soundEnabled);
	}
	
	public void setControlOptions(boolean clickAttack, 
			boolean tiltControls, int tiltSensitivity, int movementSensitivity, boolean onScreenControls) {
		BaseObject.sSystemRegistry.inputGameInterface.setUseClickForAttack(clickAttack);
		BaseObject.sSystemRegistry.inputGameInterface.setUseOrientationForMovement(tiltControls);
		BaseObject.sSystemRegistry.inputGameInterface.setOrientationMovementSensitivity((tiltSensitivity / 100.0f));
		BaseObject.sSystemRegistry.inputGameInterface.setMovementSensitivity((movementSensitivity / 100.0f));
		BaseObject.sSystemRegistry.inputGameInterface.setUseOnScreenControls(onScreenControls);

	}
	
	public void setSafeMode(boolean safe) {
		mSurfaceView.setSafeMode(safe);
	}
	
	public float getGameTime() {
		return BaseObject.sSystemRegistry.timeSystem.getGameTime();
	}
	


	public boolean isPaused() {
		return (mRunning && mGameThread != null && mGameThread.getPaused());
	}

	public void setKeyConfig(int leftKey, int rightKey, int jumpKey,
			int attackKey) {
		BaseObject.sSystemRegistry.inputGameInterface.setKeys(leftKey, rightKey, jumpKey, attackKey);
	}

}
