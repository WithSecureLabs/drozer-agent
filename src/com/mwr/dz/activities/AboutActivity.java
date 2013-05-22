package com.mwr.dz.activities;

import com.mwr.dz.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	private TextView description;
	
	private String getVersionName() {
		try {
			return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		}
		catch(NameNotFoundException e) {
			throw new RuntimeException();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		this.description = (TextView)this.findViewById(R.id.about_description);
		this.description.setText(String.format(this.getString(R.string.about_description), this.getVersionName()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
