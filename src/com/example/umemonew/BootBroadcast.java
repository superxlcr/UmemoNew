package com.example.umemonew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/***
 * 
 * @author ������
 * ���������㲥�Ľ�����
 */
public class BootBroadcast extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//������������*
		Intent service=new Intent(context, MyService.class);
		context.startService(service);
		
		Log.v("BootBroadcast","hello");
	}

}
