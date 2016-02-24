package com.example.umemonew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsFragment extends Fragment {

	private Context context=null;
	private ListView listView; 
	private ArrayList<Map<String, Object>> friendslist;	
	private SimpleAdapter adapter;

	private UmemoDatabase db=null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View friendsLayout = inflater.inflate(R.layout.friends_layout,
				container, false);
		context=container.getContext();

		//		addFriendsLayout = friendsLayout.findViewById(R.id.add_friends_layout);
		//		addFriendsLayout.setOnClickListener(new OnClickListener() {
		//			   @Override
		//			   public void onClick(View v) {
		//				   addFriends();
		//			   }
		//			  });

		return friendsLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//初始化数据库
		db = new UmemoDatabase(context, "umemo.db3", 1);
		
		listView = (ListView) getActivity().findViewById(R.id.friendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.friends_item,
				new String[]{"friendsName","friendsSign","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsSign,R.id.friendsHead,R.id.friendsUsername});
		//没加入图片，id为friendsHead
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(itemListener);

	}

	private ArrayList<Map<String,Object>> getData()
	{
		ArrayList<Map<String,String>> friendsStringArray = db.getFriendList();
		for(int i = 0;i < friendsStringArray.size();i++)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			//如果没写备注，则为用户名
			if(friendsStringArray.get(i).get("note").isEmpty())
				map.put("friendsName", friendsStringArray.get(i).get("nickname"));
			else
				map.put("friendsName", friendsStringArray.get(i).get("note"));
			map.put("friendsSign", friendsStringArray.get(i).get("signature"));
			//得到application对象
			ApplicationInfo appInfo = getActivity().getApplicationInfo();
			//得到该图片的id(name 是该图片的名字，drawable 是该图片存放的目录，appInfo.packageName是应用程序的包)
			String picId = friendsStringArray.get(i).get("pictureid");
			int resID = getResources().getIdentifier("head"+picId, "raw", appInfo.packageName);
			map.put("friendsHead", resID);
			map.put("friendsUsername", friendsStringArray.get(i).get("username"));
			friendslist.add(map);
		}
		return friendslist;
	}  

	@Override 
	public void onResume()
	{
		super.onResume();
		//初始化适配器
		listView = (ListView) getActivity().findViewById(R.id.friendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.friends_item,
				new String[]{"friendsName","friendsSign","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsSign,R.id.friendsHead,R.id.friendsUsername});
		listView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();

	}
	
	// 条目上单击处理方法.  
	OnItemClickListener itemListener = new OnItemClickListener() {  
		@Override  
		public void onItemClick(AdapterView<?> parent, View view, int position,  
				long id) {
			TextView Username = (TextView) view.findViewById(R.id.friendsUsername);
			Intent intent = new Intent(getActivity(), FriendActivity.class);
			intent.putExtra("username", Username.getText().toString());
			getActivity().startActivity(intent);
		}  
	}; 

	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_SHORT).show();  
	}

}
