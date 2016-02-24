package com.example.umemonew;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.example.umemonew.MyService.MyBinder;

/***
 * 
 * @author 方泽华 , 林坤煜
 * 全局应用信息,初始化TCP连接以及百度地图SDK
 */
public class MainApplication extends Application{
	public MyService TcpService;
	public static MainApplication instance = null;

	private void connection(){			//connect a service
		Intent intent=new Intent(MainApplication.this,MyService.class);
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
		public void onServiceConnected(ComponentName name,IBinder service){			//get a TcpService
			Log.v("onServiceConnected","hello");
			MyBinder binder = (MyBinder)service;
			TcpService = binder.getService();

			Log.v("onServiceConnected","get service");
			//TODO
			//System.out.println(TcpService);
			//            TcpService.sendMessage("\r\nlinkyfuck");
		}
	};

	@Override  
	public void onCreate() {  
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
//		instance=this;
//		connection();

	}     

	@Override  
	public void onLowMemory() {  
		super.onLowMemory();  
	}  
}
