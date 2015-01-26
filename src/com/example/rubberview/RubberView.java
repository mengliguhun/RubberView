package com.example.rubberview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class RubberView extends TextView {
	private Paint mPaint;
	private int mLayerColor = Color.parseColor("#c0c0c0");
    private float mStrokeWidth = 20;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;
	private boolean isComplete;
	private int mWidth;
    private int mHeight;
	/**
	 * 计算百分比 去掉图层
	 */
	private Thread mThread ;
	public RubberView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RubberView(Context context) {
		super(context);
		init();
	}
	/**
	 * 初始化
	 */
	private void init() {
		// TODO Auto-generated method stub
	
        mPath = new Path();
        mBitmapPaint = new Paint();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setDither(true);// 递色
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND); // 前圆角
        mPaint.setStrokeCap(Paint.Cap.ROUND); // 后圆角
        mPaint.setStrokeWidth(mStrokeWidth); // 笔宽
        
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		
	}
	/**
	 * 图层初始化
	 * @param w
	 * @param h
	 */
	private void initLayerBitmap(int w,int h) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mCanvas.drawColor(mLayerColor);
		
		mThread = new Thread(mRunnable);
        mThread.start();
	}
	/**
	 * 重置绘图
	 */
	public void reset() {
		isComplete =false;
		mPath.reset();
		initLayerBitmap(mWidth, mHeight);
		invalidate();
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		initLayerBitmap(w, h);
		
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
//		
		if (!isComplete) {
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			mCanvas.drawPath(mPath, mPaint);
		}

	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private void touch_start(float x, float y) {

		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		// commit the path to our offscreen
		mCanvas.drawPath(mPath, mPaint);
		// kill this so we don't double draw
		mPath.reset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			 touch_up();
             invalidate();
			break;
		}
		return true;
	}

	/**
	 * 统计擦除区域任务
	 */
	private Runnable mRunnable = new Runnable() {
		private int[] mPixels;

		@Override
		public void run() {

			int w = getWidth();
			int h = getHeight();

			float totalArea = w * h;

			mPixels = new int[w * h];

			/**
			 * 拿到所有的像素信息
			 */
			
			while (!isComplete) {
				SystemClock.sleep(100);
				float wipeArea = 0;
//				Bitmap bitmap = mBitmap;
				mBitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
				/**
				 * 遍历统计擦除的区域
				 */
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						int index = i + j * w;
						if (mPixels[index] == 0) {
							wipeArea++;
						}
					}
				}

				/**
				 * 根据所占百分比，进行一些操作
				 */
				if (wipeArea > 0 && totalArea > 0) {
					int percent = (int) (wipeArea * 100 / totalArea);

					if (percent > 30) {
						isComplete = true;
						postInvalidate();
					}
				}
				
			}
			
		}

	};
}
