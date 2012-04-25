package des.game.scale;

import des.game.base.BaseObject;
import des.game.base.DebugLog;
import des.game.base.GLPoint;
import des.game.base.Vector2;
import des.game.boundary.Rectangle;
import des.game.drawing.DrawableBitmap;
import des.game.drawing.Texture;



public class InputTouchButton extends InputTouchEvent{
	
	// buttons location on screen
	public Vector2 buttonLocation;
	public Vector2 indicatorOffset;
	private Vector2 indicatorTemp = new Vector2();
	private GLPoint tempPoint = new GLPoint();
	
	// optional textures for the button
	public DrawableBitmap idleIcon;
    public DrawableBitmap pressedIcon;
    public DrawableBitmap indicatorIcon;
    public int priority = 100;
    
    // area covered by the button
    public Rectangle area;
    
    // the touch information relative to the buttons location
    private float relativeX;
    private float relativeY;
    private float rawRelativeX;
    private float rawRelativeY;
    public InputTouchButton(int x, int y, int height, int width){
    	super();
    	buttonLocation = new Vector2(x,y);
    	indicatorOffset = new Vector2();
    	
    	area = new Rectangle(new GLPoint(x,y + width,0), height, width);
    }
    
    
	public boolean CaptureEvent(int id, float currentTime, float x, float y){
		boolean rtn = false;
		tempPoint.setX((double)x);
		tempPoint.setY((double)y);
		if(area.collision(tempPoint)){
			rtn = true;
			boolean wasIdle = this.state.equals(TouchState.IDLE);
			this.id = id;
			this.press(currentTime, x, y);
			
			if(wasIdle){
				this.state = TouchState.START;
			}

		
		}
		return rtn;
	}
	public float getRelativeX(){
		return relativeX;
	}
	public float getRelativeY(){
		return relativeY;
	}
	private void setRelativeLocation(){
		float halfWidth = (float)area.width/2;
		float halfHeight =(float) area.height/2;
		rawRelativeX = relativeX = x - (float)(buttonLocation.x + halfWidth);
		if(relativeX > halfWidth){
			relativeX = halfWidth;
		}
		else if(relativeX < -halfWidth){
			relativeX = -halfWidth;
		}
		
		rawRelativeY = relativeY = y - (float)(buttonLocation.x + halfHeight);
		if(relativeY > halfHeight){
			relativeY = halfHeight;
		}
		else if(relativeY < -halfHeight){
			relativeY = -halfHeight;
		}

	}
	
	public void press(float currentTime, float x, float y) {
		//if(this.state.equals(TouchState.IDLE))
			//DebugLog.d("touch", "start button " + id);
		//else{
			//DebugLog.d("touch", "press");
		//}
		super.press(currentTime, x, y);

		this.setRelativeLocation();
	}
	public void release(float currentTime, float x, float y) {
		super.release(currentTime, x, y);
		//DebugLog.d("touch", "release button " + id);
		this.setRelativeLocation();
	}
	public void drawButton(){
		final RenderSystem render = BaseObject.sSystemRegistry.renderSystem;
		
		if(this.state.equals(TouchState.IDLE)){
			if(idleIcon != null){
				if(idleIcon.getWidth() == 0){
					Texture tex = idleIcon.getTexture();
					idleIcon.resize(tex.width, tex.height);
				}
				render.scheduleForDraw(idleIcon, buttonLocation, priority, false);
			}
		}
		else{
			if(pressedIcon != null){
				if(pressedIcon.getWidth() == 0){
					Texture tex = pressedIcon.getTexture();
					pressedIcon.resize(tex.width, tex.height);
				}
				render.scheduleForDraw(pressedIcon, buttonLocation, priority, false);
			}
		}

		if(indicatorIcon != null){
			if(indicatorIcon.getWidth() == 0){
				Texture tex = indicatorIcon.getTexture();
				indicatorIcon.resize(tex.width, tex.height);
			}
			float halfWidth = (float)area.width/2;
			float halfHeight =(float) area.height/2;
			indicatorTemp.set(indicatorOffset);
			indicatorTemp.add(relativeX + buttonLocation.x + halfWidth, relativeY + buttonLocation.y + halfHeight);
			render.scheduleForDraw(indicatorIcon, indicatorTemp, priority+1, false);
		}
	}
	
}
