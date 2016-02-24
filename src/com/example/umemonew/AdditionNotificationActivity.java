package com.example.umemonew;

import java.util.Map;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr , 林坤煜
 * 处理好友请求界面
 */
public class AdditionNotificationActivity extends Activity {

	private MyService TcpService;
	private Protocol protocol = null;

	private Button verifyButton = null;
	private Button refuseButton = null;
	private TextView nameDetail = null;
	private EditText remarkDetail = null;
	private TextView signatureDetail = null;
	private ImageView headView = null;
	private TextView usernameText = null;

	private Bundle bundle = null;

	private String message = "";

	private UmemoDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addition_notification);

		db = new UmemoDatabase(this, "umemo.db3", 1);

		connection(); // every activity should add
		findView();

		bundle = getIntent().getExtras();
		protocol = new Protocol();

		message = bundle.getString("message");

		setView(message);
	}

	private void setView(String message) {
		protocol.handleAdditionDetail(message);

		// get all kinds of messages
		String username = protocol.username_of_message;
		String headId = protocol.head_number;
		String nickname = protocol.nick_name;
		String signature = protocol.signature;

		usernameText.setText(username);

		// 得到application对象
		ApplicationInfo appInfo = getApplicationInfo();
		String picId = headId;
		int resID = getResources().getIdentifier("head" + picId, "raw",
				appInfo.packageName);
		headView.setImageResource(resID);

		// headView
		nameDetail.setText(nickname);
		// remarkDetail.setText(nickname);
		signatureDetail.setText(signature);
		verifyButton.setOnClickListener(handler);
		refuseButton.setOnClickListener(handler);
	}

	private void findView() {
		usernameText = (TextView) findViewById(R.id.username_message);
		verifyButton = (Button) findViewById(R.id.receiveButton);
		refuseButton = (Button) findViewById(R.id.cancelButton);
		nameDetail = (TextView) findViewById(R.id.nameDetail);
		remarkDetail = (EditText) findViewById(R.id.remarkDetail);
		signatureDetail = (TextView) findViewById(R.id.signature);
		headView = (ImageView) findViewById(R.id.headimage_message);
	}

	OnClickListener handler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.receiveButton:
				verify();
				break;
			case R.id.cancelButton:
				refuse();
				break;
			default:
				break;
			}
			AdditionNotificationActivity.this.finish();
		}
	};

	private void verify() {
		protocol.handleAdditionDetail(message);

		String username = db.getNowUser();
		Map<String, String> userMap = db.getLoginMessage(username);
		String friendName = protocol.username_of_message;
		String headImage = userMap.get("pictureid");
		String nickName = userMap.get("nickname");
		String signature = userMap.get("signature");

		String message = protocol.generateAddition(username, friendName,
				headImage, nickName, signature, 'Y');
		sendMessage(message);

		db.insertFriend(protocol.username_of_message, protocol.nick_name,
				protocol.head_number, protocol.signature, remarkDetail
						.getText().toString());
		toastShow("好友已保存！");
	}

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void refuse() {
		protocol.handleAdditionDetail(message);

		// you should get the message below from the database
		String username = db.getNowUser();
		Map<String, String> userMap = db.getLoginMessage(username);
		String friendName = protocol.username_of_message;
		String headImage = userMap.get("pictureid");
		String nickName = userMap.get("nickname");
		String signature = userMap.get("signature");

		String message = protocol.generateAddition(username, friendName,
				headImage, nickName, signature, 'N');
		sendMessage(message);
	}

	private void sendMessage(String message) {
		if (TcpService != null)
			TcpService.sendMessage(message);
		else
			Log.v("on click", "is null");
	}

	// this two part should be added into every activity
	private void connection() { // connect a service
		Intent intent = new Intent(this, MyService.class);
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
			// TcpService=((MyService.MyBinder)(service)).getService();
			Log.v("onServiceConnected", "hello");
			MyBinder binder = (MyBinder) service;
			TcpService = binder.getService();
			System.out.println(TcpService);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
