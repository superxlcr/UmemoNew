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
 * �ص������б������Ƭ
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
	 * ˫�򻬶��˵����� 
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
		//��ʼ�����ݿ�
		db = new UmemoDatabase(context, "umemo.db3", 1);
		//��ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.placeListView);  
		placelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.place_item,
				new String[]{"title","note","place","begindate","begintime"},
				new int[]{R.id.placeTitle,R.id.placeComment,R.id.placePlace,R.id.placeDate,R.id.placeTime});
		listView.setAdapter(adapter);

		//ע��˵�����
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(listView);

		//���õ������
		listView.setOnItemClickListener(itemListener);

		//ע�᳤���˵�
		registerForContextMenu(listView);	
	}

	@Override 
	public void onResume()
	{
		super.onResume();
		//��ʼ��������
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

	//�����˵�����
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
			menu.add(0, ITEM_RENAME, 0, "������");  
			menu.add(0, ITEM_DELETE, 1, "ɾ��");
		}
	} 

	// ��Ӧ������������ɾ���¼�����  
	public boolean onContextItemSelected(MenuItem item) {  
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		final int selectedPosition = info.position;

		//����������������
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		final View renameView = layoutInflater.inflate(R.layout.rename_alert_dialog, null);
		Dialog renameAlertDialog = new AlertDialog.Builder(context).
				setTitle("������"). 
				setView(renameView).
				setPositiveButton("ȡ��", new DialogInterface.OnClickListener() { 
					@Override 
					public void onClick(DialogInterface dialog, int which) {  
					} 
				}).
				setNeutralButton("ȷ��", new DialogInterface.OnClickListener() { 
					@Override 
					public void onClick(DialogInterface dialog, int which) {
						EditText renameText=(EditText) renameView.findViewById(R.id.rename_text);
						String newTitle=renameText.getText().toString();
						Map<String,String> rename_map=placelist.get(selectedPosition);
						String oldTitle = rename_map.get("title");
						String oldPlace = rename_map.get("place");
						if(newTitle.isEmpty())
						{
							//��������ֹΪ��ֵ
							Dialog emptyRenameAlertDialog  = new AlertDialog.Builder(context).   
									setTitle("������Ϊ�գ�").   
									setMessage("��������ֹΪ��").   
									setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {   
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
							toastShow("�������ɹ���");
						}

					} 
				}). 
				create();
		if (true) {
			switch (item.getItemId()) {  
			case ITEM_RENAME:  
				// ������
				renameAlertDialog.show();
				break;  
			case ITEM_DELETE:  
				// ɾ������ 
				Map<String,String> new_map=placelist.get(selectedPosition);		//delete
				String tmp_title=new_map.get("title");
				String tmp_place=new_map.get("place");
				int id=db.getPlaceId(tmp_title, tmp_place);
				db.deletePlaceHint(id);
				placelist.remove(selectedPosition);

				Intent intentService = new Intent(context , PlaceService.class);
				Intent intentBroadCast = new Intent(context , PlaceReceiver.class);
				//ΪIntent����Action����		
				intentService.setAction("com.example.umemonew.FIRST_SERVICE");
				intentBroadCast.setAction("com.example.umemonew.TIME_ALARM");

				//ȡ������
				PendingIntent operation = PendingIntent
						.getBroadcast(context, -id, intentBroadCast , PendingIntent.FLAG_UPDATE_CURRENT);// ����һ���㲥��PendingIntentΪIntent�İ�װ		 				 			
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

	// ��Ŀ�ϵ���������.  
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

	// ��װToast,һ������ü�,��һ���������ʾʱ��ֻҪ�Ĵ�һ���ط�����.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, 1000).show();  
	}

	//���ݿ�
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}
}
