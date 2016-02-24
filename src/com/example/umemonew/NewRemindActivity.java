package com.example.umemonew;

import java.util.Calendar;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.umemonew.MyService.MyBinder;


public class NewRemindActivity extends Activity {

	private MyService TcpService;
	
	private Button dateBtn;
	private Button timeBtn;
	private Switch timeOpen;
	private Switch placeOpen;
	private View placeTextLayout;
	private TextView placeShow;
	private TextView placeText;
	//	private Switch meetingOpen;
	//	private View meetingTextLayout;
	//	private TextView meetingShow;
	//	private TextView meetingText;
	private Calendar calendar;
	private EditText title;
	private EditText note;

	//	private Dialog noTitleAlertDialog;
	//	private Dialog repeatTitleAlertDialog;
	//	private Dialog repeatTimeAlertDialog;

	private String method;
	private String type;
	private int id;
	private static final String EDIT = "edit_old";
	private static final String CREATE = "create_new";

	private ColorStateList onColor;
	private ColorStateList offColor;

	private UmemoDatabase db=null;

	private String date , time ; 
	private int Year = 0 , Month = 0 , Day = 0 , Hour = 0 , Minute  = 0 ;

	private String Longitude = ""; //����
	private String Latitude = ""; //γ��
	
	private void connection(){			//connect a service
		Intent intent=new Intent(NewRemindActivity.this,MyService.class);
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
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_remind);
		
		connection();
		
		db=new UmemoDatabase(this, "umemo.db3", 1);

		method = getIntent().getStringExtra("method");
		type = getIntent().getStringExtra("type");
		id = getIntent().getIntExtra("id", 0);

		actionBarInitial();
		timeInitial();
		viewAndColorInitial();
		textAndEnabledInitial();

		dateBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Calendar c=Calendar.getInstance();
				//ֱ�Ӵ���һ��DatePickerDialog�Ի���ʵ������������ʾ����
				new DatePickerDialog (NewRemindActivity.this,
						//�󶨼�����
						new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int month,
							int dayOfMonth) {
						Year = year ; Month = month ; Day = dayOfMonth ; 
						date = Integer.toString(year);
						date += "-" ; 
						if(month<9) date+="0";
						date += Integer.toString(month+1);  // month��Χ�ǣ�0 ~ 11������ + 1 
						date += "-" ; 
						if(dayOfMonth<10) date+="0";
						date += Integer.toString(dayOfMonth);
						dateBtn.setText(date);
					}
				},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
			}

		});

		timeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Calendar c=Calendar.getInstance();
				//����һ��TimePickerDialogʵ������������ʾ����
				new TimePickerDialog(NewRemindActivity.this,
						//�󶨼�����
						new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						Hour = hourOfDay ; Minute = minute ; 
						time = Integer.toString(hourOfDay);
						if(hourOfDay<10) time="0"+time;
						time += ":" ; 
						if(minute<10) time+="0";
						time += Integer.toString(minute);
						timeBtn.setText(time);
					}
				}
				//���ó�ʼʱ��
				,c.get(Calendar.HOUR_OF_DAY)
				,c.get(Calendar.MINUTE)
				//true��ʾ����24Сʱ��
				,true).show();
			}
		});

		timeOpen.setOnCheckedChangeListener(new OnCheckedChangeListener() {			   
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					dateBtn.setEnabled(true);
					timeBtn.setEnabled(true);
					dateBtn.setTextColor(onColor);
					timeBtn.setTextColor(onColor);
					toastShow("����ʱ������");
				}
				else
				{
					dateBtn.setEnabled(false);
					timeBtn.setEnabled(false);
					dateBtn.setTextColor(offColor);
					timeBtn.setTextColor(offColor);
					toastShow("�ر�ʱ������");
					timeReInitial();
					dateBtn.setText(date);
					timeBtn.setText(time);
				}	
			}
		});

		placeTextLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(NewRemindActivity.this, MapActivity.class);
				NewRemindActivity.this.startActivityForResult(intent, 2);
			}
		});

		placeOpen.setOnCheckedChangeListener(new OnCheckedChangeListener() {			   
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					placeTextLayout.setEnabled(true);
					placeShow.setTextColor(onColor);
					placeText.setTextColor(onColor);
					toastShow("�����ص�����");
				}

				else
				{
					placeTextLayout.setEnabled(false);
					placeShow.setTextColor(offColor);
					placeText.setTextColor(offColor);
					toastShow("�رյص�����");
				}	
			}
		});

		//		meetingTextLayout.setOnClickListener(new OnClickListener(){
		//			@Override
		//			public void onClick(View arg0) {
		//				//TODO
		//				meetingText.setText("����");
		//				toastShow("�������������ſ�");
		//			}
		//		});
		//
		//		meetingOpen.setOnCheckedChangeListener(new OnCheckedChangeListener() {			   
		//			@Override
		//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//				if(isChecked)
		//				{
		//					meetingTextLayout.setEnabled(true);
		//					meetingShow.setTextColor(onColor);
		//					toastShow("������������");
		//				}
		//
		//				else
		//				{
		//					meetingTextLayout.setEnabled(false);
		//					meetingShow.setTextColor(offColor);
		//					toastShow("�ر���������");
		//				}	
		//			}
		//		});

		//		noTitleAlertDialog = new AlertDialog.Builder(this).
		//				setTitle("����").
		//				setMessage("��������û�б��⣡").
		//				setNeutralButton("ȷ��", new DialogInterface.OnClickListener() { 
		//					@Override 
		//					public void onClick(DialogInterface dialog, int which) {
		//					} 
		//				}). 
		//				create();
		//
		//		repeatTitleAlertDialog = new AlertDialog.Builder(this).
		//				setTitle("����").
		//				setMessage("���ı����ѱ�ʹ�ã�").
		//				setNeutralButton("ȷ��", new DialogInterface.OnClickListener() { 
		//					@Override 
		//					public void onClick(DialogInterface dialog, int which) {
		//					} 
		//				}). 
		//				create();
		//
		//		repeatTimeAlertDialog = new AlertDialog.Builder(this).
		//				setTitle("����").
		//				setMessage("��ʱ���ѱ���ʹ�ã�").
		//				setNeutralButton("ȷ��", new DialogInterface.OnClickListener() { 
		//					@Override 
		//					public void onClick(DialogInterface dialog, int which) {
		//					} 
		//				}). 
		//				create();
	}

	private void repeatTimeAlert()
	{
		toastShow("���ڸ�ʱ�����б�����ѣ�");
	}
	
	private void noTitleAlert()
	{
		toastShow("���ı���Ϊ�գ�");
	}

	private void repeatTitleAlert()
	{
		toastShow("���ı����ѱ�ʹ�ã�");
	}

	private void noPlaceAlert()
	{
		toastShow("����ûѡ��һ���ص㣡");
	}

	private void actionBarInitial()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if(method.equals(EDIT))
			actionBar.setTitle("�༭����");
		else if(method.equals(CREATE))
			actionBar.setTitle("�½�����");
		else
			toastShow("method ����");
	}

	private void timeReInitial()
	{
		calendar = Calendar.getInstance();
		Year = calendar.get(Calendar.YEAR) ; 
		Month = calendar.get(Calendar.MONTH) ; 
		Day = calendar.get(Calendar.DAY_OF_MONTH) ; 
		Hour = calendar.get(Calendar.HOUR_OF_DAY) ; 
		Minute = calendar.get(Calendar.MINUTE) ;

		// ������ת��Ϊstring����
		date = Integer.toString(Year);
		date += "-" ; 
		if(Month<9) date+="0";
		date += Integer.toString(Month+1);  // 0 ~ 11
		date += "-" ;
		if(Day<10) date+="0";
		date += Integer.toString(Day);

		if(Hour<10) time+="0";
		time = Integer.toString(Hour);
		time += ":" ;

		if(Minute<10) time+="0";
		time += Integer.toString(Minute);
	}


	private void timeInitial()
	{
		calendar = Calendar.getInstance();
		Year = calendar.get(Calendar.YEAR) ; 
		Month = calendar.get(Calendar.MONTH) ; 
		Day = calendar.get(Calendar.DAY_OF_MONTH) ; 
		Hour = calendar.get(Calendar.HOUR_OF_DAY) ; 
		Minute = calendar.get(Calendar.MINUTE) ;

		// ������ת��Ϊstring����
		date = Integer.toString(Year);
		date += "-" ; 
		if(Month<9) date+="0";
		date += Integer.toString(Month+1);  // 0 ~ 11
		date += "-" ;
		if(Day<10) date+="0";
		date += Integer.toString(Day);

		if(Hour<10) time+="0";
		time = Integer.toString(Hour);
		time += ":" ;

		if(Minute<10) time+="0";
		time += Integer.toString(Minute);

		if(type.equals("time"))
		{
			Map<String , String> list = db.getTimeMessageById(id);
			date = list.get("date");
			time = list.get("time");
			int dividePos1 , dividePos2 , dividePos3;
			dividePos1 = list.get("date").indexOf('-', 0);
			dividePos2 = list.get("date").indexOf('-', dividePos1+1);
			dividePos3 = list.get("time").indexOf(':', 0);
			Year = Month = Day = Hour = Minute = 0;
			for(int i = 0;i < dividePos1;i++)
				Year = Year*10+list.get("date").charAt(i)-'0';
			for(int i = dividePos1+1;i < dividePos2;i++)
				Month = Month*10+list.get("date").charAt(i)-'0';
			Month -= 1;
			for(int i = dividePos2+1;i < list.get("date").length();i++)
				Day = Day*10+list.get("date").charAt(i)-'0';
			for(int i = 0;i < dividePos3;i++)
				Hour = Hour*10+list.get("time").charAt(i)-'0';
			for(int i = dividePos3+1;i < list.get("time").length();i++)
				Minute = Minute*10+list.get("time").charAt(i)-'0';
		}
		else if(type.equals("place"))
		{
			Map<String , String> list = db.getPlaceMessageById(id);
			if(!(list.get("begindate").isEmpty() || list.get("begintime").isEmpty()))
			{
				date = list.get("begindate");
				time = list.get("begintime");
				int dividePos1 , dividePos2 , dividePos3;
				dividePos1 = list.get("begindate").indexOf('-', 0);
				dividePos2 = list.get("begindate").indexOf('-', dividePos1+1);
				dividePos3 = list.get("begintime").indexOf(':', 0);
				Year = Month = Day = Hour = Minute = 0;
				for(int i = 0;i < dividePos1;i++)
					Year = Year*10+list.get("begindate").charAt(i)-'0';
				for(int i = dividePos1+1;i < dividePos2;i++)
					Month = Month*10+list.get("begindate").charAt(i)-'0';
				Month -= 1;
				for(int i = dividePos2+1;i < list.get("begindate").length();i++)
					Day = Day*10+list.get("begindate").charAt(i)-'0';
				for(int i = 0;i < dividePos3;i++)
					Hour = Hour*10+list.get("begintime").charAt(i)-'0';
				for(int i = dividePos3+1;i < list.get("begintime").length();i++)
					Minute = Minute*10+list.get("begintime").charAt(i)-'0';
			}
		}
	}

	private void viewAndColorInitial()
	{
		dateBtn = (Button) findViewById(R.id.dateButton);
		timeBtn = (Button) findViewById(R.id.timeButton);
		timeOpen = (Switch) findViewById(R.id.timeSwitch);


		placeOpen = (Switch) findViewById(R.id.placeSwitch);
		placeShow = (TextView) findViewById(R.id.placeShow);
		placeTextLayout = findViewById(R.id.placeTextLayout);
		placeText = (TextView) findViewById(R.id.placeText);

		//		meetingOpen = (Switch) findViewById(R.id.meetingSwitch);
		//		meetingShow = (TextView) findViewById(R.id.meetingShow);
		//		meetingTextLayout = findViewById(R.id.meetingTextLayout);
		//		meetingText = (TextView) findViewById(R.id.meetingText);

		title = (EditText) findViewById(R.id.titleText);
		note = (EditText) findViewById(R.id.noteText);

		onColor=this.getResources().getColorStateList(R.color.remind_on_color);
		offColor=this.getResources().getColorStateList(R.color.remind_off_color);
	}

	private void textAndEnabledInitial()
	{
		dateBtn.setText(date);
		timeBtn.setText(time);
		dateBtn.setEnabled(false);
		timeBtn.setEnabled(false);
		placeTextLayout.setEnabled(false);
		//		meetingTextLayout.setEnabled(false);
		if(method.equals(EDIT))
		{
			Map<String, String> list;
			if(type.equals("note"))
			{
				list = db.getNoteById(id);
				title.setText(list.get("title"));
				note.setText(list.get("note"));
			}
			else if(type.equals("time"))
			{
				list = db.getTimeMessageById(id);
				title.setText(list.get("title"));
				note.setText(list.get("note"));
				dateBtn.setText(list.get("date"));
				dateBtn.setTextColor(onColor);
				timeBtn.setText(list.get("time"));
				timeBtn.setTextColor(onColor);
				timeOpen.setChecked(true);
				dateBtn.setEnabled(true);
				timeBtn.setEnabled(true);
			}
			else if(type.equals("place"))
			{
				list = db.getPlaceMessageById(id);
				title.setText(list.get("title"));
				note.setText(list.get("note"));
				placeText.setText(list.get("place"));
				placeOpen.setChecked(true);
				placeTextLayout.setEnabled(true);
				placeText.setTextColor(onColor);
				placeShow.setTextColor(onColor);
				if(!(list.get("begindate").isEmpty() || list.get("begintime").isEmpty()))
				{
					dateBtn.setText(list.get("begindate"));
					dateBtn.setTextColor(onColor);
					timeBtn.setText(list.get("begintime"));
					timeBtn.setTextColor(onColor);
					timeOpen.setChecked(true);
					dateBtn.setEnabled(true);
					timeBtn.setEnabled(true);
				}
				//ˢ������
				String coordinate = db.getPlaceCoordinate(id);
				int divider = coordinate.indexOf('&', 0);
				Longitude = coordinate.substring(0, divider); 
				Latitude = coordinate.substring(divider+1 , coordinate.length());
			}
			else if(type.equals("meeting"));//TODO
		}
	}

	public void saveAsNote()
	{
		if(title.getText().toString().isEmpty()) //�����ֹΪ��
			noTitleAlert();
		//			noTitleAlertDialog.show();
		else if(method.equals(EDIT)) //����Ƿ�༭
		{
			//����Ƿ���Ҫת������
			if(type.equals("note"))// ͬ����ֱ�ӱ���
			{
				db.changeNoteMessage(id, title.getText().toString(), note.getText().toString());
				toastShow("����ɹ���");
				NewRemindActivity.this.finish();
			}
			else
			{
				if(db.getNoteId(title.getText().toString()) != -1)// ��ͬ���ͷ�ֹ������
					repeatTitleAlert();
				//					repeatTitleAlertDialog.show();
				else
				{
					if(type.equals("time"))// ɾ����ǰ������
					{
						db.insertNote(title.getText().toString(), note.getText().toString());
						cancelTimeBroadcast(id);
						db.deleteTimeHint(id);
					}
					else if(type.equals("place"))
					{
						db.insertNote(title.getText().toString(), note.getText().toString());
						cancelPlaceBroadcast(id);
						db.deletePlaceHint(id);
					}
					else if(type.equals("meeting")); //TODO
					else
						toastShow("type ����");
					toastShow("����ɹ���");
					NewRemindActivity.this.finish();
				}
			}

		}
		else if(db.getNoteId(title.getText().toString()) != -1) //����Ƿ�����ظ�
			repeatTitleAlert();
		//			repeatTitleAlertDialog.show();
		else if(method.equals(CREATE)) //���������ݿ�
		{
			db.insertNote(title.getText().toString(), note.getText().toString());
			toastShow("����ɹ���");
			NewRemindActivity.this.finish();
		}
		else
			toastShow("method ����");
	}

	public void saveAsTimeAlarm()
	{
		if(title.getText().toString().isEmpty()) // �������Ƿ�Ϊ��
			noTitleAlert();
		//			noTitleAlertDialog.show();
		else if(method.equals(EDIT))
		{
			//����Ƿ���Ҫת������
			if(type.equals("time"))// ͬ����ֱ�ӱ���
			{
				db.changeTimeMessage(id, title.getText().toString(), date, time, note.getText().toString());

				Calendar calendar=Calendar.getInstance();
				calendar.set(Year, Month, Day, Hour, Minute, 0);
				
				openTimeBroadcast(id , calendar);

				toastShow("����ɹ���");
				NewRemindActivity.this.finish();
			}
			else if(db.getTimeNoFinishId(date, time) != -1)// ���ʱ���Ƿ��ظ�
				repeatTimeAlert();
			//			repeatTimeAlertDialog.show();
			else
			{
				if(db.getTimeNoFinishId(title.getText().toString()) != -1)// ��ͬ���ͷ�ֹ������
					repeatTitleAlert();
				//					repeatTitleAlertDialog.show();
				else
				{
					if(type.equals("note"))
						db.deleteNote(id);
					else if(type.equals("place"))
					{
						cancelPlaceBroadcast(id);
						db.deletePlaceHint(id);
					}
					else if(type.equals("meeting")); //TODO
					else
						toastShow("type ����");

					Calendar calendar=Calendar.getInstance();
					calendar.set(Year, Month, Day, Hour, Minute, 0);
					db.insertTimelist(title.getText().toString(), date, time, note.getText().toString());

					openTimeBroadcast(db.getTimeId(title.getText().toString(), date, time) , calendar);

					toastShow("����ɹ���");
					NewRemindActivity.this.finish();
				}
			}
		}
		else if(db.getTimeNoFinishId(title.getText().toString()) != -1)// �������Ƿ��ظ�
			repeatTitleAlert();
		//			repeatTitleAlertDialog.show();
		else if(db.getTimeNoFinishId(date, time) != -1)// ���ʱ���Ƿ��ظ�
			repeatTimeAlert();
		//			repeatTimeAlertDialog.show();
		else if(method.equals(CREATE)) // ���浽���ݿ�
		{
			Calendar calendar=Calendar.getInstance();
			calendar.set(Year, Month, Day, Hour, Minute, 0);
			db.insertTimelist(title.getText().toString(), date, time, note.getText().toString());

			openTimeBroadcast(db.getTimeId(title.getText().toString(), date, time) , calendar);

			toastShow("����ɹ���");
			NewRemindActivity.this.finish();
		}
	}

	public void saveAsPlaceAlarm()
	{
		if(title.getText().toString().isEmpty())
			noTitleAlert();
		//			noTitleAlertDialog.show();
		else if(Longitude.isEmpty() || Latitude.isEmpty())
			noPlaceAlert();
		else if(method.equals(EDIT))
		{
			//����Ƿ���Ҫת������
			if(type.equals("place"))// ͬ����ֱ�ӱ���
			{
				if(timeOpen.isChecked())
					db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), date, time);
				else
					db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), "", "");

				cancelPlaceBroadcast(id);

				Calendar calendar=Calendar.getInstance();
				calendar.set(Year, Month, Day, Hour, Minute, 0);

				openPlaceBroadcast(id , calendar);

				toastShow("����ɹ���");
				NewRemindActivity.this.finish();
			}
			else 
			{
				if(db.getPlaceIdNotFinished(title.getText().toString()) != -1)// ��ͬ���ͷ�ֹ������
					repeatTitleAlert();
				//					repeatTitleAlertDialog.show();
				else
				{
					if(type.equals("note"))
						db.deleteNote(id);
					else if(type.equals("time"))
					{
						cancelTimeBroadcast(id);
						db.deleteTimeHint(id);
					}
					else if(type.equals("meeting")); //TODO
					else
						toastShow("type ����");

					Calendar calendar=Calendar.getInstance();
					calendar.set(Year, Month, Day, Hour, Minute, 0);
					db.insertPlacelist(title.getText().toString(), placeText.getText().toString(), note.getText().toString());
					int id = db.getPlaceIdNotFinished(title.getText().toString());
					if(timeOpen.isChecked())
						db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), date, time);
					else
						db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), "", "");
					openPlaceBroadcast(id, calendar);
					db.changePlaceCoordinate(id, Longitude+"&"+Latitude);

					toastShow("����ɹ���");
					NewRemindActivity.this.finish();
				}
			}
		}
		else if(db.getPlaceIdNotFinished(title.getText().toString()) != -1)// �������Ƿ��ظ�
			repeatTitleAlert();
		else if(method.equals(CREATE))
		{
			Calendar calendar=Calendar.getInstance();
			calendar.set(Year, Month, Day, Hour, Minute, 0);
			db.insertPlacelist(title.getText().toString(), placeText.getText().toString(), note.getText().toString());
			int id = db.getPlaceIdNotFinished(title.getText().toString());
			if(timeOpen.isChecked())
				db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), date, time);
			else
				db.changePlaceMessage(id, title.getText().toString(), placeText.getText().toString(), note.getText().toString(), "", "");
			openPlaceBroadcast(id, calendar);
			db.changePlaceCoordinate(id, Longitude+"&"+Latitude);
			toastShow("����ɹ���");
			NewRemindActivity.this.finish();
		}
	}

	public void saveAsMeetingAlarm()
	{
		if(title.getText().toString().isEmpty())
			noTitleAlert();
		//			noTitleAlertDialog.show();
		else
			toastShow("��δ��ɣ�");
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
			//			if(meetingOpen.isChecked())
			//				saveAsMeetingAlarm();
			if(placeOpen.isChecked())
				saveAsPlaceAlarm();
			else if(timeOpen.isChecked())
				saveAsTimeAlarm();
			else
				saveAsNote();
			return true;
		}
		else if(id == android.R.id.home)
			NewRemindActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	public void toastShow(String text) {  
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();  
	}

	private void openTimeBroadcast(int id , Calendar calendar)
	{
		Intent intent=new Intent(NewRemindActivity.this,AlarmReceiver.class);
		intent.putExtra("id", id);				//put id (to distinguish broadcast)
		PendingIntent pendingIntent=PendingIntent.getBroadcast(NewRemindActivity.this,id,intent,
				PendingIntent.FLAG_UPDATE_CURRENT);  
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE); 	
		am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
	}

	private void cancelTimeBroadcast(int id)
	{
		Intent intent=new Intent(NewRemindActivity.this,AlarmReceiver.class);
		intent.putExtra("id",id);				//put id (to distinguish broadcast)
		PendingIntent pendingIntent=PendingIntent.getBroadcast(NewRemindActivity.this,id,intent,
				PendingIntent.FLAG_UPDATE_CURRENT);  
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE); 	
		am.cancel(pendingIntent);
	}

	private void openPlaceBroadcast(int id , Calendar calendar)
	{
		Intent intent = new Intent(NewRemindActivity.this , PlaceReceiver.class);
		intent.setAction("com.example.umemonew.TIME_ALARM");
		intent.putExtra("Longitude" , Longitude) ;
		intent.putExtra("Latitude" , Latitude) ;
		intent.putExtra("id", id);
		PendingIntent operation = PendingIntent
				.getBroadcast(NewRemindActivity.this, -id, intent , PendingIntent.FLAG_UPDATE_CURRENT);// ����һ���㲥��PendingIntentΪIntent�İ�װ
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP , calendar.getTimeInMillis() , operation);
	}

	private void cancelPlaceBroadcast(int id)
	{
		Intent intentService = new Intent(NewRemindActivity.this , PlaceService.class);
		Intent intentBroadCast = new Intent(NewRemindActivity.this , PlaceReceiver.class);
		//ΪIntent����Action����		
		intentService.setAction("com.example.umemonew.FIRST_SERVICE");
		intentBroadCast.setAction("com.example.umemonew.TIME_ALARM");

		PendingIntent operation = PendingIntent
				.getBroadcast(NewRemindActivity.this, -id, intentBroadCast , PendingIntent.FLAG_UPDATE_CURRENT);// ����һ���㲥��PendingIntentΪIntent�İ�װ		 				 			
		stopService(intentService) ;
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(operation);
	}

	//ˢ�µص���Ϣ
	/**
	 * ���е�Activity����ķ���ֵ�������������������
	 * requestCode:    ��ʾ��������һ��Activityʱ����ȥ��requestCodeֵ
	 * resultCode����ʾ�����������Activity�ش�ֵʱ��resultCodeֵ
	 * data����ʾ�����������Activity�ش�������Intent����
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 2 && resultCode == 3)
		{
			String placeTitle = data.getStringExtra("placeTitle");
			Longitude = data.getStringExtra("Longitude");
			Latitude = data.getStringExtra("Latitude");
			placeText.setText(placeTitle);
		}
	}

	private void check()
	{
		toastShow(Integer.toString(Year));
		toastShow(Integer.toString(Month));
		toastShow(Integer.toString(Day));
		toastShow(Integer.toString(Hour));
		toastShow(Integer.toString(Minute));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
