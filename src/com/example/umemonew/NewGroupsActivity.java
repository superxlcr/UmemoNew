package com.example.umemonew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr
 * 新建群组界面
 */
public class NewGroupsActivity extends Activity{
	private MyService TcpService;
	private Protocol protocol = null;

	private Button newGroupsButton = null;
	private EditText groupsName = null;
	private ImageView Head = null;
	private TextView HeadHintText = null;
	private String headId = "00";

	private ListView listView; 
	private ArrayList<Map<String, Object>> friendslist;	
	private SimpleAdapter adapter;

	private UmemoDatabase db=null; 

	private ArrayList<String> addFriends;
	
	private void connection(){			//connect a service
		Intent intent=new Intent(NewGroupsActivity.this,MyService.class);
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
	public void onCreate(Bundle savedInstanceState)
	{        
		super.onCreate(savedInstanceState);

		connection();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("创建群组");

		setContentView(R.layout.activity_new_groups);

		db=new UmemoDatabase(this, "umemo.db3", 1);

		findView();				//find all the view to use

		Head.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewGroupsActivity.this, ChooseHeadActivity.class);
				NewGroupsActivity.this.startActivityForResult(intent, 4);
			}
		});

		newGroupsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(headId == "00")
					toastShow("您还没选择群组头像！");
				else if(groupsName.getText().toString().isEmpty())
					toastShow("您还没输入群组名！");
				else if(!db.getGroupPicture(groupsName.getText().toString()).isEmpty())
					toastShow("您的群组名已被使用！");
				else if(addFriends.isEmpty())
					toastShow("您还没选择任何好友！");
				else
				{
					db.newGroup(groupsName.getText().toString(), addFriends, headId);
					NewGroupsActivity.this.finish();
				}
			}
		});
	}

	private ArrayList<Map<String,Object>> getData()
	{
		ArrayList<Map<String,String>> friendsStringArray = db.getFriendList();
		for(int i = 0;i < friendsStringArray.size();i++)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			//如果没写备注，则为用户名
			if(friendsStringArray.get(i).get("note").isEmpty())
				map.put("friendsName", friendsStringArray.get(i).get("nickname"));
			else
				map.put("friendsName", friendsStringArray.get(i).get("note"));
			//得到application对象
			ApplicationInfo appInfo = getApplicationInfo();
			//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
			String picId = friendsStringArray.get(i).get("pictureid");
			int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
			map.put("friendsHead", resID);
			map.put("friendsUsername", friendsStringArray.get(i).get("username"));
			friendslist.add(map);
		}
		return friendslist;
	}

	private void findView(){
		newGroupsButton=(Button)findViewById(R.id.newGroupsButton);
		groupsName=(EditText)findViewById(R.id.groupsName);
		Head=(ImageView)findViewById(R.id.groupsHeadView);
		HeadHintText=(TextView)findViewById(R.id.hintText);

		//记录加入的好友
		addFriends = new ArrayList<String>();
		
		listView = (ListView) findViewById(R.id.newGroupsFriendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(this,getData(),R.layout.new_groups_friends_item,
				new String[]{"friendsName","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsHead,R.id.friendsUsername});
		//没加入图片，id为friendsHead
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(itemListener);
	}

	// 条目上单击处理方法.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {
			ImageView CheckImage = (ImageView) view.findViewById(R.id.checkImage);
			TextView Username = (TextView) view.findViewById(R.id.friendsUsername);
			if(CheckImage.getVisibility() == 0) // 取消
			{
				CheckImage.setVisibility(4);//不可见但占空间
				addFriends.remove(Username.getText().toString());
			}
			else // 选中
			{
				CheckImage.setVisibility(0);//可见
				addFriends.add(Username.getText().toString());
			}
		}  
	};

	//刷新头像
	/**
	 * 所有的Activity对象的返回值都是由这个方法来接收
	 * requestCode:    表示的是启动一个Activity时传过去的requestCode值
	 * resultCode：表示的是启动后的Activity回传值时的resultCode值
	 * data：表示的是启动后的Activity回传过来的Intent对象
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 4 && resultCode == 1)
		{
			headId = data.getStringExtra("headId");
			if(headId != "00")
			{
				//得到application对象
				ApplicationInfo appInfo = getApplicationInfo();
				String picId = headId;
				int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
				Head.setImageResource(resID);
				HeadHintText.setVisibility(4);//隐藏提示但仍占空间
			}
		}
	}

	public void toastShow(String text) {  
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.none, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home)
			NewGroupsActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
	
}
