package com.example.umemonew;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/***
 * 
 * @author Superxlcr
 * ѡ��ͷ�����
 */
public class ChooseHeadActivity extends Activity {

	private LayoutInflater inflater;
	private int totalNumber = 40;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_head);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("��ѡ������ͷ��");
		inflater = getLayoutInflater();

		// ������ͷ����Ŀ��̬���view
		// ȡ�ô�ֱlayout
		LinearLayout vl = (LinearLayout) findViewById(R.id.chooseHeadLayoutVertical);
		for (int i = 0; i < totalNumber; i += 4)
			addHeadHorizontalView(vl, i);
	}

	private void addHeadHorizontalView(LinearLayout vl, int start) {
		// ȡ�ô�ֱlayout
		View hlView = inflater.inflate(R.layout.choose_head_horizontal, null);
		LinearLayout hl = (LinearLayout) hlView
				.findViewById(R.id.chooseHeadLayoutHorizontal);
		for (int i = 0; i < 4; i++)
			addHeadSingleView(hl, start + i + 1);
		vl.addView(hl);
	}

	void addHeadSingleView(LinearLayout hl, int id) {
		ImageView iv = new ImageView(ChooseHeadActivity.this);
		iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		// �������Ϊ���ȵ��ķ�֮һ
		WindowManager wm = this.getWindowManager();
		int height = wm.getDefaultDisplay().getWidth() / 4;
		iv.setLayoutParams(new LinearLayout.LayoutParams(0, height, 1));
		iv.setPadding(5, 5, 5, 5);
		String temp = Integer.toString(id);
		if (id < 10)
			temp = "0" + temp;
		final String passId = temp;
		if (id < totalNumber + 1) {
			// �õ�application����
			ApplicationInfo appInfo = getApplicationInfo();
			// �õ���ͼƬ��id(name �Ǹ�ͼƬ�����֣�drawable
			// �Ǹ�ͼƬ��ŵ�Ŀ¼��appInfo.packageName��Ӧ�ó���İ�)
			String picId = Integer.toString(id);
			if (id < 10)
				picId = "0" + picId;
			int resID = getResources().getIdentifier("head" + picId, "raw",
					appInfo.packageName);
			iv.setImageResource(resID);
			// �󶨵������
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("headId", passId);
					/*
					 * ����setResult������ʾ�ҽ�Intent���󷵻ظ�֮ǰ���Ǹ�Activity��
					 * �����Ϳ�����onActivityResult�����еõ�Intent����
					 */
					setResult(1, intent);
					ChooseHeadActivity.this.finish();
				}
			});
		}
		hl.addView(iv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.none, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home)
			ChooseHeadActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}

	public void toastShow(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
