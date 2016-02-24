package com.example.umemonew;

import java.util.Calendar;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr , 林坤煜
 * 消息确认界面
 */
public class MessageNotificationActivity extends Activity{

	private MyService TcpService;
	private Protocol protocol=null;

	private Button verifyButton = null;
	private Button refuseButton = null;
	private TextView titleText = null;
	private TextView usernameText = null;
	private Button dateText = null;
	private Button timeText = null;
	private TextView noteText = null;
	private ImageView headView = null;

	private Bundle bundle = null;

	private UmemoDatabase db=null;

	private String username = "";
	private String head = "";
	private String title = "";
	private String date = "";
	private String time = "";
	private String note = "";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_notification);

		db=new UmemoDatabase(this, "umemo.db3", 1);

		connection();				//every activity should add 
		findView();

		bundle=getIntent().getExtras();
		protocol=new Protocol();

		String message=bundle.getString("message");

		setView(message);
	}

	private void setView(String message)
	{
		protocol.handleMessageDetail(message);
		Log.v("create","hello2");

		//get all kinds of messages
		username = protocol.username_of_message;
		head = protocol.head_number;
		title = protocol.title;
		date = protocol.date;
		time = protocol.time;
		note = protocol.note;



		Map<String , String> friendsMap = db.getFriendMessage(username);
		//刷新头像
		//得到application对象
		ApplicationInfo appInfo = getApplicationInfo();
		//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
		String picId = friendsMap.get("pictureid");
		int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
		headView.setImageResource(resID);
		//刷新昵称
		usernameText.setText(friendsMap.get("nickname"));
		
		
		titleText.setText(title);
		dateText.setText(date);
		timeText.setText(time);
		noteText.setText(note);
		Log.v("create","hello3");

		verifyButton.setOnClickListener(handler);
		refuseButton.setOnClickListener(handler);
	}

	private void findView()
	{
		usernameText = (TextView)findViewById(R.id.username_message);
		verifyButton = (Button)findViewById(R.id.receiveButton);
		refuseButton = (Button)findViewById(R.id.cancelButton);
		titleText = (TextView)findViewById(R.id.titleText_message);
		dateText = (Button)findViewById(R.id.dateText_message);
		timeText = (Button)findViewById(R.id.timeText_message);
		noteText = (TextView)findViewById(R.id.noteText_message);
		headView = (ImageView)findViewById(R.id.headimage_message);
	}

	OnClickListener handler =new OnClickListener(){
		@Override
		public void onClick(View v){
			switch(v.getId()){
			case R.id.receiveButton:
				verify();
				break;
			case R.id.cancelButton:
				refuse();
				break;
			default:
				break;
			}
		}
	};

	private void verify()
	{
		if(db.getTimeNoFinishId(title) != -1)
			toastShow("该提醒标题与本地提醒标题冲突，已作废！");
		else if(db.getTimeNoFinishId(date, time) != -1)// 检测时间是否重复
			toastShow("该提醒时间与本地提醒时间冲突，已作废！");
		else
		{
			int Year , Month , Day , Hour , Minute;
			Year = Month = Day = Hour = Minute = 0;
			
			int dividePos1 , dividePos2 , dividePos3;
			dividePos1 = date.indexOf('-', 0);
			dividePos2 = date.indexOf('-', dividePos1+1);
			dividePos3 = time.indexOf(':', 0);
			Year = Month = Day = Hour = Minute = 0;
			for(int i = 0;i < dividePos1;i++)
				Year = Year*10+date.charAt(i)-'0';			
			for(int i = dividePos1+1;i < dividePos2;i++)
				Month = Month*10+date.charAt(i)-'0';
			Month -= 1;
			for(int i = dividePos2+1;i < date.length();i++)
				Day = Day*10+date.charAt(i)-'0';
			for(int i = 0;i < dividePos3;i++)
				Hour = Hour*10+time.charAt(i)-'0';
			for(int i = dividePos3+1;i < time.length();i++)
				Minute = Minute*10+time.charAt(i)-'0';
			
			Calendar calendar=Calendar.getInstance();
			calendar.set(Year, Month, Day, Hour, Minute, 0);
			db.insertTimelist(title, date, time, noteText.getText().toString());
			int id = db.getTimeId(title, date, time);
			Intent intent=new Intent(MessageNotificationActivity.this,AlarmReceiver.class);
			intent.putExtra("id", id);				//put id (to distinguish broadcast)
			PendingIntent pendingIntent=PendingIntent.getBroadcast(MessageNotificationActivity.this,id,intent,
					PendingIntent.FLAG_UPDATE_CURRENT);  
			AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE); 	
			am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

			toastShow("保存成功！");
		}
		MessageNotificationActivity.this.finish();
	}

	private void refuse()
	{
		MessageNotificationActivity.this.finish();
	}	

	//this two part should be added into every activity
	private void connection(){			//connect a service
		Intent intent=new Intent(this,MyService.class);
		bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
		Log.v("connection","connected");
	}

	private ServiceConnection serviceConnection=new ServiceConnection(){
		@Override
		public void onServiceDisconnected(ComponentName name){
			Log.v("onServiceDisconnected","hello");
			TcpService=null;
		}
		@Override
		public void onServiceConnected(ComponentName name,IBinder service){
			Log.v("onServiceConnected","hello");
			MyBinder binder = (MyBinder)service;
			TcpService = binder.getService();
			System.out.println(TcpService);
		}
	};

	private void toastShow(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
