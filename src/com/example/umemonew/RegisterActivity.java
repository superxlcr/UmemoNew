package com.example.umemonew;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
/***
 * 
 * @author Superxlcr , 林坤煜
 * 注册界面
 */
public class RegisterActivity extends Activity {

	private Button registerButton = null;
	private EditText Username = null;
	private EditText Password = null;
	private EditText Nickname = null;
	private EditText PersonalSign = null;
	private ImageView Head = null;
	private TextView HeadHintText = null;
	private String headId = "00";

	private TcpClientLogin mTcpClient = null;	
	private connectTask conctTask = null;
	private static final int LOGINPORT = 20003;

	private UmemoDatabase db=null;

	private void findView(){
		registerButton=(Button)findViewById(R.id.registerButton);
		Username=(EditText)findViewById(R.id.username);
		Password=(EditText)findViewById(R.id.password);
		Nickname=(EditText)findViewById(R.id.nickname);
		PersonalSign=(EditText)findViewById(R.id.personalSign);
		Head=(ImageView)findViewById(R.id.headview);
		HeadHintText=(TextView)findViewById(R.id.hintText);
		mTcpClient = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{        
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("注册用户");

		setContentView(R.layout.activity_register);

		db=new UmemoDatabase(this, "umemo.db3", 1);

		findView();				//find all the view to use

		Head.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RegisterActivity.this, ChooseHeadActivity.class);
				RegisterActivity.this.startActivityForResult(intent, 0);
			}
		});

		// connect to the server
		conctTask = new connectTask();
		conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(Username.getText().toString().isEmpty())
					toastShow("您的用户名为空！");
				else if(Password.getText().toString().isEmpty())
					toastShow("您的密码为空！");
				else if(Nickname.getText().toString().isEmpty())
					toastShow("您的昵称为空！");
				else if(PersonalSign.getText().toString().isEmpty())
					toastShow("您的个性签名为空！");
				else if(headId == "00")
					toastShow("您还没有选择头像！");
				else if(db.checkRepeatUsername(Username.getText().toString()))
					toastShow("您的用户名已被使用！");
				else
				{
					//sends the message to the server
					if (mTcpClient != null) 
					{
						mTcpClient.sendMessage("2 "+ Username.getText().toString() +" "+ Password.getText().toString());
					}
				}


			}
		});
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
		if(requestCode == 0 && resultCode == 1)
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
	/**
	 * receive the message from server with asyncTask  
	 **/
	public class connectTask extends AsyncTask<String,String,TcpClientLogin> {
		@Override
		protected TcpClientLogin doInBackground(String... message) 
		{
			//we create a TCPClient object and
			mTcpClient = new TcpClientLogin(new TcpClientLogin.OnMessageReceived() 
			{
				@Override
				//here the messageReceived method is implemented
				public void messageReceived(String message) {
					try{
						//this method calls the onProgressUpdate
						publishProgress(message);			

						if(message!=null){
							Log.v("Return Message from Socket::::: >>>>> ",message);
						}
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			},LOGINPORT);
			mTcpClient.run();			//run the TcpClient
			if(mTcpClient!=null)
			{
				mTcpClient.sendMessage("Initial Message when connected with Socket Server");		//send message
			}
			return null;
		}

		@Override
		//called by publishProgress
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			String tmp=values[0];
			Log.v("onProcessUpdate",tmp);
			if (tmp.equals("11"))
			{
				toastShow("注册成功！");

				String UsernameString = Username.getText().toString();
				String PasswordString = Password.getText().toString();
				String NicknameString = Nickname.getText().toString();
				String PersonalSignString = PersonalSign.getText().toString();
				String HeadId = headId;
				db.insertLoginMessage(UsernameString, PasswordString);
				db.changeNickname(UsernameString, NicknameString);
				db.changePictureId(UsernameString, HeadId);
				db.changeSignature(UsernameString, PersonalSignString);
				RegisterActivity.this.finish();
			}
			else if (tmp.equals("20"))
			{
				toastShow("您的用户名已被使用！");
			}
			else {
				toastShow("注册失败，出现了错误！");
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
			RegisterActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		try
		{
			Log.v("onDestroy.","send a bye");
			mTcpClient.sendMessage("bye");
			mTcpClient.stopClient();
			conctTask.cancel(true);
			conctTask = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		super.onDestroy();
	}
}
