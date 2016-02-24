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

public class TimeFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, String>> timelist;	
	private SimpleAdapter adapter;
	private UmemoDatabase db=null;

	private static final int ITEM_RENAME = 1;
	private static final int ITEM_DELAY = 2;
	private static final int ITEM_DELETE = 3;

	/** 
	 * ˫�򻬶��˵����� 
	 */  
	private BidirSlidingLayout bidirSldingLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View timeLayout = inflater.inflate(R.layout.time_layout,
				container, false);
		context=container.getContext();			//get context
		return timeLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʼ�����ݿ�
		db = new UmemoDatabase(context, "umemo.db3", 1);		

		//��ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.timeListView);  
		timelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.time_item,
				new String[]{"date","time","title","note"},
				new int[]{R.id.timeDate,R.id.timeTime,R.id.timeTitle,R.id.timeComment});
		listView.setAdapter(adapter);

		//ע��˵�����
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(listView);


		//���õ������
		listView.setOnItemClickListener(itemListener);

		//���ó����˵�
		registerForContextMenu(listView);


	}

	@Override 
	public void onResume()
	{
		super.onResume();
		//��ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.timeListView);  
		timelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.time_item,
				new String[]{"date","time","title","note"},
				new int[]{R.id.timeDate,R.id.timeTime,R.id.timeTitle,R.id.timeComment});
		listView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();

	}

	private ArrayList<Map<String,String>> getData()
	{
		timelist=db.getTimelistNotFinished();		//get not finished
		return timelist;
	}

	//�����˵�����
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,  
			ContextMenuInfo menuInfo) {
		if (com.example.umemonew.BidirSlidingLayout.slideState == com.example.umemonew.BidirSlidingLayout.DO_NOTHING)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			String timeTitle = ((TextView) info.targetView.findViewById(R.id.timeTitle)).getText().toString();
			if(!timeTitle.isEmpty())
				menu.setHeaderTitle(timeTitle);
			else
				menu.setHeaderTitle(" ");
			menu.add(0, ITEM_RENAME, 0, "������");
			menu.add(0, ITEM_DELAY, 0, "�ӳ�5����");
			menu.add(0, ITEM_DELETE, 2, "ɾ��");
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
						Map<String,String> rename_map=timelist.get(selectedPosition);
						String oldTitle=rename_map.get("title");
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
							//�ı����ݿ�
							int id=db.getNoteId(oldTitle);
							db.changeNoteMessage(id, newTitle);
							rename_map.put("title",newTitle);
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
			case ITEM_DELAY:
				toastShow("�ӳ�5����");
				//TODO    �ӳ�
				break;
			case ITEM_DELETE:  
				// ɾ������  
				Map<String,String> new_map=timelist.get(selectedPosition);		//delete
				String tmp_string=new_map.get("title");
				int id=db.getTimeNoFinishId(tmp_string);
				timelist.remove(selectedPosition);

				Intent intent=new Intent(context ,AlarmReceiver.class);
				int request=id;
				intent.putExtra("request",request);				//put id (to distinguish broadcast)
				PendingIntent pendingIntent=PendingIntent.getBroadcast(context,request,intent,
						PendingIntent.FLAG_UPDATE_CURRENT);  
				AlarmManager am=(AlarmManager)context.getSystemService(Service.ALARM_SERVICE);
				am.cancel(pendingIntent);

				db.deleteTimeHint(id);
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
			TextView timeTitle = (TextView) view.findViewById(R.id.timeTitle);
			Intent intent = new Intent(getActivity(), NewRemindActivity.class);
			String method = "edit_old";
			String type = "time";
			intent.putExtra("method",method);
			intent.putExtra("type",type);
			int timeId = db.getTimeNoFinishId(timeTitle.getText().toString());
			intent.putExtra("id", timeId);
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

