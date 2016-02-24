package com.example.umemonew;

import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author Superxlcr , 林坤煜
 * 登陆主界面
 */
public class LoginActivity extends Activity {

	private MyService TcpService;
	private Protocol protocol = null;

	private EditText Username;
	private EditText Password;
	private Button LoginBtn;
	private ImageView HeadView;
	private TextView Register;
	private TextView ForgetPassword;

	private SharedPreferences sharedPreferences;  
	private SharedPreferences.Editor editor;

	private UmemoDatabase db=null;

	public static Activity instance = null; 

	private String picId = "00";

	private void connection(){			//connect a service
		Intent intent=new Intent(LoginActivity.this,MyService.class);
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
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_login);

		db=new UmemoDatabase(this, "umemo.db3", 1);

		Username = (EditText) findViewById(R.id.username);
		Password = (EditText) findViewById(R.id.password);
		LoginBtn = (Button) findViewById(R.id.loginButton);
		HeadView = (ImageView) findViewById(R.id.headview);
		Register = (TextView) findViewById(R.id.register);
		ForgetPassword = (TextView) findViewById(R.id.forgetPassword);
		instance = this;

		//自动填写密码
		sharedPreferences = this.getSharedPreferences("umemo",MODE_PRIVATE);
		editor = sharedPreferences.edit();

		String user_name = sharedPreferences.getString("username",null);  
		String pass_word = sharedPreferences.getString("password",null);
		String headId = sharedPreferences.getString("head" , "00");
		int login=sharedPreferences.getInt("LOGINABLE",0);
		Username.setText(user_name);
		Password.setText(pass_word);
		//得到application对象
		ApplicationInfo appInfo = getApplicationInfo();
		int resID = getResources().getIdentifier("head"+headId, "raw", appInfo.packageName);
		HeadView.setImageResource(resID);

		//如果登陆过就自动登陆
		if(login != 0)
		{
			db.upgradeNowUser(user_name);
			Intent intent = new Intent(LoginActivity.this , SecondActivity.class);
			connection();
			LoginActivity.this.startActivity(intent);
		}

		//输入用户名刷新头像
		Username.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus)
				{
					String UsernameString = Username.getText().toString();
					Map<String, String> UserMap = db.getLoginMessage(UsernameString);
					if(UserMap.isEmpty())
					{
						//得到application对象
						ApplicationInfo appInfo = getApplicationInfo();
						int resID = getResources().getIdentifier("head00", "raw", appInfo.packageName);
						HeadView.setImageResource(resID);
					}
					else
					{
						picId = UserMap.get("pictureid");
						//得到application对象
						ApplicationInfo appInfo = getApplicationInfo();
						int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
						HeadView.setImageResource(resID);
					}
				}
			}
		});

		LoginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String UsernameString = Username.getText().toString();
				String PasswordString = Password.getText().toString();
				if(UsernameString.isEmpty() || PasswordString.isEmpty())
					toastShow("用户名或密码为空！");
				else
				{
					if(db.checkLoginMessage(UsernameString, PasswordString))
					{
						editor.putString("username",UsernameString);
						editor.putString("password",PasswordString);
						editor.putString("head", picId);
						editor.putInt("LOGINABLE",1);
						editor.commit();
						db.upgradeNowUser(UsernameString);
						Intent intent = new Intent(LoginActivity.this , SecondActivity.class);
						connection();
						LoginActivity.this.startActivity(intent);
					}
					else
						toastShow("用户名或密码错误！");
				}
			}
		});

		Register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
				LoginActivity.this.startActivity(intent);
			}
		});

		ForgetPassword.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toastShow("忘记密码了吧～");
			}
		});

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
		return super.onOptionsItemSelected(item);
	}

	public void toastShow(String text) {  
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();  
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}

}
