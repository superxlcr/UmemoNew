package com.example.umemonew;

import java.util.ArrayList;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr
 * 群组成员管理界面
 */
public class ManageGroupsActivity extends Activity {

	private MyService TcpService;
	private Protocol protocol = null;

	private LayoutInflater inflater;
	private int totalFriendsNumber = 0;
	private String groupsName = "";
	private ArrayList<String> groupsMember;

	private Button newGroupsTask;
	private Button deleteGroups;

	private Dialog deleteAlertDialog;

	private UmemoDatabase db=null;

	public static ManageGroupsActivity instance = null;

	private void connection(){			//connect a service
		Intent intent=new Intent(ManageGroupsActivity.this,MyService.class);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_groups);
		ActionBar actionBar = getActionBar();  
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("群组管理");
		inflater = getLayoutInflater();

		instance = this;

		//初始化数据库
		db = new UmemoDatabase(this, "umemo.db3", 1);

		groupsName = getIntent().getStringExtra("groupsName");
		groupsMember = db.getGroupMembers(groupsName);
		totalFriendsNumber = groupsMember.size();

		newGroupsTask = (Button) findViewById(R.id.new_groups_task);
		deleteGroups = (Button) findViewById(R.id.delete_groups);
		newGroupsTask.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new_groups_remind();
			}
		});
		deleteGroups.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				deleteAlertDialog = new AlertDialog.Builder(ManageGroupsActivity.this).
						setTitle("警告").
						setMessage("你确定要删除这个群吗？").
						setNegativeButton("取消", new DialogInterface.OnClickListener() { 
							@Override 
							public void onClick(DialogInterface dialog, int which) {
							} 
						}).
						setPositiveButton("确定", new DialogInterface.OnClickListener() { 
							@Override 
							public void onClick(DialogInterface dialog, int which) {
								db.deleteGroup(groupsName);
								ManageGroupsActivity.this.finish();
							} 
						}).
						create();
				deleteAlertDialog.show();
			}
		});
		//更新头像！
		//取得垂直layout
		LinearLayout vl=(LinearLayout) findViewById(R.id.friendsHeadLayoutVertical);
		for(int i = 0;i < totalFriendsNumber;i+=4)
			addHeadHorizontalView(vl,i);

		connection();

	}

	private void addHeadHorizontalView(LinearLayout vl,int start)
	{
		//取得垂直layout
		View hlView = inflater.inflate(R.layout.manage_groups_head_horizontal, null);
		LinearLayout hl =(LinearLayout) hlView.findViewById(R.id.friendsHeadLayoutHorizontal);
		for(int i = 0; i < 4;i++)
			addHeadSingleView(hl , start+i);
		vl.addView(hl);
	}

	void addHeadSingleView(LinearLayout hl , int id)
	{
		View singleView = inflater.inflate(R.layout.manage_groups_head_single, null);
		ImageView iv = (ImageView) singleView.findViewById(R.id.friendsHead);
		TextView tv = (TextView) singleView.findViewById(R.id.friendsName);

		//宽度设置为长度的四分之一
		WindowManager wm = this.getWindowManager();
		int height = wm.getDefaultDisplay().getWidth()/4;
		singleView.setLayoutParams(new LinearLayout.LayoutParams(0, height+100, 1));
		singleView.setPadding(8, 8, 8, 8);

		if(id < totalFriendsNumber)
		{
			Map<String, String> friendMap = db.getFriendMessage(groupsMember.get(id));
			//得到application对象
			ApplicationInfo appInfo = getApplicationInfo();
			//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
			String picId = friendMap.get("pictureid");
			int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
			iv.setImageResource(resID);
			tv.setText(friendMap.get("nickname"));
			//TODO
		}
		else if(id == totalFriendsNumber)
		{
			iv.setImageResource(R.drawable.new_add);
			tv.setText("添加成员");
			singleView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ManageGroupsActivity.this , AddFriendsToGroupsActivity.class);
					intent.putExtra("groupsName", groupsName);
					ManageGroupsActivity.this.startActivity(intent);
				}
			});
		}
		hl.addView(singleView);
	}

	private void new_groups_remind()
	{
		Intent intent = new Intent(this , GroupRemindActivity.class);
		intent.putExtra("groupsName", groupsName);
		ManageGroupsActivity.this.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == android.R.id.home)
			ManageGroupsActivity.this.finish();
		else if(id == R.id.action_new_remind)
			new_groups_remind();
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
