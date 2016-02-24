package com.example.umemonew;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class MapActivity extends Activity
{	
	private EditText placeTitle;
	private Button confirm;

	public LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();	

	/**��λSDK���Է���bd09��bd09ll��gcj02�����������꣬
	 * ����Ҫ����λ���λ��ͨ���ٶ�Android��ͼ SDK���е�ͼչʾ��
	 * �뷵��bd09ll������ƫ��ĵ����ڰٶȵ�ͼ�ϡ� 
	 */
	private String tempcoor="bd09ll";
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	/**
	 *  ���� �����ͼʱ���ֵ�overlayer
	 */
	private BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);
	//baidu map
	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI���
	boolean isFirstLoc = true;// �Ƿ��״ζ�λ

	/**
	 * ��ǰ�� �����
	 */
	private LatLng nowLoc = null  ;
	private LatLng currentPt = null ;

	private Marker mMarker;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		initView();
		actionBarInitial();

		mLocClient.start();	

		/**
		 * ������ͼ
		 */
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			public void onMapClick(LatLng point) {
				currentPt = point;

				mBaiduMap.clear(); // ���֮ǰ���е�overlayer
				OverlayOptions ooA = new MarkerOptions().position(currentPt).icon(bd)
						.zIndex(9).draggable(true);
				mMarker = (Marker) (mBaiduMap.addOverlay(ooA));
			}
			// �����POI��ʱľ���õ�
			public boolean onMapPoiClick(MapPoi poi) {
				return false;
			}  
		});

		confirm.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if(currentPt == null)
					toastShow("��ѡ��һ��λ��");
				else if(placeTitle.getText().toString().isEmpty())
					toastShow("���ĵص���Ϊ�գ�");
				else
				{
					Intent intent = new Intent();
					String Longitude = Double.toString(currentPt.longitude) ; 
					String Latitude = Double.toString(currentPt.latitude) ;
					intent.putExtra("Longitude" , Longitude) ;
					intent.putExtra("Latitude" , Latitude) ;
					intent.putExtra("placeTitle", placeTitle.getText().toString());
					/*
					 * ����setResult������ʾ�ҽ�Intent���󷵻ظ�֮ǰ���Ǹ�Activity�������Ϳ�����onActivityResult�����еõ�Intent����
					 */
					setResult(3, intent);
					MapActivity.this.finish();
				}
			}
		});

	}

	private void initView(){

		//����༭��ȷ����
		placeTitle = (EditText) findViewById(R.id.placeName);
		confirm = (Button) findViewById(R.id.confirmButton);

		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);

		// ��λ��ʼ��
		mLocClient = new LocationClient(this); 

		mLocClient.registerLocationListener(myListener);  // ע�� ������

		LocationClientOption option = new LocationClientOption();
		//option.setProdName("@string/app_name") ; // ���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
		option.setOpenGps(true);			// ��GPS,������û�����ȶ������  
		option.setLocationMode(tempMode);	//���ö�λģʽ
		option.setCoorType(tempcoor);		//���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		option.setIsNeedAddress(true); 		// ����Ϊ ��Ҫ��õ�ַ
		option.setScanSpan(1000); 			//���������������������ֵС��1000��ms��ʱ������һ�ζ�λģʽ��
		mLocClient.setLocOption(option);
	}

	public void toastShow(String text) {  
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();  
	}

	private void actionBarInitial()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("�ص�ѡ��");
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			Log.v("onReceiveLocation" , "on") ; 
			if (location == null || mMapView == null)
				return;		

			// �Ѿ�γ�ȴ洢��double������
			double lon = Double.valueOf(location.getLongitude()) ;
			double lat = Double.valueOf(location.getLatitude()) ;

			nowLoc = new LatLng(lon , lat) ; 

			MyLocationData locData = new MyLocationData.Builder()
			.accuracy(location.getRadius())
			// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
			.direction(100).latitude(location.getLatitude())
			.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;

				//				//��ʾ��ַ
				//				StringBuffer sb = new StringBuffer(256);
				//				sb.append("\n���� : ");
				//				sb.append(location.getLongitude()); 
				//				sb.append("\nγ�� : ");
				//				sb.append(location.getLatitude());
				//				sb.append("\n��ַ : ");
				//				sb.append(location.getAddrStr());
				//				textView.setText(sb);
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
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
			MapActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

}