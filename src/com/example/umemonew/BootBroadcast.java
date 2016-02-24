package com.example.umemonew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//开机启动服务*
		Intent service=new Intent(context, MyService.class);
		context.startService(service);
		
		Log.v("BootBroadcast","hello");
	}

}
