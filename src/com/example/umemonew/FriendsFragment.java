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
		
		//��ʼ�����ݿ�
		db = new UmemoDatabase(context, "umemo.db3", 1);
		
		listView = (ListView) getActivity().findViewById(R.id.friendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.friends_item,
				new String[]{"friendsName","friendsSign","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsSign,R.id.friendsHead,R.id.friendsUsername});
		//û����ͼƬ��idΪfriendsHead
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(itemListener);

	}

	private ArrayList<Map<String,Object>> getData()
	{
		ArrayList<Map<String,String>> friendsStringArray = db.getFriendList();
		for(int i = 0;i < friendsStringArray.size();i++)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			//���ûд��ע����Ϊ�û���
			if(friendsStringArray.get(i).get("note").isEmpty())
				map.put("friendsName", friendsStringArray.get(i).get("nickname"));
			else
				map.put("friendsName", friendsStringArray.get(i).get("note"));
			map.put("friendsSign", friendsStringArray.get(i).get("signature"));
			//�õ�application����
			ApplicationInfo appInfo = getActivity().getApplicationInfo();
			//�õ���ͼƬ��id(name �Ǹ�ͼƬ�����֣�drawable �Ǹ�ͼƬ��ŵ�Ŀ¼��appInfo.packageName��Ӧ�ó���İ�)
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
		//��ʼ��������
		listView = (ListView) getActivity().findViewById(R.id.friendsListView);  
		friendslist = new ArrayList<Map<String,Object>>();
		adapter = new SimpleAdapter(getActivity().getBaseContext(),getData(),R.layout.friends_item,
				new String[]{"friendsName","friendsSign","friendsHead","friendsUsername"},
				new int[]{R.id.friendsName,R.id.friendsSign,R.id.friendsHead,R.id.friendsUsername});
		listView.setAdapter(adapter);
		adapter.notifyDataSetInvalidated();

	}
	
	// ��Ŀ�ϵ���������.  
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

	// ��װToast,һ������ü�,��һ���������ʾʱ��ֻҪ�Ĵ�һ���ط�����.
	public void toastShow(String text) {  
		Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_SHORT).show();  
	}

}
