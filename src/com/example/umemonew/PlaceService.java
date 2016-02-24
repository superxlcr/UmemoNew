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
	// ����ʵ�ֵķ���
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	// Service������ʱ�ص��÷�����
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.v("onCreat" , "Service is OnCreat") ; 
		
		listener = new NotiftLocationListener();
		mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		
		mLocationClient  = new LocationClient(this);
		mLocationClient.registerLocationListener(listener);
		
		// ���Լ��ĸĽ�
		LocationClientOption option = new LocationClientOption();
		option.setProdName("@string/app_name") ; // ���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
		option.setOpenGps(true);			// ��GPS,������û�����ȶ������  
		option.setLocationMode(tempMode);	//���ö�λģʽ
		option.setCoorType(tempcoor);		//���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		option.setIsNeedAddress(true); 		// ����Ϊ ��Ҫ��õ�ַ
		
		mLocationClient.setLocOption(option) ;
		mLocationClient.start();		
	}
	// Service������ʱ�ص��÷���
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
			mNotifyLister.SetNotifyLocation(latitude,longitude, 100 ,"bd09ll");//4����������Ҫλ�����ѵĵ�����꣬���庬������Ϊ��γ�ȣ����ȣ����뷶Χ������ϵ����(gcj02,gps,bd09,bd09ll)
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
	    	//mVibrator.vibrate(1000);//�������ѵ��趨λ�ø���
	    	Intent intent3=new Intent(getBaseContext(),AlarmActivity.class);
	    	intent3.putExtra("type", "place");
	    	intent3.putExtra("id" , id);
	    	intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(intent3);
	    }
	}
	
	// Service���ر�֮ǰ�ص���
		@Override
		public void onDestroy()
		{
			Log.v("onDestroy" , "Service is Destroyed") ;
			super.onDestroy();
			
			//��� 
			mLocationClient.removeNotifyEvent(mNotifyLister);
			mLocationClient = null;
			mNotifyLister= null;
			listener = null;
		}
}
