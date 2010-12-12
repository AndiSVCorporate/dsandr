package ru.jecklandin.duckshot.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.flurry.android.FlurryAgent;

import android.content.Context;
import android.util.Log;
import ru.jecklandin.duckshot.*;
import ru.jecklandin.duckshot.GameObject.OBJ_TYPE;

public class DuckShotModel {

	private static String TAG = "DuckShotModel";
	  
	// Game objects 
	public Vector<Wave> mWaves = new Vector<Wave>();
	public Vector<Stone> mStones = new Vector<Stone>();
	public Vector<Integer> mYes = new Vector<Integer>();
	
	private static DuckShotModel s_instance;
	public static synchronized DuckShotModel getInstance() {
		if (DuckShotModel.s_instance == null) {
			DuckShotModel.s_instance = new DuckShotModel();
		}
		return s_instance;
	}

	public static final int WAVES_NUM = 10;
	public static int WAVES_HEIGHT = 0;
	public static final int MAX_MSEC = 1999;
	public static int WAVES_OFFSET;
	private static final int WAVES_GAP = ScrProps.scale(28);
	
	public DuckShotModel() {
		WAVES_OFFSET = ScrProps.screenHeight - WAVES_NUM * WAVES_GAP - Desk.getInstance().mDesk.getHeight() - ScrProps.scale(80); 
		WAVES_HEIGHT = WAVES_NUM * WAVES_GAP;
		
		// loading Y-coord
		for (int i=0; i<WAVES_NUM; ++i) {
			mYes.add(WAVES_OFFSET + i * WAVES_GAP);
		}
		
		reinitialize();
	}
	
	public void reinitialize() {
		mWaves.removeAllElements();
		for (int i=0; i<mYes.size(); ++i) {
			// -50 .. +50
			int mx = (int) (Math.random()*50 - 50);
			// 1 .. 5
			int ms = i / 2;
			mWaves.add(new Wave(mx, mYes.get(i), ms));
		}
		
		for (int i=0; i<1; ++i) {
			addRandomDuck();
		}
	}
	
	@Deprecated
	public void launchStone(int x, long msec) {
		Stone stone = new Stone(x, getYFromMsec(msec));
		synchronized (mStones) { 
			mStones.add(stone); 
		}
		
		checkForCollide(stone, mYes.size() - 1 - getYNumFromMsec(msec));
	}
	
	@Deprecated
	public void launchStone(int wave_number, int x) {
		Log.d(TAG, "!!!"+x);
		Stone stone = new Stone(x, mWaves.get(wave_number).y);
		mStones.add(stone);
		checkForCollide(stone, wave_number);
		Map<String, String> map = new HashMap<String, String>() ;
		map.put("wave_number", ""+wave_number);
		map.put("x", ""+x);
		FlurryAgent.onEvent("shot", map);
	}
	/**
	 * Final point of stone's flight
	 * @param msec
	 * @return
	 */
	@Deprecated
	public int getYFromMsec( long msec ) {
		return mYes.get(mYes.size() - 1 - getYNumFromMsec(msec));
	}
	
	@Deprecated
	public int getYNumFromMsec( long msec ) {
		//msec is 1999 max
		//1999 / 10yes = 200  
		final int MULT = 200;
		
		//matching Y-es with mseconds
		int y = (int) (msec / 200);
		return y;
	}
	
	public int getTopY() {
		return mYes.get(0);
	}
	
	public int getBottomY() {
		return mYes.get(mYes.size()-1) + WAVES_GAP;
	}
	
	private void checkForCollide(Stone stone, int ny) {
		for (Duck duck : mWaves.get(ny).ducks) {  
			duck.throwStone(stone); 
		} 
	}
	
	/**
	 * Adds new duck to the given wave
	 * @param wave_num
	 * @return x is it able to place one more duck, -1 otherwise
	 */
	public int addDuck(int wave_num) {
		Duck d = new Duck( 0 );
		return addDuck(d, wave_num);
	}
	
	/**
	 * Adds existing duck to the given wave
	 * @param d
	 * @param wave_num
	 * @return -1 if can't, x otherwise
	 */
	public int addDuck(Duck d, int wave_num) {
		int randx;
		int tries = 3; 
		do {
			randx = (int) (Math.random() * ScrProps.screenWidth);
			if (--tries < 0) {
				return -1;
			}
		} while (!addDuck(d, wave_num, randx));
		
		return randx;
	}
	
	/**
	 * Try to add the duck to the specified place
	 * @param d
	 * @param wave_num
	 * @param x
	 * @return
	 */
	public boolean addDuck(Duck d, int wave_num, int x) {
		Wave ownedWave = mWaves.get(wave_num);
		if (!ownedWave.isPlaceFree(x)) {
			return false;
		}
		
		d.mScoreValue = 50 + 10*(mWaves.size() - 1 - wave_num);
		d.ownedWave =  ownedWave;
		d.offset = x;
		d.ownedWave.addDuck(d);
		return true;
	}
	
	/**
	 * 
	 * @param duck
	 * @return distance
	 */
	public int moveDuckToRandomWave(Duck duck) {
		Duck d = duck;
		Wave wave = duck.ownedWave;
		int wasy = d.ownedWave.y;
		int wasx = (int) d.ownedWave.offset;
		duck.ownedWave.removeDuck(duck);
		int randy = 0;
		int randx = 0; 
		// look for free wave
		do {
			randy = (int) (Math.random() * mWaves.size());
			if (randy == mWaves.indexOf(wave)) {  //we want another wave
				randy -= (randy==0 ? -1 : 1);
			}
			randx = (int) (Math.random() * ScrProps.screenWidth);;
		} while ( ! addDuck(d, randy, randx));
		
		
		int ydistance = Math.abs(wasy - mWaves.get(randy).y);
		int xdistance = Math.abs(wasx - randx);
		Log.d(TAG, "Moved duck "+ydistance+" | "+xdistance);
		Log.d(TAG, "Dist "+Math.hypot(xdistance, ydistance));
		return (int) Math.hypot(xdistance, ydistance);
	}

	public void addRandomDuck() {
		int randy;
		// look for free wave
		do {
			randy = (int) (Math.random() * mWaves.size());
		} while (addDuck(randy) < 0);
	}

	public void cleanup() {
		boolean need_gc = false;
		synchronized (mStones) {
			Iterator<Stone> it = mStones.iterator();
			while (it.hasNext()) {
				if  (it.next().sank) {
					need_gc = true;
					it.remove();
				}
			}
		}
		
		if (need_gc) {
			//System.gc();
		}
	}

	public int getTimeoutByDistance(int distance) {
		int maxtm = 80; //80 drawings is max timeout
		int maxy = mWaves.get(mWaves.size()-1).y - mWaves.get(0).y;
		int maxx = ScrProps.screenWidth;
		int maxdist = (int) Math.hypot(maxx, maxy);
		int timeout = distance * maxtm / maxdist;
		return timeout;
	}
	

}