package ru.jecklandin.duckshot;

import ru.jecklandin.utils.FpsCounter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class SFGameField extends SurfaceView implements SurfaceHolder.Callback
{

	/**
     * ������� ���������
     */
    private SurfaceHolder mSurfaceHolder;

    /**
     * �����������
     * @param context
     * @param attrs
     */
    public SFGameField(Context context)
    {
        super(context);

        // ������������� �� ������� Surface
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    /**
     * ��������� ������� ���������
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    	GameManager man = new GameManager(holder, getContext());
    	man.setRunning(true);
    	man.start();
    	
    }

    @Override
    /**
     * �������� ������� ���������
     */
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    /**
     * ����������� ������� ���������
     */
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }
}


class GameManager extends Thread
{
    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 250;

   /** �������, �� ������� ����� �������� */
    private SurfaceHolder mSurfaceHolder;

    /** ��������� ������ (����������� ��� ���. �����, ����� ���� ������� ��������� �����, ����� �����������) */
    private boolean mRunning;

    /** ����� ��������� */
    private Paint mPaint;

    private ObjectDrawer mDrawer;
    
    /**
     * �����������
     * @param surfaceHolder ������� ���������
     * @param context �������� ����������
     */
    public GameManager(SurfaceHolder surfaceHolder, Context context)
    {
        mSurfaceHolder = surfaceHolder;
        mRunning = false;
        mDrawer = ObjectDrawer.getInstance(context);
    }

    /**
     * ������� ��������� ������
     * @param running
     */
    public void setRunning(boolean running)
    {
        mRunning = running;
    }

    @Override
    /** ��������, ����������� � ������ */
    public void run()
    {
    	long lastTime = 0;
        while (mRunning)
        {
        	long c = System.currentTimeMillis();
            Canvas canvas = null;
            try
            {
                // ���������� Canvas-�
                canvas = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder)
                {
                    // ���������� ���������
                	
                	mDrawer.drawObjects(canvas);
                	
                	FpsCounter.notifyDrawing();
                }
                
                
                sleep(20);
            }
            catch (Exception e) { }
            finally
            {
                if (canvas != null)
                {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                lastTime = (System.currentTimeMillis() - c);
                Log.d("FRAME:", "" + lastTime);
            }
        }
    }
}