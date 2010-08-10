package ru.jecklandin.duckshot;


import com.flurry.android.FlurryAgent;

import ru.jecklandin.duckshot.model.DuckShotModel;
import ru.jecklandin.utils.FpsCounter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DuckGame extends Activity {
    
	private static final String TAG = "DuckGame";
	
	private static final int LVL_DIALOG = 1;
	private static final int PAUSE_DIALOG = 2;
	
	GameField mGf;
    Vibrator mVibro;
    Typeface mTypeface;
    Typeface mHelsTypeface;
    Match mMatch;
//  SFGameField sf;
    
    private DuckTimer mTimer;
    private FPSPrinter mFpsPr;
    
    private SlingView mSling;
    
    private boolean mShownDialog = false;
    private Handler mDialogHandler;
    
   public static DuckGame s_instance;	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScreenProps.initialize(this);
        ImgManager.loadImages(this);
        DuckGame.s_instance = this;
        
        mVibro = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mGf = new GameField(this);
        mTimer = new DuckTimer(mGf);
        mFpsPr = new FPSPrinter();
        
        setContentView(mGf);
        
        //handles a match's finish
        Handler matchHandler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.arg1 == 42) {
					stopMatch();
				}
				return false;
			}
		});
        mMatch = new Match(90, matchHandler);
        
        mSling = new SlingView(this);    
        getWindow().addContentView(mSling, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        //debug btn
        LinearLayout lay = (LinearLayout)View.inflate(this, R.layout.btns, null);
        ImageButton imb = (ImageButton) lay.findViewById(R.id.ImageButton01);
        getWindow().addContentView(lay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        imb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DuckShotModel.getInstance().addRandomDuck();
				
			}
		}); 

        mDialogHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mTimer.setRunning(true);
				mFpsPr.setRunning(true);
				mMatch.resumeMatch();
				mShownDialog = false;
			}
		};
        
        mTimer.start();
        mFpsPr.start();
        mMatch.start();
        
        mTypeface = Typeface.createFromAsset(getAssets(), "Whypo.ttf");
        mHelsTypeface = Typeface.createFromAsset(getAssets(), "helsinki.ttf");

//        FlurryAgent.onStartSession(this, "Y965UZQRQDF3DQ122CN5");
    }

	@Override
	protected void onPause() {
		mTimer.setRunning(false);
		mFpsPr.setRunning(false);
		mMatch.pauseMatch();
		super.onPause();
	}

	@Override
	protected void onStop() {
//		FlurryAgent.onEndSession(this);
//		mTimer.setRunning(false);
//		mFpsPr.setRunning(false);
		super.onStop();
	} 

	@Override
	protected void onResume() {
		mTimer.setRunning(!mShownDialog);
		mFpsPr.setRunning(!mShownDialog);
		mMatch.resumeMatch();
		super.onResume();
	}

    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_MOVE) {  
    		mSling.setXY(event.getX(), event.getY());
    	} else if (event.getAction() == MotionEvent.ACTION_UP) {
    		mSling.shot((int)event.getX(), (int)event.getY()); 
    	} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		mSling.grab((int)event.getX(), (int)event.getY());
    	}
		return super.onTouchEvent(event);
	}    
	
	@Override     
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showPauseDialog();
		}
		return super.onKeyDown(keyCode, event);
	}    
	

	private void showPauseDialog() {
		mTimer.setRunning(false);
		mFpsPr.setRunning(false);
		mMatch.pauseMatch();
		showDialog(PAUSE_DIALOG);
		mShownDialog = true;
	}

	void stopMatch() {
		mTimer.setRunning(false);
		showDialog(LVL_DIALOG);
	}
	
	public static Match getCurrentMatch() {
		return DuckGame.s_instance.mMatch;
	}
	
	public static Vibrator getVibrator() {
		return DuckGame.s_instance.mVibro;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == LVL_DIALOG) {
			return new LevelCompletedDialog(this, mMatch);
		} else if (id == PAUSE_DIALOG) {
			return new PauseDialog(this, mDialogHandler);
		}
		return super.onCreateDialog(id);
	}
} 

class DuckTimer extends Thread {
	
	View m_view;
	boolean mRunning = true;
	
	DuckTimer(View v) {
		m_view = v;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
 
			if (mRunning) {
				ObjectDrawer.sLastTick = System.currentTimeMillis();
//				if (!ObjectDrawer.lock) 
				m_view.postInvalidate();
			}
			try {
				sleep(30);
			} catch (InterruptedException e) {
				Log.d("DuckTimer", "Stopping");
				// e.printStackTrace();
				return;
			}

		}
	}

	public void setRunning(boolean run) {
		mRunning = run;
	}
}

class FPSPrinter extends Thread {

	boolean mRunning = true;

	@Override
	public void run() {
		while (!isInterrupted()) {
			if (mRunning) {
				Log.d("Ducks ### FPS", FpsCounter.getFPS() + "");
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				Log.d("FPS", "Stopping");
				return;
			}

		}

	}

	public void setRunning(boolean run) {
		mRunning = run;
	}
}