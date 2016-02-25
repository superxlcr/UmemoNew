package com.example.umemonew;

import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

//@SuppressLint("NewApi")
/***
 * 
 * @author 林坤煜 添加好友界面
 */
public class AdditionActivity extends Activity {

	private MyService TcpService;
	private Protocol protocol = null;

	private Button sendButton = null;
	private EditText friendNameText = null;

	private UmemoDatabase db;

	private void findView() {
		sendButton = (Button) findViewById(R.id.sendButton);
		friendNameText = (EditText) findViewById(R.id.friendNameText);
	}

	private void connection() { // connect a service
		Intent intent = new Intent(AdditionActivity.this, MyService.class);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addition);
		protocol = new Protocol();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("添加好友");

		db = new UmemoDatabase(this, "umemo.db3", 1);

		connection();
		findView(); // find all the view to use
		sendButton.setOnClickListener(handler);
	}

	OnClickListener handler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.sendButton:
				if (friendNameText.getText().toString().isEmpty())
					toastShow("您查找的用户名为空！");
				else if (db.checkFriend(friendNameText.getText().toString()))
					toastShow("您已添加过此好友！");
				else
					sendMessage(); // send a message
				break;
			default:
				break;
			}
		}
	};

	private void sendMessage() {

		String username = db.getNowUser();
		Map<String, String> userMap = db.getLoginMessage(username);
		String friendName = friendNameText.getText().toString();
		String headCode = userMap.get("pictureid");
		String nickName = userMap.get("nickname");
		String signature = userMap.get("signature");

		String message = protocol.generateAddition(username, friendName,
				headCode, nickName, signature, 'A');
		Log.v("FriendActivity", "sending message");

		// 此处添加3个单机版朋友,1号默认接收请求,2号默认发送镜像请求,3号默认拒绝
		if (TcpService != null) {
			if (friendName.equals("friend1")) {
				String Friend1Message = protocol.generateAddition(friendName, username,
						"07", "野人地狱咆哮", "我很脑残我只加好友", 'A');
				TcpService.handleConnection(protocol.agreeToAdd(Friend1Message));
			} else if (friendName.equals("friend2")) {
				String Friend2Message = protocol.generateAddition(friendName, username,
						"14", "法师吉安娜", "添加反弹……", 'A');
				TcpService.handleConnection(Friend2Message);
			} else if (friendName.equals("friend3")) {
				String Friend3Message = protocol.generateAddition(friendName, username,
						"39", "高冷的西西里", "拒绝你！", 'A');
				TcpService.handleConnection(protocol.refuseToAdd(Friend3Message));
			} else {
				toastShow("单机版请添加friend1,friend2或friend3～");
			}
		}
		// 服务器已关闭,不需要再发送信息
		// if(TcpService!=null)
		// TcpService.sendMessage(message);
		else
			Log.v("on click", "is null");

		// friendNameText.setText(friendName+"!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.none, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home)
			AdditionActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}