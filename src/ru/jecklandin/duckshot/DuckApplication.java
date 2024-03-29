package ru.jecklandin.duckshot;

import ru.jecklandin.duckshot.levels.Level;
import ru.jecklandin.duckshot.model.DuckShotModel;
import ru.jecklandin.duckshot.units.Obstacle;
import android.app.Application;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;

public class DuckApplication extends Application {

	private Match mCurrentMatch;
    private static Typeface mCommonTypeface;
    private static SoundPool mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    private static DuckApplication sInstance;
    private static int MUSIC_ID;
    
    public static final String FLURRY_KEY = "TG7D2CT31BQI2GFNSLEP";
    public static final int FPS = 23;
	
    private Level mCurrentLevel;
    
	@Override
	public void onCreate() {
		super.onCreate();
		mCommonTypeface = Typeface.createFromAsset(getAssets(), "KOMIKAX_.ttf");
		DuckApplication.sInstance = this;
	}
	 
	public static DuckApplication getInstance() {
		return sInstance;
	}
	
	public static Typeface getCommonTypeface() {
		return mCommonTypeface;
	}
	
	public Match getCurrentMatch() {
		return mCurrentMatch;
	}
	
	public void newMatch(Handler han) {
		mCurrentMatch = new Match(mCurrentLevel.mLevelTime, han);
	}

	public void setHandler(Handler han) {
		if (mCurrentMatch!=null) {
			mCurrentMatch.setHandler(han);
		}
	}
	
	public void setLevel(Level level) {
		if (mCurrentLevel != null) {
			mCurrentLevel.unloadResources();
			System.gc();
		}
		
		mCurrentLevel = level;
		ImgManager.loadLevelResources(level); 
		SoundManager.getInstance().loadSounds(level);
		Obstacle.initBitmaps();
		
		DuckShotModel.getInstance().reinitialize(level);
		level.initItemsBitmaps();
		
		Bundle lvlSettings = getCurrentLevel().getSettings();
		
		ObjectDrawer.getInstance(this).mCreaturesOnTop 
			= lvlSettings.getBoolean("creaturesOnTop");
		Stone.sDestroyedByGround = lvlSettings.getBoolean("destroyedByGround");
		
		if (DuckGame.s_instance != null) {
			DuckGame.s_instance.mSling.initConstants();
		}
	}
	
	public Level getCurrentLevel() {
		return mCurrentLevel;
	}
	
}
