package ru.jecklandin.duckshot;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Debug;
import android.util.Log;

public class ImgManager {

	private static Context mCtx;
	private static Map<String, Bitmap> mImgMap = new HashMap<String, Bitmap>();
	private static Map<String, Bitmap[]> mAniMap = new HashMap<String, Bitmap[]>();
	  
	public static void loadImages(Context ctx) {
		mCtx = ctx;  
		
		Bitmap bm = getBitmap(R.drawable.wave);
		
		int qq[] = new int[bm.getWidth() * bm.getHeight()];
		bm.getPixels(qq, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight() );
		
		bm = Bitmap.createBitmap(qq, 0, bm.getWidth(), bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
		
		 
		     
		mImgMap.put("wave", bm);
		bm = getBitmap(R.drawable.duck);
		mImgMap.put("duck", bm);
		bm = getBitmap(R.drawable.deadduck);
		mImgMap.put("deadduck", bm);
		bm = getBitmap(R.drawable.desk);
		mImgMap.put("desk", bm);
		bm = getBitmap(R.drawable.stonea);
		mImgMap.put("stone", bm);
		bm = getBitmap(R.drawable.stoneb);
		mImgMap.put("stone2", bm);
		bm = getBitmap(R.drawable.stonec);
		mImgMap.put("stone3", bm);      
		bm = getBitmap(R.drawable.indent);
		mImgMap.put("indent", bm);
		bm = getBitmap(R.drawable.fountain);
		mImgMap.put("fountain", bm);
		bm = getBitmap(R.drawable.power);
		mImgMap.put("power", bm);
		bm = getBitmap(R.drawable.stub); 
		mImgMap.put("stub", bm);
		bm = getBitmap(R.drawable.pointer);
		mImgMap.put("pointer", bm);
		bm = getBitmap(R.drawable.pointerh);
		mImgMap.put("pointerh", bm);
		bm = getBitmap(R.drawable.sun);
		mImgMap.put("sun", bm);
		bm = getBitmap(R.drawable.clouda);
		mImgMap.put("cloud1", bm);
		bm = getBitmap(R.drawable.cloudb);
		mImgMap.put("cloud2", bm);
		bm = getBitmap(R.drawable.cloudc);
		mImgMap.put("cloud3", bm);
		bm = getBitmap(R.drawable.sight);
		mImgMap.put("sight", bm);
		bm = getBitmap(R.drawable.settings_quant);
		mImgMap.put("quant", bm);
		bm = getBitmap(R.drawable.settings_quant_e);
		mImgMap.put("quant_e", bm);
		bm = getBitmap(R.drawable.pl_green);
		mImgMap.put("pl_green", bm);
		bm = getBitmap(R.drawable.pl_yell);
		mImgMap.put("pl_yell", bm);

		
		
		
		bm = getBitmap(R.drawable.anifountain);
		Bitmap[] anim = new Bitmap[8];
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*84, 0, 84, 84);
		}
		mAniMap.put("fountain", anim);
		
		   
		bm = getBitmap(R.drawable.duckdive);
		anim = new Bitmap[16];
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*120, 0, 120, 120);
		}
		mAniMap.put("duckdive", anim);
		 
		
		bm = getBitmap(R.drawable.duckemerge);
		anim = new Bitmap[8];
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*120, 0, 120, 120);
		}
		mAniMap.put("duckemerge", anim);
		
		bm = getBitmap(R.drawable.digits);
		anim = new Bitmap[11];
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*30, 0, 30, 30);
		}
		mAniMap.put("digits", anim);
		
		bm = getBitmap(R.drawable.digits_red);
		anim = new Bitmap[11]; 
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*30, 0, 30, 30);
		}
		mAniMap.put("digits_red", anim);
		
		bm = getBitmap(R.drawable.digits_time);
		anim = new Bitmap[11]; 
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*25, 0, 25, 20);
		}
		mAniMap.put("digits_time", anim);
		
		bm = getBitmap(R.drawable.awards);
		anim = new Bitmap[5]; 
		for (int i=0; i<anim.length; ++i) {
			anim[i] = Bitmap.createBitmap(bm, i*35, 0, 35, 35);
		}
		mAniMap.put("awards", anim);
	}

	private static Bitmap getBitmap(int id) {
		return BitmapFactory.decodeResource(mCtx.getResources(), id);
	}
	
	public static Bitmap getBitmap(String name) {
		return mImgMap.get(name);
	}
	
	public static Bitmap[] getAnimation(String name) {
		return mAniMap.get(name);
	}
}
