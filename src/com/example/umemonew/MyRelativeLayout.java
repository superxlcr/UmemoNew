package com.example.umemonew;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout {

	private static final int LEFT_DISTANCE = 20;// 滑动图片的X坐标到屏幕的边，即X坐标的值，或者说滑动图片的滑动区域到屏幕左边框的距离。
	private Context mContext;
	private Bitmap drawBitmap;// 滑动图片
	private ImageView leftImageView;// 左侧控件
	private ImageView rightImageView;// 右侧控件
	private int locationX;// 点击位置
	private static final int MOVE_TIME = 10;// 移动时间
	private static final float MOVE_SPEED = 0.9f;// 移动速度
	private Handler mHandler;// 与Main通信
	private int drawXMax;// 滑动图片能滑动的X坐标的最大值

	public MyRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		getDrawBitmap();// 初始化滑动图片
		setWillNotDraw(false);// 防止onDraw方法不执行
	}

	public MyRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		getDrawBitmap();
		setWillNotDraw(false);
	}

	public MyRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		getDrawBitmap();
		setWillNotDraw(false);
	}

	/**
	 * 得到滑动图片
	 */
	private void getDrawBitmap() {

		if (drawBitmap == null) {
			drawBitmap = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.slidebtn_move);
		}

	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		leftImageView = (ImageView) findViewById(R.id.id_main_img_left);
		rightImageView = (ImageView) findViewById(R.id.id_main_img_right);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("action_down");
			locationX = x;
			return isActionDown(event);

		case MotionEvent.ACTION_MOVE:
			System.out.println("action_move");
			locationX = x;
			invalidate();
			return true;
		case MotionEvent.ACTION_UP:
			System.out.println("action_up");
			if (!isLocked()) {
				imageBack(event);
			}
			return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * 滑动图片回退
	 * 
	 * @param event
	 */
	private void imageBack(MotionEvent event) {
		locationX = (int) (event.getX() - LEFT_DISTANCE);
		if (locationX > 0) {
			mHandler.postDelayed(runnableImgBack, MOVE_TIME);
		}

	}

	/**
	 * 回退线程变量
	 */
	private Runnable runnableImgBack = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			locationX = locationX - (int) (MOVE_SPEED * MOVE_TIME);

			System.out.println("location==" + locationX);
			if (locationX > 0) {
				mHandler.postDelayed(runnableImgBack, MOVE_TIME);
				invalidate();
			}else{
				leftImageView.setVisibility(View.VISIBLE);
				clearVariable();
			}
		}
	};

	/**
	 * 清除变量
	 */
	private void clearVariable(){
		
		temp=0;
		tempCount=0;
		mDrawX=0;
		
	}
	
	/**
	 * 判断是否点击到了滑动图片
	 * 
	 * @param event
	 * @return
	 */
	private boolean isActionDown(MotionEvent event) {

		Rect rect = new Rect();
		leftImageView.getHitRect(rect);
		boolean isDown = rect.contains((int) event.getX(), (int) event.getY());
		if (isDown) {
			leftImageView.setVisibility(View.GONE);
		}
		return isDown;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawImage(canvas);
	}

	int temp = 0;// 全局变量，用来储存触摸点位置与滑动图片宽度的差
	int tempCount = 0;// 全局变量
	int mDrawX = 0;// 全局变量，储存每次绘画的X坐标

	/**
	 * 画图片
	 */
	private void drawImage(Canvas canvas) {
		// 得到最大值
		drawXMax = getScreenWidth() - rightImageView.getWidth();
		// 设置绘制图片的X坐标
		int drawX = locationX - leftImageView.getWidth();
		// 设置绘制图片的Y坐标
		int drawY = leftImageView.getTop();
		// if(drawX<LEFT_DISTANCE){
		// leftImageView.setVisibility(View.VISIBLE);
		// return;
		// }else{
		// if(isLocked()){
		// return;
		// }
		// }
		// 如果点击位置小于滑动图片的X坐标值
		if (locationX < LEFT_DISTANCE) {

			return;
		}
		System.out.println("temp=" + temp);
		// 如果X坐标小于左边框距离
		// 或者等于左边框距离
		// 或者点击位置等于左边框距离
		// 即触摸点此时在（LEFT_DISTANCE+滑动图片的宽度）范围内活动
		if (drawX < LEFT_DISTANCE || drawX == LEFT_DISTANCE
				|| locationX == LEFT_DISTANCE) {
			// 如果X坐标大于上一次的X坐标
			if (drawX > temp) {

				/**
				 * 解释：为什么这里会有tempCount自加？ 如果你点击的范围在滑动图片的显示范围内，
				 * 当你第一次点击，DrawX=20，那么第二次，DrawX=21，，，，
				 * 所以需要自加。而且后面还要这个tempCount变量值。
				 */
				// 变量自加
				tempCount++;
				drawX = LEFT_DISTANCE + tempCount;
			} else {// 否则就说明是第一次点击
				drawX = LEFT_DISTANCE;
			}
		} else {// 这种情况就是触摸点的位置在一个比较特殊的地方（LEFT_DISTANCE+滑动图片的宽度）,接着之前的位置画，不然会出现重画
			drawX = drawX + tempCount;
		}
		// 判断绘制图片的X坐标是否到了最大值
//		if (drawX > drawXMax || drawX == drawXMax) {
//			drawX = drawXMax;
//		}
		// 把绘制图片的X坐标取出来，用来判断是否解锁成功
		mDrawX = drawX;
		leftImageView.setVisibility(View.GONE);
		if (locationX == LEFT_DISTANCE) {
			leftImageView.setVisibility(View.VISIBLE);
		}
		canvas.drawBitmap(drawBitmap, drawX < 0 ? LEFT_DISTANCE : drawX, drawY,
				null);
		// 得到点击位置与绘制图片的差
		temp = locationX - leftImageView.getWidth();
	}

	/**
	 * @return 是否已经成功解锁
	 */
	private boolean isLocked() {

		if (mDrawX >= drawXMax) {
			Message msg = new Message();
			msg.what = AlarmActivity.LOCKED_SUCCESS;
			mHandler.sendMessage(msg);
			return true;
		}
		return false;

	}

	/**
	 * @return 屏幕宽度
	 */
	private int getScreenWidth() {
		int width = ((WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getWidth();
		return width;
	}

	/**
	 * 与MainActivity通信
	 * 
	 * @param handler
	 */
	public void setMainHandler(Handler handler) {
		this.mHandler = handler;
	}

}
