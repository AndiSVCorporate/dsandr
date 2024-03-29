package ru.jecklandin.duckshot;


import ru.jecklandin.utils.FpsCounter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DuckGame extends Activity {
    
	private static final String TAG = "DuckGame";
	
	public static final String NEW_MATCH = "ru.jecklandin.duckshot.NEW_MATCH";
	public static final String RESUME_MATCH = "ru.jecklandin.duckshot.RESUME_MATCH";
	
	private static final int LVL_DIALOG = 1;
	private static final int PAUSE_DIALOG = 2;
	
	GameField mGf;
    
    Match mMatch;
    DuckApplication mApplication;
    
    private DuckTimer mTimer;
    private FPSPrinter mFpsPr;
    
    SlingView mSling;
    
    private boolean mShownDialog = false;
    private Handler mDialogHandler;
    private Handler mMatchHandler;
     
    public static DuckGame s_instance;	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DuckGame.s_instance = this;
         
        mApplication = (DuckApplication)getApplication();
        
        //kludge ): 
        //check if the bitmaps were not loaded
        if (ImgManager.mCommonImgMap == null) {
        	finish();
        	return;
        }
        
        mGf = new GameField(this);
        mTimer = new DuckTimer(mGf);
        mFpsPr = new FPSPrinter();
         
        setContentView(mGf);
        
        //handles a match's finish
        mMatchHandler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.arg1 == 42) {
					stopMatch();
				}
				return false;
			}
		});  
        
        if (getIntent().getAction().equals(NEW_MATCH)) {
        	mApplication.newMatch(mMatchHandler);
        	SoundManager.getInstance().seekAtZero();
        } else {
        	mApplication.setHandler(mMatchHandler);
        }
        mMatch = mApplication.getCurrentMatch();
        
        mSling = new SlingView(this);    
        getWindow().addContentView(mSling, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        //debug btn
//        LinearLayout lay = (LinearLayout)View.inflate(this, R.layout.btns, null);
//        ImageButton imb = (ImageButton) lay.findViewById(R.id.ImageButton01);
//        getWindow().addContentView(lay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        imb.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				DuckShotModel.getInstance().addRandomDuck();
//			}
//		}); 

        mDialogHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) { //pause dialog was cancelled
					mTimer.setRunning(true);
					mFpsPr.setRunning(true);
					mMatch.resumeMatch();
				}
				mShownDialog = false;
			}
		};
        
        mTimer.start();
//        mFpsPr.start();
        if (getIntent().getAction().equals(NEW_MATCH)) {
        	mMatch.start();
        }
        
//        FlurryAgent.onStartSession(this, "Y965UZQRQDF3DQ122CN5");
    }

	@Override
	protected void onPause() {
		mTimer.setRunning(false);
		mFpsPr.setRunning(false);
		mMatch.pauseMatch();
		
//		Debug.stopMethodTracing();
		
		SoundManager.getInstance().turnMusic(false);
		
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
		if (mMatch == null) {
			finish();
			return;
		}
		mMatch.resumeMatch();
		mTimer.setRunning(!mShownDialog);
		mFpsPr.setRunning(!mShownDialog);
		
//		Debug.startMethodTracing("ducks2");
		
		SoundManager.getInstance().turnMusic(true);
		
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
		SoundManager.getInstance().turnMusic(false);
		showDialog(LVL_DIALOG);
		mShownDialog = true;
	}
	
	public static Match getCurrentMatch() {
		return DuckGame.s_instance.mMatch;
	}
	

	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == LVL_DIALOG) {
			return new LevelCompletedDialog(this);
		} else if (id == PAUSE_DIALOG) {
			return new PauseDialog(this, mDialogHandler);
		}
		return super.onCreateDialog(id);
	}

	/**
	 * Called from LevelCompletedDialog
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction().equals(NEW_MATCH)) {
			mApplication.newMatch(mMatchHandler); 
			mMatch = mApplication.getCurrentMatch();
			mMatch.start();
			mShownDialog = false;
		}
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
				m_view.postInvalidate();
			}
			try {
				sleep(1000 / DuckApplication.FPS);
			} catch (InterruptedException e) {
				Log.d("DuckTimer", "Stopping");
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