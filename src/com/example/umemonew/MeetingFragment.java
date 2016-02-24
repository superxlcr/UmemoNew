package com.example.umemonew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/***
 * 
 * @author Superxlcr
 * �������ѽ�����Ƭ
 */
public class MeetingFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, String>> meetinglist;	
	private SimpleAdapter adapter;

	private static final int ITEM_RENAME = 1;  
	private static final int ITEM_DELETE = 2;

	/** 
     * ˫�򻬶��˵����� 
     */  
    private BidirSlidingLayout bidirSldingLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View meetingLayout = inflater.inflate(R.layout.meeting_layout,
				container, false);
		context=container.getContext();			//get context
		return meetingLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.meetingListView);  
		meetinglist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.meeting_item,
				new String[]{"meetingTitle","meetingComment","meetingPlace","meetingTime"},
				new int[]{R.id.meetingTitle,R.id.meetingComment,R.id.meetingPlace,R.id.meetingTime});
		listView.setAdapter(adapter);

		//ע��˵�����
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(listView);

		//���õ������
		listView.setOnItemClickListener(itemListener);

		//ע�᳤���˵�
		registerForContextMenu(listView);	
	}

	private ArrayList<Map<String,String>> getData()
	{
		//TODO
		for(int i=10;i<20;i++)
		{
			Map<String,String> new_map=new HashMap<String, String>(); 
			new_map.put("meetingTitle","This is meeting title "+i);
			new_map.put("meetingComment","meeting comment: "+i);
			new_map.put("meetingPlace","�ص�"+i);
			new_map.put("meetingTime","11:12�� "+i+" ��Ч");
			meetinglist.add(new_map);
		}
		return meetinglist;
	}

	//�����˵�����
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,  
			ContextMenuInfo menuInfo) {
		if (com.example.umemonew.BidirSlidingLayout.slideState == com.example.umemonew.BidirSlidingLayout.DO_NOTHING)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			String meetingTitle = ((TextView) info.targetView.findViewById(R.id.meetingTitle)).getText().toString();
			if(!meetingTitle.isEmpty())
				menu.setHeaderTitle(meetingTitle);
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
						Map<String,String> rename_map=meetinglist.get(selectedPosition);
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
							//TODO
							rename_map.put("meetingTitle",newTitle);
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
				//TODO
				meetinglist.remove(selectedPosition);
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
			TextView meetingTitle = (TextView) view.findViewById(R.id.meetingTitle);
			toastShow("������ :"+meetingTitle.getText().toString());
			// TODO �༭���棡
		}  
	}; 

	// ��װToast,һ������ü�,��һ���������ʾʱ��ֻҪ�Ĵ�һ���ط�����.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, 1000).show();  
	}
	//TODO
}
