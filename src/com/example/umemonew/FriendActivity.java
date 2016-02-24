package com.example.umemonew;

import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.umemonew.MyService.MyBinder;

public class FriendActivity extends Activity {
	private MyService TcpService;
	private Protocol protocol = null;

	private ImageView imageView2;

	private TextView friendUsername;
	private TextView friendNickname;
	private TextView friendSignature;

	private EditText friendNote;

	private Button deleteFriend;
	private Button editNote;

	private Drawable drawable;

	private UmemoDatabase db=null;

	private String friendName= "";

	private int state = 0;

	private void connection(){			//connect a service
		Intent intent=new Intent(FriendActivity.this,MyService.class);
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
		setContentView(R.layout.friend_data);

		connection();

		db=new UmemoDatabase(this, "umemo.db3", 1);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("������Ϣ");

		friendName = getIntent().getStringExtra("username");

		initViews();

		friendNote.setEnabled(false);

		Map<String , String> friendMap = db.getFriendMessage(friendName);
		friendUsername.setText(friendName);
		friendNickname.setText(friendMap.get("nickname"));
		friendNote.setText(friendMap.get("note"));
		friendSignature.setText(friendMap.get("signature"));
		friendSignature.setSelected(true);

		//�õ�application����
		ApplicationInfo appInfo = getApplicationInfo();
		//�õ���ͼƬ��id(name �Ǹ�ͼƬ�����֣�drawable �Ǹ�ͼƬ��ŵ�Ŀ¼��appInfo.packageName��Ӧ�ó���İ�)
		String headId = friendMap.get("pictureid");
		int resID = getResources().getIdentifier("head"+headId, "raw", appInfo.packageName);
		imageView2.setImageResource(resID);

		editNote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (state == 0) {
					state = 1;
					friendNote.setEnabled(true);
					friendNote.setBackground(drawable);
					editNote.setBackground(getResources().getDrawable(R.drawable.blue_tick));
				}
				else {
					state = 0;
					friendNote.setEnabled(false);
					editNote.setBackground(getResources().getDrawable(R.drawable.edit));
					db.changeFriendNote(friendName, friendNote.getText().toString());
					friendSignature.setSelected(true);
				}
			}
		});

		deleteFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(FriendActivity.this);
				dialog.setTitle("ȷ��ɾ����");
				dialog.setMessage("��ȷ��ɾ���ú�����");
				dialog.setCancelable(false);
				dialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						db.deleteFriend(friendName);
						FriendActivity.this.finish();
					}
				});
				dialog.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				dialog.show();
			}
		});

	}

	private void initViews() {
		imageView2 = (ImageView) findViewById(R.id.headview2);
		friendUsername = (TextView) findViewById(R.id.friend_username);
		friendNickname = (TextView) findViewById(R.id.friend_nickname);
		friendSignature = (TextView) findViewById(R.id.friend_signature);
		friendNote = (EditText) findViewById(R.id.friend_note);
		editNote = (Button) findViewById(R.id.edit_note);
		deleteFriend = (Button) findViewById(R.id.delete_friend);
		drawable = friendNote.getBackground();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.second_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home)
			FriendActivity.this.finish();
		else if(id == R.id.action_new_remind) {				
			Intent intent = new Intent(this, FriendRemindActivity.class);
			intent.putExtra("username", friendName);
			this.startActivity(intent);
			return true;
		}
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
