package com.example.umemonew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr
 * 主界面
 */
public class SecondActivity extends Activity implements OnClickListener {
	private MyService TcpService;
	private Protocol protocol = null;

	private SharedPreferences sharedPreferences;  
	private SharedPreferences.Editor editor; 

	private NoteFragment noteFragment;
	private View noteLayout;
	private ImageView noteImage;
	private TimeFragment timeFragment;
	private View timeLayout;
	private ImageView timeImage;
	private PlaceFragment placeFragment;
	private View placeLayout;
	private ImageView placeImage;
	//	private MeetingFragment meetingFragment;
	//	private View meetingLayout;
	//	private ImageView meetingImage;
	private FragmentManager fragmentManager;
	private FragmentManager fragmentManager2;
	private TextView showDate;
	private TextView showWeekday;

	private FriendsFragment friendsFragment;
	private GroupsFragment groupsFragment;

	private TextView friendsText;
	private View friendsLayout;
	private TextView groupsText;
	private View groupsLayout;

	private View settingLayout;
	private View personalInfoLayout;

	private View leftMenu;
	private View showTimeHead;
	private ImageView showHead;
	private TextView showNickname;

	private View myContent;

	private UmemoDatabase db;
	public SecondActivity instance=this;

	/** 
	 * 双向滑动菜单布局 
	 */  
	private BidirSlidingLayout bidirSldingLayout;

	private void connection(){			//connect a service
		Intent intent=new Intent(SecondActivity.this,MyService.class);
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

	public void printDate() {
		Date dt=new Date(System.currentTimeMillis());
		String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0) w = 0;
		SimpleDateFormat formatter=new SimpleDateFormat("MM月dd日");
		showDate.setText(formatter.format(dt));
		showWeekday.setText(weekDays[w]);	
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//透明的悬浮actionBar必须置顶！
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));
		//google的actionbar是分为上下两栏显示的，上面的代码只能设置顶部actionbar的背景色，
		//为了让下面的背景色一致，还需要添加一行代码：
		actionBar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#10000000")));

		setContentView(R.layout.activity_second);

		db = new UmemoDatabase(this, "umemo.db3", 1);		

		showHead = (ImageView) findViewById(R.id.show_head);
		showNickname = (TextView) findViewById(R.id.show_nick_name);
		if(!db.getNowUser().isEmpty())
		{
			String Username = db.getNowUser();
			Map<String , String> userMap = db.getLoginMessage(Username);
			//刷新头像
			//得到application对象
			ApplicationInfo appInfo = getApplicationInfo();
			int resID = getResources().getIdentifier("head"+userMap.get("pictureid"), "raw", appInfo.packageName);
			showHead.setImageResource(resID);
			//刷新昵称
			showNickname.setText(userMap.get("nickname"));
		}
		else
			toastShow("fail");

		initViews();			//initial
		fragmentManager = getFragmentManager();
		fragmentManager2 = getFragmentManager();
		setTabSelection(0);		//set initial fragment
		setTabSelection2(0);
		//setOverflowShowingAlways();


		bidirSldingLayout = (BidirSlidingLayout) findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(noteLayout);
		bidirSldingLayout.setScrollEvent(timeLayout);
		bidirSldingLayout.setScrollEvent(placeLayout);
		//		bidirSldingLayout.setScrollEvent(meetingLayout);
		bidirSldingLayout.setScrollEvent(myContent);

		printDate();

		connection();
	}

	@Override 
	public void onResume()
	{
		super.onResume();
		showHead = (ImageView) findViewById(R.id.show_head);
		showNickname = (TextView) findViewById(R.id.show_nick_name);
		if(!db.getNowUser().isEmpty())
		{
			String Username = db.getNowUser();
			Map<String , String> userMap = db.getLoginMessage(Username);
			//刷新头像
			//得到application对象
			ApplicationInfo appInfo = getApplicationInfo();
			int resID = getResources().getIdentifier("head"+userMap.get("pictureid"), "raw", appInfo.packageName);
			showHead.setImageResource(resID);
			//刷新昵称
			showNickname.setText(userMap.get("nickname"));
		}
		else
			toastShow("fail");

	}

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {  
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();  
	} 

	private void setting(){
		Intent intent = new Intent(SecondActivity.this , SettingActivity.class);
		SecondActivity.this.startActivity(intent);
	}

	private void changeUser() {
		//TODO
		//		sharedPreferences=LoginActivity.instance.getSharedPreferences("umemo",MODE_PRIVATE);
		//		editor=sharedPreferences.edit();
		//		editor.putInt("LOGINABLE",0);
		//		editor.commit();
		//		this.finish();
		toastShow("维护中……");
	}

	private void personalInfo() {
		Intent intent = new Intent(SecondActivity.this , PrivateDataActivity.class);
		SecondActivity.this.startActivity(intent);

	}

	private void initViews() {
		myContent = findViewById(R.id.my_content);
		myContent.setOnClickListener(this);			//the whole activity

		noteLayout = findViewById(R.id.note_layout);
		noteImage = (ImageView) findViewById(R.id.note_image);
		noteLayout.setOnClickListener(this);

		timeLayout = findViewById(R.id.time_layout);
		timeImage = (ImageView) findViewById(R.id.time_image);
		timeLayout.setOnClickListener(this);

		placeLayout = findViewById(R.id.place_layout);
		placeImage = (ImageView) findViewById(R.id.place_image);
		placeLayout.setOnClickListener(this);

		//		meetingLayout = findViewById(R.id.meeting_layout);
		//		meetingImage = (ImageView) findViewById(R.id.meeting_image);
		//		meetingLayout.setOnClickListener(this);

		friendsLayout = findViewById(R.id.friends_layout);
		friendsText = (TextView) findViewById(R.id.friends);
		friendsLayout.setOnClickListener(this);

		groupsLayout = findViewById(R.id.groups_layout);
		groupsText = (TextView) findViewById(R.id.groups);
		groupsLayout.setOnClickListener(this);

		personalInfoLayout = findViewById(R.id.personal_info_layout);
		personalInfoLayout.setOnClickListener(this);
		settingLayout = findViewById(R.id.setting_layout);
		settingLayout.setOnClickListener(this);

		showDate = (TextView) findViewById(R.id.show_date);		//show date and weekday
		showWeekday = (TextView) findViewById(R.id.show_weekday);

		leftMenu = findViewById(R.id.left_menu);
		leftMenu.setPadding(0, getActionbarHeight(this), 0, 0);

		WindowManager wm = this.getWindowManager();		 
		int windowWidth = wm.getDefaultDisplay().getHeight();

		showTimeHead = findViewById(R.id.show_time_head);
		showTimeHead.setPadding(0, getActionbarHeight(this), 0, 0);
		showTimeHead.getLayoutParams().height = windowWidth*3/10;

	}

	public static int getActionbarHeight(Activity context) {
		int actionBarHeight = 0;
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,
				tv, true))
		{
			actionBarHeight =
					TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	public void addFriends(View target) {
		Intent intent = new Intent(SecondActivity.this , AdditionActivity.class);
		SecondActivity.this.startActivity(intent);
	}

	public void addGroups(View target) {
		//TODO
		toastShow(getResources().getString(R.string.add_groups));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.note_layout:
			setTabSelection(0);
			break;
		case R.id.time_layout:
			setTabSelection(1);
			break;
		case R.id.place_layout:
			setTabSelection(2);
			break;
			//		case R.id.meeting_layout:
			//			setTabSelection(3);
			//			break;
		case R.id.friends_layout:
			// 当点击了便签tab时，选中第1个tab
			setTabSelection2(0);
			break;
		case R.id.groups_layout:
			// 当点击了便签tab时，选中第2个tab
			setTabSelection2(1);
			break;
		case R.id.personal_info_layout:
			personalInfo();
			break;
		case R.id.setting_layout:
			setting();
			break;	
		default:
			break;
		}
	}

	private void setTabSelection(int index) {
		clearSelection();				//clear selection
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		hideFragments(transaction);		//hide fragment
		switch (index) {
		case 0:
			noteImage.setImageResource(R.drawable.note_selected);	//change color
			if (noteFragment == null) {
				noteFragment = new NoteFragment();
				transaction.add(R.id.fragment_content, noteFragment);
			} else {
				transaction.show(noteFragment);
			}
			break;
		case 1:
			timeImage.setImageResource(R.drawable.time_selected);
			if (timeFragment == null) {
				timeFragment = new TimeFragment();
				transaction.add(R.id.fragment_content, timeFragment);
			} else {
				transaction.show(timeFragment);
			}
			break;
		case 2:
			placeImage.setImageResource(R.drawable.place_selected);	
			if (placeFragment == null) {
				placeFragment = new PlaceFragment();
				transaction.add(R.id.fragment_content, placeFragment);
			} else {
				transaction.show(placeFragment);
			}
			break;
			//		case 3:
			//			meetingImage.setImageResource(R.drawable.meeting_selected);	
			//			if (meetingFragment == null) {
			//				meetingFragment = new MeetingFragment();
			//				transaction.add(R.id.fragment_content, meetingFragment);
			//			} else {
			//				transaction.show(meetingFragment);
			//			}
			//			break;
		}
		transaction.commit();
	}

	private void setTabSelection2(int index) {
		// 每次选中之前先清楚掉上次的选中状态
		clearSelection2();
		// 开启一个Fragment事务
		FragmentTransaction transaction2 = fragmentManager2.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments2(transaction2);
		switch (index) {
		case 0:
			// 当点击了便签tab时，改变控件的图片和文字颜色
			friendsText.setBackgroundColor(getResources().getColor(R.color.group_selected_color));
			friendsText.setTextColor(getResources().getColor(R.color.group_text_selected_color));
			if (friendsFragment == null) {
				// 如果NoteFragment为空，则创建一个并添加到界面上
				friendsFragment = new FriendsFragment();
				transaction2.add(R.id.menu_content, friendsFragment);
			} else {
				// 如果NOteFragment不为空，则直接将它显示出来
				transaction2.show(friendsFragment);
			}
			break;
		case 1:
			// 当点击了便签tab时，改变控件的图片和文字颜色
			groupsText.setBackgroundColor(getResources().getColor(R.color.group_selected_color));
			groupsText.setTextColor(getResources().getColor(R.color.group_text_selected_color));
			if (groupsFragment == null) {
				// 如果NoteFragment为空，则创建一个并添加到界面上
				groupsFragment = new GroupsFragment();
				transaction2.add(R.id.menu_content, groupsFragment);
			} else {
				// 如果NOteFragment不为空，则直接将它显示出来
				transaction2.show(groupsFragment);
			}
			break;
		}
		transaction2.commit();
	}

	private void clearSelection() {
		noteImage.setImageResource(R.drawable.note_unselected);
		timeImage.setImageResource(R.drawable.time_unselected);
		placeImage.setImageResource(R.drawable.place_unselected);
		//		meetingImage.setImageResource(R.drawable.meeting_unselected);
	}

	private void clearSelection2() {
		friendsText.setBackgroundColor(getResources().getColor(R.color.menu_color));
		groupsText.setBackgroundColor(getResources().getColor(R.color.menu_color));
		friendsText.setTextColor(getResources().getColor(R.color.group_text_color));
		groupsText.setTextColor(getResources().getColor(R.color.group_text_color));
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (noteFragment != null) {
			transaction.hide(noteFragment);
		}
		if (timeFragment != null) {
			transaction.hide(timeFragment);
		}
		if (placeFragment != null) {
			transaction.hide(placeFragment);
		}
		//		if (meetingFragment != null) {
		//			transaction.hide(meetingFragment);
		//		}
	}

	private void hideFragments2(FragmentTransaction transaction2) {
		if (friendsFragment != null) {
			transaction2.hide(friendsFragment);
		}
		if (groupsFragment != null) {
			transaction2.hide(groupsFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second_activity, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { 		// 如果是手机上的返回键
			LoginActivity.instance.finish();
			SecondActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_new_remind) {				
			Intent intent = new Intent(SecondActivity.this, NewRemindActivity.class);
			String method = "create_new";
			intent.putExtra("method",method);
			intent.putExtra("type" , "new");
			this.startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*//overflow强制显示图标
	@Override  
	public boolean onMenuOpened(int featureId, Menu menu) {  
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {  
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {  
				try {  
					Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);  
					m.setAccessible(true);  
					m.invoke(menu, true);  
				} catch (Exception e) {  
				}  
			}  
		}  
		return super.onMenuOpened(featureId, menu);  
	}*/

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
