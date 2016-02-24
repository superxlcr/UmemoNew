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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

public class AddFriendsToGroupsActivity extends Activity{
	private MyService TcpService;
	private Protocol protocol = null;

	private Button confirmButton = null;

	private ListView listView; 
	private ArrayList<Map<String, Object>> friendslist;	
	private SimpleAdapter adapter;

	private UmemoDatabase db=null; 

	private ArrayList<String> addFriends;
	private String groupsName = "";
	
	private void connection(){			//connect a service
		Intent intent=new Intent(AddFriendsToGroupsActivity.this,MyService.class);
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
		actionBar.setTitle("选择添加进入群组的好友");

		setContentView(R.layout.activity_add_friends_to_groups);

		db=new UmemoDatabase(this, "umemo.db3", 1);

		groupsName = getIntent().getStringExtra("groupsName");
		
		findView();				//find all the view to use

		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(addFriends.isEmpty())
					toastShow("您还没选择任何好友！");
				else
				{
					db.addGroupMembers(groupsName, addFriends);
					ManageGroupsActivity.instance.finish();
					AddFriendsToGroupsActivity.this.finish();
				}
			}
		});
	}

	private ArrayList<Map<String,Object>> getData()
	{
		ArrayList<Map<String,String>> friendsStringArray = db.getFriendsNotInGroup(groupsName);
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
		confirmButton=(Button)findViewById(R.id.confirmButton);
		
		//记录加入的好友
		addFriends = new ArrayList<String>();
		
		listView = (ListView) findViewById(R.id.FriendsListView);  
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
			AddFriendsToGroupsActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}
}
