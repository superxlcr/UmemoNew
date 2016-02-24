package com.example.umemonew;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author 林炜润
 * 闹钟提醒界面
 */
public class AlarmActivity extends Activity {
	private MyService TcpService;
	private Protocol protocol = null;

	// 声明MediaPlayer对象
	private MediaPlayer alarmMusic = null;
	private UmemoDatabase db = null;
	public int id;
	private int volume;
	private Handler handler2 = null;

	private TextView textView;
	private TextView nodeView;

	public static final int LOCKED_SUCCESS = 1;
	public static final int VIEW_INVALIDATE = 2;
	private MyRelativeLayout myRelativeLayout;
	private AudioManager audioManager = null;

	private String type;

	private SharedPreferences pref;
	private Vibrator vibrator;
	private NotificationManager notificationManager;
	final int LED_ID = 12345678;

	private boolean vibrationCheck;
	private boolean ledCheck;
	private boolean soundCheck;
	private int soundLevel;
	private int soundType;

	private void connection() { // connect a service
		Intent intent = new Intent(AlarmActivity.this, MyService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		Log.v("connection", "connected");
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.v("onServiceDisconnected", "hello");
			TcpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v("onServiceConnected", "hello");
			MyBinder binder = (MyBinder) service;
			TcpService = binder.getService();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connection();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		// 全屏显示
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_alarm);

		initViews();

		db = new UmemoDatabase(this, "umemo.db3", 1);
		id = getIntent().getIntExtra("id", 0);
		type = getIntent().getStringExtra("type");
		String totalTime = "";
		String things = "";
		String note = "";
		if (type.equals("time")) {
			Map<String, String> new_map = db.getTimeMessageById(id);
			totalTime = new_map.get("date") + " " + new_map.get("time");
			things = new_map.get("title");
			note = new_map.get("note");
		} else if (type.equals("place")) {
			Map<String, String> new_map = db.getPlaceMessageById(id);
			totalTime = new_map.get("date") + " " + new_map.get("time");
			things = new_map.get("title");
			note = new_map.get("note");
		}

		textView.setText(things);
		nodeView.setText(note);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);// 获取电源管理器对象
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		// 获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是LogCat里用的Tag
		wl.acquire();// 点亮屏幕

		getSharedData();
		startHint();
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (volume < 9) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						volume++, 0);
				handler2.postDelayed(this, 150);
			} else {
				handler2.removeCallbacks(this);
				return;
			}
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == LOCKED_SUCCESS) {
				type = getIntent().getStringExtra("type");
				if (type.equals("time"))
					db.changeTimeState(id, "1");
				else if (type.equals("place")) {
					Intent intent = new Intent();
					// 为Intent设置Action属性
					intent.setAction("com.example.umemonew.FIRST_SERVICE");
					stopService(intent);
					db.changePlaceState(id, "1");
				}
				stopHint();
				AlarmActivity.this.finish();
			} else if (msg.what == VIEW_INVALIDATE) {
				myRelativeLayout.invalidate();
			}
		};
	};

	/**
	 * 初始化视图，并添加相应事件
	 */
	private void initViews() {
		textView = (TextView) findViewById(R.id.id_main_txt_test);
		nodeView = (TextView) findViewById(R.id.id_node_text);
		myRelativeLayout = (MyRelativeLayout) findViewById(R.id.id_main_mrlt_relativelayout);
		myRelativeLayout.setMainHandler(handler);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
							| AudioManager.FLAG_SHOW_UI);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
		if (alarmMusic != null) {
			alarmMusic.release();
		}
	}

	private void getSharedData() {
		pref = getSharedPreferences("setting_data", MODE_PRIVATE);
		vibrationCheck = pref.getBoolean("is_vibration", true);
		ledCheck = pref.getBoolean("is_led_on", true);
		soundCheck = pref.getBoolean("is_sound_on", true);
		soundLevel = pref.getInt("sound_level", 1);
		soundType = pref.getInt("sound_type", 0);
	}

	private void startHint() {
		if (vibrationCheck) {
			vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			long[] vibrates = { 1000, 1000 };
			vibrator.vibrate(vibrates, 0);
		}
		if (ledCheck) {
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification();
			notification.defaults = Notification.DEFAULT_LIGHTS;
			notification.flags = Notification.FLAG_SHOW_LIGHTS;
			notificationManager.notify(LED_ID, notification);
		}
		if (soundCheck) {
			audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

			alarmMusic = MediaPlayer.create(this, getMusicIds().get(soundType));
			alarmMusic.setVolume(0f, (float) soundLevel / 10);

			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

			alarmMusic.setLooping(true);
			alarmMusic.start();

			handler2 = new Handler();
			handler2.postDelayed(runnable, 0);
		}
	}

	private void stopHint() {
		if (vibrationCheck) {
			vibrator.cancel();
		}
		if (ledCheck) {
			notificationManager.cancel(LED_ID);
		}
		if (soundCheck) {
			alarmMusic.stop();
		}
	}

	public static List<Integer> getMusicIds() {
		try {
			Field[] musicFields = R.raw.class.getFields();
			List<Integer> musicIds = new ArrayList<Integer>();
			for (Field field : musicFields) {
				if (field.getName().indexOf("m_") == 0)
					musicIds.add(field.getInt(R.raw.class));
			}
			return musicIds;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

}
