package com.example.umemonew;

import java.util.ArrayList;
import java.util.Map;

import com.example.umemonew.BidirSlidingLayout;
import com.example.umemonew.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
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

public class NoteFragment extends Fragment {
	private Context context=null;

	private ListView listView; 
	private ArrayList<Map<String, String>> notelist;	
	private SimpleAdapter adapter;

	private static final int ITEM_RENAME = 1;  
	private static final int ITEM_DELETE = 2;

	private UmemoDatabase db;	

	/** 
     * ˫�򻬶��˵����� 
     */  
    private BidirSlidingLayout bidirSldingLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View noteLayout = inflater.inflate(R.layout.note_layout,
				container, false);
		context=container.getContext();			//get context
		return noteLayout;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʼ�����ݿ�
		db = new UmemoDatabase(context, "umemo.db3", 1);		

		//ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.noteListView);  
		notelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.note_item,
				new String[]{"title","note"},
				new int[]{R.id.noteTitle,R.id.noteComment});
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
		listView = (ListView) getActivity().findViewById(R.id.noteListView);  
		notelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.note_item,
				new String[]{"title","note"},
				new int[]{R.id.noteTitle,R.id.noteComment});
		listView.setAdapter(adapter);
	}

	private ArrayList<Map<String,String>> getData()
	{
		notelist=db.getNotelistNotFinished();
		return notelist;
	} 

	//�����˵�����
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,  
			ContextMenuInfo menuInfo) {
		if (com.example.umemonew.BidirSlidingLayout.slideState == com.example.umemonew.BidirSlidingLayout.DO_NOTHING)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			String noteTitle = ((TextView) info.targetView.findViewById(R.id.noteTitle)).getText().toString();
			if(!noteTitle.isEmpty())
				menu.setHeaderTitle(noteTitle);
			else
				menu.setHeaderTitle(" ");
			menu.add(0, ITEM_RENAME, 0, "������");  
			menu.add(0, ITEM_DELETE, 1, "ɾ��");
		}
	} 

	// ��Ӧ������������ɾ���¼�����  
	public boolean onContextItemSelected(MenuItem item) {  
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  
		// info.targetView�õ�list.xml�е�LinearLayout����.  
		//String stuId = ((TextView) info.targetView.findViewById(R.id.noteTitle)).getText().toString();
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
						Map<String,String> rename_map=notelist.get(selectedPosition);
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
			case ITEM_DELETE:  
				// ɾ������  
				Map<String,String> new_map=notelist.get(selectedPosition);		//delete
				String tmp_string=new_map.get("title");
				int id=db.getNoteId(tmp_string);
				notelist.remove(selectedPosition);
				db.deleteNote(id);
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
			TextView noteTitle = (TextView) view.findViewById(R.id.noteTitle);
			Intent intent = new Intent(getActivity(), NewRemindActivity.class);
			String method = "edit_old";
			String type = "note";
			intent.putExtra("method",method);
			intent.putExtra("type",type);
			int noteId = db.getNoteId(noteTitle.getText().toString());
			intent.putExtra("id", noteId);
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

