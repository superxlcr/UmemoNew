package com.example.umemonew;

import java.util.ArrayList;
import java.util.Map;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/***
 * 
 * @author Superxlcr
 * 地点提醒列表界面碎片
 */
public class PlaceFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, String>> placelist;	
	private SimpleAdapter adapter;
	private UmemoDatabase db=null;

	private static final int ITEM_RENAME = 1;  
	private static final int ITEM_DELETE = 2;

	/** 
	 * 双向滑动菜单布局 
	 */  
	private BidirSlidingLayout bidirSldingLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View placeLayout = inflater.inflate(R.layout.place_layout,
				container, false);
		context=container.getContext();			//get context
		return placeLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化数据库
		db = new UmemoDatabase(context, "umemo.db3", 1);
		//初始化适配器
		listView = (ListView) getActivity().findViewById(R.id.placeListView);  
		placelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.place_item,
				new String[]{"title","note","place","begindate","begintime"},
				new int[]{R.id.placeTitle,R.id.placeComment,R.id.placePlace,R.id.placeDate,R.id.placeTime});
		listView.setAdapter(adapter);

		//注册菜单滑动
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(listView);

		//设置点击监听
		listView.setOnItemClickListener(itemListener);

		//注册长按菜单
		registerForContextMenu(listView);	
	}

	@Override 
	public void onResume()
	{
		super.onResume();
		//初始化适配器
		listView = (ListView) getActivity().findViewById(R.id.placeListView);  
		placelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.place_item,
				new String[]{"title","note","place","begindate","begintime"},
				new int[]{R.id.placeTitle,R.id.placeComment,R.id.placePlace,R.id.placeDate,R.id.placeTime});
		listView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();

	}

	private ArrayList<Map<String,String>> getData()
	{
		placelist = db.getPlacelistNotFinished();
		return placelist;
	}

	//长按菜单设置
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,  
			ContextMenuInfo menuInfo) {
		if (com.example.umemonew.BidirSlidingLayout.slideState == com.example.umemonew.BidirSlidingLayout.DO_NOTHING)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			String placeTitle = ((TextView) info.targetView.findViewById(R.id.placeTitle)).getText().toString();
			if(!placeTitle.isEmpty())
				menu.setHeaderTitle(placeTitle);
			else
				menu.setHeaderTitle(" ");
			menu.add(0, ITEM_RENAME, 0, "重命名");  
			menu.add(0, ITEM_DELETE, 1, "删除");
		}
	} 

	// 响应长按重命名和删除事件处理  
	public boolean onContextItemSelected(MenuItem item) {  
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		final int selectedPosition = info.position;

		//设置重命名弹出框
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		final View renameView = layoutInflater.inflate(R.layout.rename_alert_dialog, null);
		Dialog renameAlertDialog = new AlertDialog.Builder(context).
				setTitle("重命名"). 
				setView(renameView).
				setPositiveButton("取消", new DialogInterface.OnClickListener() { 
					@Override 
					public void onClick(DialogInterface dialog, int which) {  
					} 
				}).
				setNeutralButton("确定", new DialogInterface.OnClickListener() { 
					@Override 
					public void onClick(DialogInterface dialog, int which) {
						EditText renameText=(EditText) renameView.findViewById(R.id.rename_text);
						String newTitle=renameText.getText().toString();
						Map<String,String> rename_map=placelist.get(selectedPosition);
						String oldTitle = rename_map.get("title");
						String oldPlace = rename_map.get("place");
						if(newTitle.isEmpty())
						{
							//重命名禁止为空值
							Dialog emptyRenameAlertDialog  = new AlertDialog.Builder(context).   
									setTitle("重命名为空！").   
									setMessage("重命名禁止为空").   
									setPositiveButton("确定", new DialogInterface.OnClickListener() {   
										@Override   
										public void onClick(DialogInterface dialog, int which) {   

										}   
									}).      
									create();   
							emptyRenameAlertDialog.show();
						}
						else
						{
							int id = db.getPlaceId(oldTitle, oldPlace);
							db.changePlaceMessage(id, newTitle);
							rename_map.put("placeTitle",newTitle);
							adapter.notifyDataSetInvalidated();
							toastShow("重命名成功！");
						}

					} 
				}). 
				create();
		if (true) {
			switch (item.getItemId()) {  
			case ITEM_RENAME:  
				// 重命名
				renameAlertDialog.show();
				break;  
			case ITEM_DELETE:  
				// 删除数据 
				Map<String,String> new_map=placelist.get(selectedPosition);		//delete
				String tmp_title=new_map.get("title");
				String tmp_place=new_map.get("place");
				int id=db.getPlaceId(tmp_title, tmp_place);
				db.deletePlaceHint(id);
				placelist.remove(selectedPosition);

				Intent intentService = new Intent(context , PlaceService.class);
				Intent intentBroadCast = new Intent(context , PlaceReceiver.class);
				//为Intent设置Action属性		
				intentService.setAction("com.example.umemonew.FIRST_SERVICE");
				intentBroadCast.setAction("com.example.umemonew.TIME_ALARM");

				//取消提醒
				PendingIntent operation = PendingIntent
						.getBroadcast(context, -id, intentBroadCast , PendingIntent.FLAG_UPDATE_CURRENT);// 启动一个广播，PendingIntent为Intent的包装		 				 			
				context.stopService(intentService) ; 
				AlarmManager am=(AlarmManager)context.getSystemService(Service.ALARM_SERVICE);
				am.cancel(operation);

				adapter.notifyDataSetChanged();  		//update
				break;  
			default:  
				break;

			}
		}   
		return false;  
	}

	// 条目上单击处理方法.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {  
			TextView placeTitle = (TextView) view.findViewById(R.id.placeTitle);
			Intent intent = new Intent(getActivity(), NewRemindActivity.class);
			String method = "edit_old";
			String type = "place";
			intent.putExtra("method",method);
			intent.putExtra("type",type);
			int placeId = db.getPlaceIdNotFinished(placeTitle.getText().toString());
			intent.putExtra("id", placeId);
			getActivity().startActivity(intent);
		}  
	}; 

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, 1000).show();  
	}

	//数据库
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
