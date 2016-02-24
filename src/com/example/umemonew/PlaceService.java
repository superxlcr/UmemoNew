package com.example.umemonew;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


public class PlaceService extends Service
{
	private Vibrator mVibrator;
	private LocationClient mLocationClient;
	private NotiftLocationListener listener;
	private double now_longitude , now_latitude;
	private double longitude,latitude;
	private NotifyLister mNotifyLister;
	
	private LocationMode tempMode = LocationMode.Hight_Accuracy;//
	private String tempcoor="bd09ll";//
	
	private int id = 0;
	
	String Longitude , Latitude ; 
	// 必须实现的方法
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	// Service被创建时回调该方法。
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.v("onCreat" , "Service is OnCreat") ; 
		
		listener = new NotiftLocationListener();
		mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		
		mLocationClient  = new LocationClient(this);
		mLocationClient.registerLocationListener(listener);
		
		// 我自己的改进
		LocationClientOption option = new LocationClientOption();
		option.setProdName("@string/app_name") ; // 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setOpenGps(true);			// 打开GPS,这个设置还待商榷。。。  
		option.setLocationMode(tempMode);	//设置定位模式
		option.setCoorType(tempcoor);		//返回的定位结果是百度经纬度，默认值gcj02
		option.setIsNeedAddress(true); 		// 设置为 需要获得地址
		
		mLocationClient.setLocOption(option) ;
		mLocationClient.start();		
	}
	// Service被启动时回调该方法
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		
		if(intent.hasExtra("Longitude"))
			 Longitude = intent.getStringExtra("Longitude") ;
		 
		if(intent.hasExtra("Latitude"))
			 Latitude = intent.getStringExtra("Latitude") ;
		
		id = intent.getIntExtra("id", 0);
		
		longitude = Double.parseDouble(Longitude) ; 
		latitude = Double.parseDouble(Latitude) ; 
		
		mLocationClient.start(); 
		return START_STICKY;
	}

	private Handler notifyHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mNotifyLister = new NotifyLister();
			mNotifyLister.SetNotifyLocation(latitude,longitude, 100 ,"bd09ll");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
			mLocationClient.registerNotify(mNotifyLister);
		}
		
	};
	
	public class NotiftLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			if(location != null)
			{
				now_longitude = location.getLongitude();
				now_latitude = location.getLatitude();
				notifyHandler.sendEmptyMessage(0);
			}			
		}
	}
	
	public class NotifyLister extends BDNotifyListener{
	    public void onNotify(BDLocation mlocation, float distance){
	    	//mVibrator.vibrate(1000);//振动提醒已到设定位置附近
	    	Intent intent3=new Intent(getBaseContext(),AlarmActivity.class);
	    	intent3.putExtra("type", "place");
	    	intent3.putExtra("id" , id);
	    	intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(intent3);
	    }
	}
	
	// Service被关闭之前回调。
		@Override
		public void onDestroy()
		{
			Log.v("onDestroy" , "Service is Destroyed") ;
			super.onDestroy();
			
			//清空 
			mLocationClient.removeNotifyEvent(mNotifyLister);
			mLocationClient = null;
			mNotifyLister= null;
			listener = null;
		}
}
