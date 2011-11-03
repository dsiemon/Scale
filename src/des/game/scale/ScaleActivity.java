package des.game.scale;

import java.lang.reflect.InvocationTargetException;





import des.game.base.BaseObject;
import des.game.base.DebugLog;


import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
public abstract class ScaleActivity extends Activity implements SensorEventListener {

    
    private static final int CHANGE_LEVEL_ID = Menu.FIRST;
    private static final int TEST_ANIMATION_ID = CHANGE_LEVEL_ID + 1;
    private static final int TEST_DIARY_ID = CHANGE_LEVEL_ID + 2;
    private static final int METHOD_TRACING_ID = CHANGE_LEVEL_ID + 3;
    
    public static final int ROLL_TO_FACE_BUTTON_DELAY = 400;
    
    public static final int QUIT_GAME_DIALOG = 0;
    
    // If the version is a negative number, debug features (logging and a debug menu)
    // are enabled.
    public static final int VERSION = -1;

    protected GLSurfaceView mGLSurfaceView;
    protected Game mGame;
    protected boolean mMethodTracing;

    protected SensorManager mSensorManager;
    protected SharedPreferences.Editor mPrefsEditor;
    protected long mLastTouchTime = 0L;
    protected long mLastRollTime = 0L;
    protected View mPauseMessage = null;
    protected View mWaitMessage = null;
    protected View mLevelNameBox = null;
    protected TextView mLevelName = null;
    protected Animation mWaitFadeAnimation = null;
    
    
    private long mSessionId = 0L;
    
    protected abstract void handleCreate();
    protected abstract Game createGame();
    protected abstract void handlePause();
    protected abstract void handleResume();
    
    protected abstract boolean onMenuButton(int keyCode, KeyEvent event);
    protected abstract boolean onBackButton(int keyCode, KeyEvent event);
    protected abstract void extensionGameFlowEvent(int eventCode, int index);
    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
        final boolean debugLogs = prefs.getBoolean(PreferenceConstants.PREFERENCE_ENABLE_DEBUG, false);
        
        if (VERSION < 0 || debugLogs) {
        	DebugLog.setDebugLogging(true);
        } else {
        	DebugLog.setDebugLogging(false);
        }
        
        DebugLog.d("AndouKun", "onCreate scale");
        
        this.handleCreate();
        
        setContentView(R.layout.main);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        mPauseMessage = findViewById(R.id.pausedMessage);
        mWaitMessage = findViewById(R.id.pleaseWaitMessage);
       // mLevelNameBox = findViewById(R.id.levelNameBox);
       // mLevelName = (TextView)findViewById(R.id.levelName);
        mWaitFadeAnimation = AnimationUtils.loadAnimation(this, R.anim.wait_message_fade);

        
        //mGLSurfaceView.setGLWrapper(new GLErrorLogger());
        mGLSurfaceView.setEGLConfigChooser(false); // 16 bit, no z-buffer
        //mGLSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        mGame = this.createGame();
        mGame.setSurfaceView(mGLSurfaceView);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int defaultWidth = 800;
        int defaultHeight = 480;
        if (dm.widthPixels != defaultWidth) {
        	float ratio =((float)dm.widthPixels) / dm.heightPixels;
        	defaultWidth = (int)(defaultHeight * ratio);
        }
        

        
        
        mPrefsEditor = prefs.edit();
        // Make sure that old game information is cleared when we start a new game.
        if (getIntent().getBooleanExtra("newGame", false)) {
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_LEVEL_INDEX);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_LEVEL_COMPLETED);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_LINEAR_MODE);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_TOTAL_GAME_TIME);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_PEARLS_COLLECTED);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_PEARLS_TOTAL);
        	mPrefsEditor.remove(PreferenceConstants.PREFERENCE_ROBOTS_DESTROYED);
			mPrefsEditor.remove(PreferenceConstants.PREFERENCE_DIFFICULTY);
			mPrefsEditor.commit();
        }
        
        
        String tmpLevel = getIntent().getStringExtra("level");
//        if(tmpLevel == null){
//        	tmpLevel = "one";
//        }
        
        mGame.bootstrap(this, dm.widthPixels, dm.heightPixels, defaultWidth, defaultHeight, 0);
        mGame.setPendingLevel(BaseObject.sSystemRegistry.levelSystem.parseLevelId(tmpLevel));
        mGLSurfaceView.setRenderer(mGame.getRenderer());

     
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // This activity uses the media stream.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
          
        mSessionId = prefs.getLong(PreferenceConstants.PREFERENCE_SESSION_ID, System.currentTimeMillis());
   
    }

    
    @Override
    protected void onDestroy() {
        DebugLog.d("AndouKun", "onDestroy()");
        mGame.stopLevel();
        
        
        BaseObject.sSystemRegistry.bufferLibrary.invalidateHardwareBuffers();
        super.onDestroy();
        
    }


    @Override
    protected void onPause() {
        super.onPause();
        DebugLog.d("AndouKun", "onPause");

        hidePauseMessage();
        
        mGame.onPause();
        mGLSurfaceView.onPause();
        mGame.getRenderer().onPause();	// hack!
        
        this.handlePause();
        
        if (mMethodTracing) {
            Debug.stopMethodTracing();
            mMethodTracing = false;
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Preferences may have changed while we were paused.
        SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
        final boolean debugLogs = prefs.getBoolean(PreferenceConstants.PREFERENCE_ENABLE_DEBUG, false);
        
        if (VERSION < 0 || debugLogs) {
        	DebugLog.setDebugLogging(true);
        } else {
        	DebugLog.setDebugLogging(false);
        }
        this.handleResume();
        DebugLog.d("AndouKun", "onResume");
        mGLSurfaceView.onResume();
        mGame.onResume(this, false);
       
        
        final boolean soundEnabled = prefs.getBoolean(PreferenceConstants.PREFERENCE_SOUND_ENABLED, true);
        final boolean safeMode = prefs.getBoolean(PreferenceConstants.PREFERENCE_SAFE_MODE, false);
        final boolean clickAttack = prefs.getBoolean(PreferenceConstants.PREFERENCE_CLICK_ATTACK, true);
        final boolean tiltControls = prefs.getBoolean(PreferenceConstants.PREFERENCE_TILT_CONTROLS, false);
        final int tiltSensitivity = prefs.getInt(PreferenceConstants.PREFERENCE_TILT_SENSITIVITY, 50);
        final int movementSensitivity = prefs.getInt(PreferenceConstants.PREFERENCE_MOVEMENT_SENSITIVITY, 100);
        final boolean onScreenControls = prefs.getBoolean(PreferenceConstants.PREFERENCE_SCREEN_CONTROLS, false);

        final int leftKey = prefs.getInt(PreferenceConstants.PREFERENCE_LEFT_KEY, KeyEvent.KEYCODE_DPAD_LEFT);
        final int rightKey = prefs.getInt(PreferenceConstants.PREFERENCE_RIGHT_KEY, KeyEvent.KEYCODE_DPAD_RIGHT);
        final int jumpKey = prefs.getInt(PreferenceConstants.PREFERENCE_JUMP_KEY, KeyEvent.KEYCODE_SPACE);
        final int attackKey = prefs.getInt(PreferenceConstants.PREFERENCE_ATTACK_KEY, KeyEvent.KEYCODE_SHIFT_LEFT);
        
        mGame.setSoundEnabled(soundEnabled);
        mGame.setControlOptions(clickAttack, tiltControls, tiltSensitivity, movementSensitivity, onScreenControls);
        mGame.setKeyConfig(leftKey, rightKey, jumpKey, attackKey);
        mGame.setSafeMode(safeMode);
        
        if (mSensorManager != null) {
            Sensor orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (orientation != null) {
                mSensorManager.registerListener(this, 
                    orientation,
                    SensorManager.SENSOR_DELAY_GAME,
                    null);
            }
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
    	if (!mGame.isPaused()) {
	        mGame.onTrackballEvent(event);
	        final long time = System.currentTimeMillis();
	        mLastRollTime = time;
    	}
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (!mGame.isPaused()) {
    		mGame.onTouchEvent(event);
	    	
	        final long time = System.currentTimeMillis();
	        if (event.getAction() == MotionEvent.ACTION_MOVE && time - mLastTouchTime < 32) {
		        // Sleep so that the main thread doesn't get flooded with UI events.
		        try {
		            Thread.sleep(32);
		        } catch (InterruptedException e) {
		            // No big deal if this sleep is interrupted.
		        }
		        mGame.getRenderer().waitDrawingComplete();
	        }
	        mLastTouchTime = time;
    	}
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	boolean result = true;
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		result = this.onBackButton(keyCode, event);
    	} else if (keyCode == KeyEvent.KEYCODE_MENU) {
    		result = this.onMenuButton(keyCode, event);
    	} else {
		    result = mGame.onKeyDownEvent(keyCode);
		    // Sleep so that the main thread doesn't get flooded with UI events.
		    try {
		        Thread.sleep(4);
		    } catch (InterruptedException e) {
		        // No big deal if this sleep is interrupted.
		    }
    	}
        return result;
    }
     
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	boolean result = false;
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		result = true;
    	} else if (keyCode == KeyEvent.KEYCODE_MENU){ 
	        if (VERSION < 0) {
	        	result = false;	// Allow the debug menu to come up in debug mode.
	        }
    	} else {
    		result = mGame.onKeyUpEvent(keyCode);
	        // Sleep so that the main thread doesn't get flooded with UI events.
	        try {
	            Thread.sleep(4);
	        } catch (InterruptedException e) {
	            // No big deal if this sleep is interrupted.
	        }
    	}
        return result;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        boolean handled = false;
        // Only allow the debug menu in development versions.
        if (VERSION < 0) {
//	        menu.add(0, CHANGE_LEVEL_ID, 0, R.string.change_level);
//	        menu.add(0, TEST_ANIMATION_ID, 0, R.string.test_animation);
//	        menu.add(0, TEST_DIARY_ID, 0, R.string.test_diary);
//	
//	        menu.add(0, METHOD_TRACING_ID, 0, R.string.method_tracing);
	        handled = true;
        }
        
        return handled;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent i;
        switch(item.getItemId()) {



        case METHOD_TRACING_ID:
            if (mMethodTracing) {
                Debug.stopMethodTracing();
            } else {
                Debug.startMethodTracing("andou");
            }
            mMethodTracing = !mMethodTracing;
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
    

    
    /*
     *  When the game thread needs to stop its own execution (to go to a new level, or restart the
     *  current level), it registers a runnable on the main thread which orders the action via this
     *  function.
     */
    public void onGameFlowEvent(int eventCode, int index) {
       switch (eventCode) {
           case GameFlowEvent.EVENT_END_GAME: 
        	   DebugLog.i("game flow", "end game");
               mGame.stopLevel();
               finish();
               break;
           case GameFlowEvent.EVENT_RESTART_LEVEL:
        	   DebugLog.i("game flow", "restart");
        	   mGame.restartLevel();
        	   break;

        	   default:
        		   this.extensionGameFlowEvent(eventCode, index);
         
        	   
       }
    }
    
    protected void saveGame() {
    	if (mPrefsEditor != null) {
//    		final int completed = LevelTree.packCompletedLevels(mLevelRow);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_LEVEL_ROW, mLevelRow);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_LEVEL_INDEX, mLevelIndex);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_LEVEL_COMPLETED, completed);
//    		mPrefsEditor.putLong(PreferenceConstants.PREFERENCE_SESSION_ID, mSessionId);
//    		mPrefsEditor.putFloat(PreferenceConstants.PREFERENCE_TOTAL_GAME_TIME, mTotalGameTime);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_LAST_ENDING, mLastEnding);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_ROBOTS_DESTROYED, mRobotsDestroyed);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_PEARLS_COLLECTED, mPearlsCollected);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_PEARLS_TOTAL, mPearlsTotal);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_LINEAR_MODE, mLinearMode);
//    		mPrefsEditor.putBoolean(PreferenceConstants.PREFERENCE_EXTRAS_UNLOCKED, mExtrasUnlocked);
//    		mPrefsEditor.putInt(PreferenceConstants.PREFERENCE_DIFFICULTY, mDifficulty);
//    		mPrefsEditor.commit();
    	}
    }
    

    
    protected void showPauseMessage() {
    	if (mPauseMessage != null) {
    		mPauseMessage.setVisibility(View.VISIBLE);
    	}
    	if (mLevelNameBox != null && mLevelName != null) {
    		mLevelName.setText("paused");
    		mLevelNameBox.setVisibility(View.VISIBLE);
    	}
    }
   
    protected void hidePauseMessage() {
    	if (mPauseMessage != null) {
    		mPauseMessage.setVisibility(View.GONE);
    	}
    	if (mLevelNameBox != null) {
    		mLevelNameBox.setVisibility(View.GONE);
    	}
    }
    
    protected void showWaitMessage() {
    	if (mWaitMessage != null) {
    		mWaitMessage.setVisibility(View.VISIBLE);
    		mWaitMessage.startAnimation(mWaitFadeAnimation);
    	}
    }
    
    protected void hideWaitMessage() {
    	if (mWaitMessage != null) {
    		mWaitMessage.setVisibility(View.GONE);
    		mWaitMessage.clearAnimation();
    	}
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        
    }


    public void onSensorChanged(SensorEvent event) {
       synchronized (this) {
           if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
               final float x = event.values[1];
               final float y = event.values[2];
               final float z = event.values[0];
               mGame.onOrientationEvent(x, y, z);
           }
       }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (id == QUIT_GAME_DIALOG) {
        	
            dialog = new AlertDialog.Builder(this)
                .setTitle("")
                .setPositiveButton(R.string.quit_game_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	finish();
                    	if (UIConstants.mOverridePendingTransition != null) {
         	 		       try {
         	 		    	  UIConstants.mOverridePendingTransition.invoke(ScaleActivity.this, R.anim.fade_in, R.anim.fade_out);
         	 		       } catch (InvocationTargetException ite) {
         	 		           DebugLog.d("Activity Transition", "Invocation Target Exception");
         	 		       } catch (IllegalAccessException ie) {
         	 		    	   DebugLog.d("Activity Transition", "Illegal Access Exception");
         	 		       }
         	            }
                    }
                })
                .setNegativeButton(R.string.quit_game_dialog_cancel, null)
                .setMessage(R.string.quit_game_dialog_message)
                .create();
        }
        return dialog;
    }
}
