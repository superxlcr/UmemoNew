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
 * �½�Ⱥ�����
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
		actionBar.setTitle("����Ⱥ��");

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
					toastShow("����ûѡ��Ⱥ��ͷ��");
				else if(groupsName.getText().toString().isEmpty())
					toastShow("����û����Ⱥ������");
				else if(!db.getGroupPicture(groupsName.getText().toString()).isEmpty())
					toastShow("����Ⱥ�����ѱ�ʹ�ã�");
				else if(addFriends.isEmpty())
					toastShow("����ûѡ���κκ��ѣ�");
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
			//���ûд��ע����Ϊ�û���
			if(friendsStringArray.get(i).get("note").isEmpty())
				map.put("friendsName", friendsStringArray.get(i).get("nickname"));
			else
				map.put("friendsName", friendsStringArray.get(i).get("note"));
			//�õ�application����
			ApplicationInfo appInfo = getApplicationInfo();
			//�õ���ͼƬ��id(name �Ǹ�ͼƬ�����֣�drawable �Ǹ�ͼƬ��ŵ�Ŀ¼��appInfo.packageName��Ӧ�ó���İ�)
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

		//��¼����ĺ���
		addFriends = new ArrayList<String>();
		
		listView = (ListView) findViewById(R.id.newGroupsFriendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(this,getData(),R.layout.new_groups_friends_item,
				new String[]{"friendsName","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsHead,R.id.friendsUsername});
		//û����ͼƬ��idΪfriendsHead
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(itemListener);
	}

	// ��Ŀ�ϵ���������.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {
			ImageView CheckImage = (ImageView) view.findViewById(R.id.checkImage);
			TextView Username = (TextView) view.findViewById(R.id.friendsUsername);
			if(CheckImage.getVisibility() == 0) // ȡ��
			{
				CheckImage.setVisibility(4);//���ɼ���ռ�ռ�
				addFriends.remove(Username.getText().toString());
			}
			else // ѡ��
			{
				CheckImage.setVisibility(0);//�ɼ�
				addFriends.add(Username.getText().toString());
			}
		}  
	};

	//ˢ��ͷ��
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
		if(requestCode == 4 && resultCode == 1)
		{
			headId = data.getStringExtra("headId");
			if(headId != "00")
			{
				//�õ�application����
				ApplicationInfo appInfo = getApplicationInfo();
				String picId = headId;
				int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
				Head.setImageResource(resID);
				HeadHintText.setVisibility(4);//������ʾ����ռ�ռ�
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
