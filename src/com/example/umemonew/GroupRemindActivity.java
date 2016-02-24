package com.example.umemonew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

//@SuppressLint("NewApi")
public class GroupRemindActivity extends Activity {

	private Protocol protocol = null;
	private MyService TcpService;

	private Button dateButton=null;
	private Button timeButton=null;
	private EditText titleText=null;
	private EditText noteText=null;
	private ImageView myHead = null;
	private Calendar calendar ; 

	private ArrayList<String> friendsArray;
	
	private String groupsName = "";

	private UmemoDatabase db;

	private void findView(){
		titleText = (EditText) findViewById(R.id.titleText_message);
		dateButton = (Button)findViewById(R.id.dateText_message);
		timeButton = (Button)findViewById(R.id.timeText_message);
		noteText= (EditText)findViewById(R.id.noteText_message);
		myHead = (ImageView)findViewById(R.id.headimage_message);
	}

	private void connection(){			//connect a service
		Intent intent=new Intent(GroupRemindActivity.this,MyService.class);
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
			//			TcpService=((MyService.MyBinder)(service)).getService();
			Log.v("onServiceConnected","hello");
			MyBinder binder = (MyBinder)service;
			TcpService = binder.getService();

			System.out.println(TcpService);
		}
	};

	private void setViewListener(){
		myHead.setOnClickListener(handler);
		dateButton.setOnClickListener(handler);
		timeButton.setOnClickListener(handler);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){        
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group);
		protocol=new Protocol();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("群组提醒");

		db = new UmemoDatabase(this, "umemo.db3", 1);

		groupsName = getIntent().getStringExtra("groupsName");
		friendsArray = db.getGroupMembers(groupsName); 

		findView();				//find all the view to use
		setButtonView();		//set date and time
		setViewListener();
		connection();

		//刷新头像
		//得到application对象
		ApplicationInfo appInfo = getApplicationInfo();
		//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
		String picId = db.getGroupPicture(groupsName);
		int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
		myHead.setImageResource(resID);

		//刷新姓名
		TextView name = (TextView) findViewById(R.id.username_message);
		name.setText(groupsName);
	}


	OnClickListener handler=new OnClickListener(){
		@Override
		public void onClick(View v){
			switch(v.getId()){
			case R.id.dateText_message:
				getDateView();			//show date dialog
				break;
			case R.id.timeText_message:
				getTimeView();			//show time dialog
				break;
			default:
				break;
			}
		}
	};

	private void getDateView(){				
		final Calendar c=Calendar.getInstance();		
		new DatePickerDialog (GroupRemindActivity.this,new DatePickerDialog.OnDateSetListener() {
			@Override
			//click the button to set time
			public void onDateSet(DatePicker view, int year, int month,int dayOfMonth) {
				Calendar cc = Calendar.getInstance();
				cc.set(year, month, dayOfMonth);
				SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
				dateButton.setText(dateFormat.format(cc.getTime()));
			}
		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
	}

	private void getTimeView(){
		final Calendar c=Calendar.getInstance();
		new TimePickerDialog(GroupRemindActivity.this,new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Calendar cc = Calendar.getInstance();
				cc.set(c.YEAR, c.MONTH, c.DAY_OF_MONTH,hourOfDay,minute);
				SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-mm-dd HH:mm");
				timeButton.setText(timeFormat.format(cc.getTime()).substring(11,16));
			}
		},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show();
	}

	private void setButtonView(){			//set date and time
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date dt=new Date();
		String format=dateFormat.format(dt);
		String date=format.substring(0,10);
		String time=format.substring(11,16);

		dateButton.setText(date);
		timeButton.setText(time);
	}

	private void sendMessage(String name){
		//you should get the user_name and friend_name from the database
		//And use a for to send messages to all

		String username = db.getNowUser();
		String friendName = name;

		String title = titleText.getText().toString();
		String date = dateButton.getText().toString();
		String time = timeButton.getText().toString();
		String note = noteText.getText().toString();

		String message = protocol.generateMessages(username, friendName, title, date, time, note);
		Log.v("FriendActivity","sending message");


		if(TcpService!=null)
			TcpService.sendMessage(message);
		else Log.v("on click","is null");

		toastShow("您的好友提醒已发送");
	}

	private void toastShow(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.new_remind, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_save) {
			if(titleText.getText().toString().isEmpty())
				toastShow("您的标题为空！");
			else
			{
				for(int i = 0;i < friendsArray.size();i++)
					sendMessage(friendsArray.get(i));    //send a message
				GroupRemindActivity.this.finish();
			}
			return true;
		}
		else if(id == android.R.id.home)
			GroupRemindActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}


}
