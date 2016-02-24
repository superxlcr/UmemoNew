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

	/**定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，
	 * 若需要将定位点的位置通过百度Android地图 SDK进行地图展示，
	 * 请返回bd09ll，将无偏差的叠加在百度地图上。 
	 */
	private String tempcoor="bd09ll";
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	/**
	 *  设置 点击地图时出现的overlayer
	 */
	private BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding);
	//baidu map
	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	boolean isFirstLoc = true;// 是否首次定位

	/**
	 * 当前的 点击点
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
		 * 监听地图
		 */
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			public void onMapClick(LatLng point) {
				currentPt = point;

				mBaiduMap.clear(); // 清除之前所有的overlayer
				OverlayOptions ooA = new MarkerOptions().position(currentPt).icon(bd)
						.zIndex(9).draggable(true);
				mMarker = (Marker) (mBaiduMap.addOverlay(ooA));
			}
			// 下面的POI暂时木有用到
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
					toastShow("请选择一个位置");
				else if(placeTitle.getText().toString().isEmpty())
					toastShow("您的地点名为空！");
				else
				{
					Intent intent = new Intent();
					String Longitude = Double.toString(currentPt.longitude) ; 
					String Latitude = Double.toString(currentPt.latitude) ;
					intent.putExtra("Longitude" , Longitude) ;
					intent.putExtra("Latitude" , Latitude) ;
					intent.putExtra("placeTitle", placeTitle.getText().toString());
					/*
					 * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，这样就可以在onActivityResult方法中得到Intent对象，
					 */
					setResult(3, intent);
					MapActivity.this.finish();
				}
			}
		});

	}

	private void initView(){

		//标题编辑和确定键
		placeTitle = (EditText) findViewById(R.id.placeName);
		confirm = (Button) findViewById(R.id.confirmButton);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 定位初始化
		mLocClient = new LocationClient(this); 

		mLocClient.registerLocationListener(myListener);  // 注册 监听器

		LocationClientOption option = new LocationClientOption();
		//option.setProdName("@string/app_name") ; // 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setOpenGps(true);			// 打开GPS,这个设置还待商榷。。。  
		option.setLocationMode(tempMode);	//设置定位模式
		option.setCoorType(tempcoor);		//返回的定位结果是百度经纬度，默认值gcj02
		option.setIsNeedAddress(true); 		// 设置为 需要获得地址
		option.setScanSpan(1000); 			//当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。
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
		actionBar.setTitle("地点选择");
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			Log.v("onReceiveLocation" , "on") ; 
			if (location == null || mMapView == null)
				return;		

			// 把经纬度存储在double变量中
			double lon = Double.valueOf(location.getLongitude()) ;
			double lat = Double.valueOf(location.getLatitude()) ;

			nowLoc = new LatLng(lon , lat) ; 

			MyLocationData locData = new MyLocationData.Builder()
			.accuracy(location.getRadius())
			// 此处设置开发者获取到的方向信息，顺时针0-360
			.direction(100).latitude(location.getLatitude())
			.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;

				//				//显示地址
				//				StringBuffer sb = new StringBuffer(256);
				//				sb.append("\n经度 : ");
				//				sb.append(location.getLongitude()); 
				//				sb.append("\n纬度 : ");
				//				sb.append(location.getLatitude());
				//				sb.append("\n地址 : ");
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
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
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