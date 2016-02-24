package com.example.umemonew;

import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author 庄乐豪
 * 个人资料界面
 */
public class PrivateDataActivity extends Activity {
	private MyService TcpService;
	private Protocol protocol = null;

	private LinearLayout headLayout1;
	private ImageView imageView1;

	private TextView hintText1;
	private TextView editUsername;
	private EditText editNickname;
	private TextView textSignature;
	private EditText editSignature;

	private Button editMessage;

	private Drawable drawable;

	private UmemoDatabase db=null;

	private String headId = "00";

	private void connection(){			//connect a service
		Intent intent=new Intent(PrivateDataActivity.this,MyService.class);
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
		setContentView(R.layout.private_data);

		connection();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("个人资料");

		db=new UmemoDatabase(this, "umemo.db3", 1);

		initViews();

		hintText1.setVisibility(8);
		headLayout1.setEnabled(false);
		imageView1.setEnabled(false);
		editNickname.setEnabled(false);
		editSignature.setVisibility(8);

		editUsername.setText(db.getNowUser());
		Map<String , String> userMap = db.getLoginMessage(db.getNowUser());
		editNickname.setText(userMap.get("nickname"));
		textSignature.setText(userMap.get("signature"));
		editSignature.setText(userMap.get("signature"));
		//得到application对象
		ApplicationInfo appInfo = getApplicationInfo();
		//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
		headId = userMap.get("pictureid");
		int resID = getResources().getIdentifier("head"+headId, "raw", appInfo.packageName);
		imageView1.setImageResource(resID);
		textSignature.setSelected(true);

		imageView1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PrivateDataActivity.this , ChooseHeadActivity.class);
				PrivateDataActivity.this.startActivityForResult(intent, 5);
			}
		});


		editMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editMessage.getText().toString().equals(getResources().getString(R.string.edit))) {
					editMessage.setText(getResources().getString(R.string.save_message));
					hintText1.setVisibility(0);
					headLayout1.setEnabled(true);
					imageView1.setEnabled(true);
					editNickname.setEnabled(true);
					textSignature.setVisibility(8);
					editSignature.setVisibility(0);
				}
				else {
					editMessage.setText(getResources().getString(R.string.edit));
					hintText1.setVisibility(8);
					headLayout1.setEnabled(false);
					imageView1.setEnabled(false);
					editNickname.setEnabled(false);
					textSignature.setVisibility(0);
					editSignature.setVisibility(8);
					textSignature.setText(editSignature.getText().toString());
					
					db.changeNickname(db.getNowUser(), editNickname.getText().toString());
					db.changeSignature(db.getNowUser(), editSignature.getText().toString());
					db.changePictureId(db.getNowUser(), headId);
					
					Toast.makeText(PrivateDataActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
					textSignature.setSelected(true);
				}
			}
		});

	}

	private void initViews() {
		headLayout1 = (LinearLayout) findViewById(R.id.headlayout1);
		imageView1 = (ImageView) findViewById(R.id.headview1);
		hintText1 = (TextView) findViewById(R.id.hinttext1);
		editUsername = (TextView) findViewById(R.id.edit_username);
		editNickname = (EditText) findViewById(R.id.edit_nickname);
		editSignature = (EditText) findViewById(R.id.edit_signature);
		editMessage = (Button) findViewById(R.id.edit_message);
		textSignature = (TextView) findViewById(R.id.text_signature);
		drawable = editNickname.getBackground();
	}

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
		if(requestCode == 5 && resultCode == 1)
		{
			headId = data.getStringExtra("headId");
			//得到application对象
			ApplicationInfo appInfo = getApplicationInfo();
			String picId = headId;
			int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
			imageView1.setImageResource(resID);

		}
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
			PrivateDataActivity.this.finish();
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
