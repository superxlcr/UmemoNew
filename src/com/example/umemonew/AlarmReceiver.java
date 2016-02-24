package com.example.umemonew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Not used!
/***
 * 
 * @author 林炜润
 * 提醒广播接收器
 */
public class AlarmReceiver extends BroadcastReceiver{
	@Override 
	public void onReceive(Context context,Intent intent){
		Intent it = new Intent(context, AlarmActivity.class);
		
		int id=intent.getIntExtra("id",-1);
		it.putExtra("id",id);
		it.putExtra("type","time");
		
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);
	}

}
