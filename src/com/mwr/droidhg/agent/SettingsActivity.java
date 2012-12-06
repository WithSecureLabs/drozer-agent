package com.mwr.droidhg.agent;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	private Preference about_preference = null;

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.preferences);
		
		this.about_preference = (Preference)this.findPreference("about_mercury");
		this.about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				
				return true;
			}
			
		});
	}
	
}
