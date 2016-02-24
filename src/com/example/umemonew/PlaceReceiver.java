package com.example.umemonew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PlaceReceiver extends BroadcastReceiver {
	@Override	
	public void onReceive(Context context,Intent intent){
	     String Longitude, Latitude; 
	     Longitude = intent.getStringExtra("Longitude") ; 
	     Latitude = intent.getStringExtra("Latitude") ; 
		 		 
	     Intent intent2 = new Intent();
	     //ΪIntent����Action����		
		 intent2.setAction("com.example.umemonew.FIRST_SERVICE");
		 intent2.putExtra("Longitude" , Longitude) ; 
		 intent2.putExtra("Latitude" , Latitude) ;
		 intent2.putExtra("id", intent.getIntExtra("id", 0));
		 //����ָ��Serivce
		 context.startService(intent2); 
		 Toast.makeText(context,"����λ������",Toast.LENGTH_SHORT).show();      
	}  	
}
