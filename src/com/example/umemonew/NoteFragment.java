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
     * 双向滑动菜单布局 
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
		//初始化数据库
		db = new UmemoDatabase(context, "umemo.db3", 1);		

		//始化适配器
		listView = (ListView) getActivity().findViewById(R.id.noteListView);  
		notelist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.note_item,
				new String[]{"title","note"},
				new int[]{R.id.noteTitle,R.id.noteComment});
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

	//长按菜单设置
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
			menu.add(0, ITEM_RENAME, 0, "重命名");  
			menu.add(0, ITEM_DELETE, 1, "删除");
		}
	} 

	// 响应长按重命名和删除事件处理  
	public boolean onContextItemSelected(MenuItem item) {  
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  
		// info.targetView得到list.xml中的LinearLayout对象.  
		//String stuId = ((TextView) info.targetView.findViewById(R.id.noteTitle)).getText().toString();
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
						Map<String,String> rename_map=notelist.get(selectedPosition);
						String oldTitle=rename_map.get("title");
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
							//改变数据库
							int id=db.getNoteId(oldTitle);
							db.changeNoteMessage(id, newTitle);
							rename_map.put("title",newTitle);
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

	// 条目上单击处理方法.  
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

