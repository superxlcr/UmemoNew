package com.example.umemonew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/***
 * 
 * @author Superxlcr
 * Ⱥ���б���Ƭ
 */
public class GroupsFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, Object>> groupslist;	
	private SimpleAdapter adapter;

	private Button newGroupsBtn;

	private UmemoDatabase db=null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View groupsLayout = inflater.inflate(R.layout.groups_layout,
				container, false);
		context=container.getContext();
		return groupsLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//��ʼ�����ݿ�
		db = new UmemoDatabase(context, "umemo.db3", 1);

		newGroupsBtn = (Button) getActivity().findViewById(R.id.new_groups);
		listView = (ListView) getActivity().findViewById(R.id.groupsListView);  
		groupslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.groups_item,
				new String[]{"groupsName","groupsHead"},
				new int[]{R.id.groupsName,R.id.groupsHead});
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(itemListener);

		newGroupsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity() , NewGroupsActivity.class);
				getActivity().startActivity(intent);
			}
		});

	}

	private ArrayList<Map<String,Object>> getData()
	{
		Map<String , String> groupsMap = db.getAllGroups();
		Iterator<String> iter = groupsMap.keySet().iterator();
		String groupName;
		while (iter.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			groupName = iter.next();
			map.put("groupsName" , groupName);
			//�õ�application����
			ApplicationInfo appInfo = getActivity().getApplicationInfo();
			//�õ���ͼƬ��id(name �Ǹ�ͼƬ�����֣�drawable �Ǹ�ͼƬ��ŵ�Ŀ¼��appInfo.packageName��Ӧ�ó���İ�)
			String picId = groupsMap.get(groupName);
			int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
			map.put("groupsHead", resID);
			groupslist.add(map);
		}
		return groupslist;
	}

	@Override 
	public void onResume()
	{
		super.onResume();
		//��ʼ��������
		newGroupsBtn = (Button) getActivity().findViewById(R.id.new_groups);
		listView = (ListView) getActivity().findViewById(R.id.groupsListView);  
		groupslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.groups_item,
				new String[]{"groupsName","groupsHead"},
				new int[]{R.id.groupsName,R.id.groupsHead});
		listView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();
	}
	
	// ��Ŀ�ϵ���������.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {  
			TextView groupsName = (TextView) view.findViewById(R.id.groupsName);
			Intent intent = new Intent(getActivity() , ManageGroupsActivity.class);
			intent.putExtra("groupsName", groupsName.getText().toString());
			getActivity().startActivity(intent);
		}  
	}; 

	// ��װToast,һ������ü�,��һ���������ʾʱ��ֻҪ�Ĵ�һ���ط�����.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_SHORT).show();  
	}

}
