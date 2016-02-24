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
 * 相遇提醒界面碎片
 */
public class MeetingFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, String>> meetinglist;	
	private SimpleAdapter adapter;

	private static final int ITEM_RENAME = 1;  
	private static final int ITEM_DELETE = 2;

	/** 
     * 双向滑动菜单布局 
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
		//初始化适配器
		listView = (ListView) getActivity().findViewById(R.id.meetingListView);  
		meetinglist = new ArrayList<Map<String,String>>();
		adapter = new SimpleAdapter(context,getData(),R.layout.meeting_item,
				new String[]{"meetingTitle","meetingComment","meetingPlace","meetingTime"},
				new int[]{R.id.meetingTitle,R.id.meetingComment,R.id.meetingPlace,R.id.meetingTime});
		listView.setAdapter(adapter);

		//注册菜单滑动
		bidirSldingLayout = (BidirSlidingLayout) getActivity().findViewById(R.id.bidir_sliding_layout);
		bidirSldingLayout.setScrollEvent(listView);

		//设置点击监听
		listView.setOnItemClickListener(itemListener);

		//注册长按菜单
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
			new_map.put("meetingPlace","地点"+i);
			new_map.put("meetingTime","11:12： "+i+" 生效");
			meetinglist.add(new_map);
		}
		return meetinglist;
	}

	//长按菜单设置
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
						Map<String,String> rename_map=meetinglist.get(selectedPosition);
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
							//TODO
							rename_map.put("meetingTitle",newTitle);
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

	// 条目上单击处理方法.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {  
			TextView meetingTitle = (TextView) view.findViewById(R.id.meetingTitle);
			toastShow("你点击了 :"+meetingTitle.getText().toString());
			// TODO 编辑界面！
		}  
	}; 

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, 1000).show();  
	}
	//TODO
}
