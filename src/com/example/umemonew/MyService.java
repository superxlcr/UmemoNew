package com.example.umemonew;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

//The background service binded with the application!
//If anyone modify something here, I will hit him! 

@SuppressLint("NewApi")				//very important
/***
 * 
 * @author 林坤煜
 * 后台连接服务
 */
public class MyService extends Service{

	private UmemoDatabase db;
	
	private Protocol protocol=null;

	private int tmp_timer;

	private static final int SERVERPORT = 20001;
	public TCPClient mTcpClient = null;	
	private connectTask conctTask = null;
	public final MyBinder binder=new MyBinder();

	private Timer heartBeatTimer= null;
	private TimerTask heartBeatTask = null;

	public static MyService instance = null;
	
	//	private Protocol 

	private void startHeartBeatThread(){			//heart beat package
		heartBeatTimer = new Timer();
		heartBeatTask = new TimerTask(){
			@Override
			public void run(){
				try{
					mTcpClient.sendMessage("\r\n");
					Log.v("heartbeat","send message");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		//TODO 25s send a heart beat message to the server (you can set it bigger)
		heartBeatTimer.schedule(heartBeatTask, 25000, 25000); 
	}

	private void checkThread(){			//check and restart package
		heartBeatTimer = new Timer();
		heartBeatTask = new TimerTask(){
			@Override
			public void run(){
				try{
					Log.v("check thread", "tmp: "+tmp_timer);
					if(tmp_timer==mTcpClient.timer){
						//TODO	Must not update the UI here!
						conctTask.cancel(true);
						conctTask = new connectTask();
						conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						Log.v("check thread","conctTask");	
					}
					tmp_timer=mTcpClient.timer;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		//TODO 30s check if get the heart_beat message from the server (you can set it bigger)
		//The number must bigger than that in the heart_beat thread!
		heartBeatTimer.schedule(heartBeatTask, 30000, 30000); 
	}

	@Override
	public IBinder onBind(Intent intent){
		return binder;
	}

	public class MyBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}

	public boolean sendMessage(String message){
		Log.v("MyService","send a message");
		if (mTcpClient != null) {
			mTcpClient.sendMessage(message);
			return true;
		}
		return false;
	}

	@Override
	public void onCreate(){
		super.onCreate();

		db = new UmemoDatabase(this, "umemo.db3", 1);
		
		protocol=new Protocol();

		// 单机版本不需要心跳以及检查是否掉线
//		conctTask = new connectTask();
//		Log.v("onCreate","conctTask");
//
//		conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//		Log.v("MyService","service is created!");
//
//		startHeartBeatThread();
//		checkThread();
		
		instance = this;
	}
	
	//receive the message from server with asyncTask 
	public class connectTask extends AsyncTask<String,String,TCPClient> {	 
		@Override
		//we create a TCPClient object 
		protected TCPClient doInBackground(String... message) {	
			mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
				@Override
				public void messageReceived(String message) {		//here the messageReceived method is implemented
					try{
						//TODO this method calls the onProgressUpdate
						publishProgress(message);

						if(message!=null){
							Log.v("Return Message from Socket::::: >>>>> ",message);
						}
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			},SERVERPORT);
			mTcpClient.run(db.getNowUser());			//run the TcpClient
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

			String message=values[0];
			Log.v("receive a message",message);

			//do something to change UI
			//And you can only modify here
			if(message.equals("heartbeat!")) 
			{	
				//Don't modify this part!
				Log.v("onProgerssUpdate","get a heartbeat");
//				makeToast("heartbeat!");
				mTcpClient.timer++;
			}
			else if(message.equals("No such person!")){
				makeToast("找不到该用户");
			}
			else if(message.equals("login success")){
//				makeToast("登录成功");
			}
			else if(message.equals("send success!")){
				makeToast("发送成功");
			}
			else if(message.length() >= 13){
				int taskNum = protocol.differMessage(message);
				String realMessage = message.substring(9);
				if(taskNum == 1){
					handleMessage(realMessage);
				}
				else if(taskNum == 2){
					handleAddition(realMessage);
				}
			}
		}
	}

	// 增加一个公共处理连接信息的方法用来手动处理消息
	public void handleConnection(String message) {
		if(message.equals("heartbeat!")) 
		{	
			//Don't modify this part!
			Log.v("onProgerssUpdate","get a heartbeat");
//			makeToast("heartbeat!");
			mTcpClient.timer++;
		}
		else if(message.equals("No such person!")){
			makeToast("找不到该用户");
		}
		else if(message.equals("login success")){
//			makeToast("登录成功");
		}
		else if(message.equals("send success!")){
			makeToast("发送成功");
		}
		else if(message.length() >= 13){
			int taskNum = protocol.differMessage(message);
			String realMessage = message.substring(9);
			if(taskNum == 1){
				handleMessage(realMessage);
			}
			else if(taskNum == 2){
				handleAddition(realMessage);
			}
		}
	}
	
	private void handleMessage(String message){
		makeToast("您有一条新消息");
		setNotification(message,0);
	}

	private void handleAddition(String message){
		
		Log.v("MyService", message);
		int messageLen = message.length();
		if(message.charAt(messageLen-1)=='A'){
//			makeToast("有人要加你哦亲...");
			setNotification(message,1);
		}
		else if(message.charAt(messageLen-1)=='Y'){
			
			
			protocol.handleAdditionDetail(message);
			db.insertFriend(protocol.username_of_message, protocol.nick_name, protocol.head_number, protocol.signature, "");
//			makeToast("某人同意了你的请求...");
			setNotification(message,2);
		}
		else if(message.charAt(messageLen-1)=='N'){
			
//			makeToast("某人拒绝了你的请求...");
			setNotification(message,3);
		}
	}

	public void setNotification(String message,int mId)
	{
		//TODO
		if(mId == 0)	
		{
			protocol.handleMessageDetail(message);

			NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent(MyService.this, MessageNotificationActivity.class);
			intent.putExtra("message",message);			//pass the string to the activity	
			PendingIntent pi = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

			Notification mBuilder = new Notification.Builder(MyService.this)
				.setContentTitle(protocol.username_of_message+"发来一条备忘")						//设置通知栏标题
				.setContentText(protocol.title) 
				.setContentIntent(pi) 
				.setTicker("您收到一条新的备忘") 							//animation effects
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setSmallIcon(R.drawable.logo)//small icon
				.build();
			manager.notify(mId,mBuilder);
		}
		else if(mId == 1)
		{
			protocol.handleAdditionDetail(message);

			NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent(MyService.this, AdditionNotificationActivity.class);
			intent.putExtra("message",message);			//pass the string to the activity	
			PendingIntent pi = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

			Notification mBuilder = new Notification.Builder(MyService.this)
				.setContentTitle(protocol.username_of_message+"发来验证请求")						//设置通知栏标题
				.setContentText("我是"+protocol.nick_name) 
				.setContentIntent(pi) 
				.setTicker("您收到一条新的验证请求") 							//animation effects
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setSmallIcon(R.drawable.logo)			//small icon
				.build();
			manager.notify(mId,mBuilder);
		}
		else if(mId == 2)
		{
			protocol.handleAdditionDetail(message);

			NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

			Notification mBuilder = new Notification.Builder(MyService.this)
				.setContentTitle(protocol.username_of_message+"同意了你的验证请求")						//设置通知栏标题
				.setContentText("我们已经是朋友了") 
				.setTicker("您收到一条新的通知") 							//animation effects
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setSmallIcon(R.drawable.logo)			//small icon
				.build();
			manager.notify(mId,mBuilder);
		}
		else if(mId == 3)
		{
			protocol.handleAdditionDetail(message);

			NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

			Notification mBuilder = new Notification.Builder(MyService.this)
				.setContentTitle(protocol.username_of_message+"拒绝了你的验证请求")						//设置通知栏标题
				.setContentText("") 
				.setTicker("您收到一条新的通知") 							//animation effects
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setSmallIcon(R.drawable.logo)			//small icon
				.build();
			manager.notify(mId,mBuilder);
		}
	}

	public void makeToast(String str){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
	}

	@Override  
	public int onStartCommand(Intent intent, int flags, int startId) {  
		Log.d("MyService_Start", "onStartCommand() executed");  
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);  
	}  

	@Override  
	public void onDestroy() { 
		try{
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
		Intent Intent = new Intent(this, MyService.class);  
		startService(Intent);
	}  
}
