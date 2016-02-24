package com.example.umemonew;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

public class SettingActivity extends Activity {
	
	private Switch vibrationSwitch;
	private Switch ledSwitch;
	private Switch soundSwitch;
	private SeekBar defaultSeekBar;
	private Button saveButton;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private Spinner soundSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("…Ë÷√");
		
		initViews();
		pref = getSharedPreferences("setting_data", MODE_PRIVATE);
		editor = pref.edit();
		
		boolean isVibration = pref.getBoolean("is_vibration", true);
		boolean isLedOn = pref.getBoolean("is_led_on", true);
		boolean isSoundOn = pref.getBoolean("is_sound_on", true);
		int soundLevel = pref.getInt("sound_level", 1);
		int soundType = pref.getInt("sound_type", 0);
		
		vibrationSwitch.setChecked(isVibration);
		ledSwitch.setChecked(isLedOn);
		soundSwitch.setChecked(isSoundOn);
		defaultSeekBar.setProgress(soundLevel);
		
		if (!isSoundOn) {
			defaultSeekBar.setEnabled(false);
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingActivity.this, 
				R.layout.simple_spinner_item, getMusicValues());
		soundSpinner.setAdapter(adapter);
		soundSpinner.setSelection(soundType);
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editor.putBoolean("is_vibration", vibrationSwitch.isChecked());
				editor.putBoolean("is_led_on", ledSwitch.isChecked());
				editor.putBoolean("is_sound_on", soundSwitch.isChecked());
				editor.putInt("sound_level", defaultSeekBar.getProgress());
				editor.putInt("sound_type", soundSpinner.getSelectedItemPosition());
				editor.commit();
				
				SettingActivity.this.finish();
			}
		});
		
		soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					defaultSeekBar.setProgress(1);
					defaultSeekBar.setEnabled(true);
				} else {
					defaultSeekBar.setProgress(0);
					defaultSeekBar.setEnabled(false);
				}
			}
		});
		
		defaultSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (progress == 0) {
					soundSwitch.setChecked(false);
				}
			}
		});
		
	}
	
	private void initViews() {
		vibrationSwitch = (Switch) findViewById(R.id.vibrationCheck);
		ledSwitch = (Switch) findViewById(R.id.ledCheck);
		soundSwitch = (Switch) findViewById(R.id.soundCheck);
		defaultSeekBar = (SeekBar) findViewById(R.id.default_seekbar);
		saveButton = (Button) findViewById(R.id.check_setting);
		soundSpinner = (Spinner) findViewById(R.id.sound_spinner);
	}
	
	public static List<String> getMusicValues() {
		try {
			Field[] musicFields = R.raw.class.getFields();
			List<String> musicValues = new ArrayList<String>();
			for (Field field : musicFields) {
				if (field.getName().indexOf("m_") == 0)
					musicValues.add(field.getName().substring(2));
			}
			return musicValues;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.none, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home)
			SettingActivity.this.finish();
		return super.onOptionsItemSelected(item);
	}
	
}
