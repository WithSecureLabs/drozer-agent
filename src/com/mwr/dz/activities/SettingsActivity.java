package com.mwr.dz.activities;

import java.util.Observable;
import java.util.Observer;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	
	public static final String ABOUT_DROZER_PREFERENCE = "about_drozer";
	public static final String ENDPOINT_SETTINGS_PREFERENCE = "endpoint_settings";
	
	public static final int NEW_ENDPOINT = 1;
	public static final int EDIT_ENDPOINT = 2;
	
	private Preference about_preference = null;
	private PreferenceCategory endpoint_preferences = null;

	@Override
	public void onActivityResult(int request_code, int result_code, Intent data) {
		if(result_code == RESULT_OK) {
			Bundle bundle = data.getExtras();
			Endpoint endpoint;
			
			switch(request_code) {
			case SettingsActivity.NEW_ENDPOINT:
				endpoint = new Endpoint(
						bundle.getString(Endpoint.ENDPOINT_NAME),
						bundle.getString(Endpoint.ENDPOINT_HOST),
						bundle.getInt(Endpoint.ENDPOINT_PORT),
						bundle.getBoolean(Endpoint.ENDPOINT_SSL),
						bundle.getString(Endpoint.ENDPOINT_TRUSTSTORE_PATH),
						bundle.getString(Endpoint.ENDPOINT_TRUSTSTORE_PASSWORD),
						bundle.getString(Endpoint.ENDPOINT_PASSWORD));
				
				if(Agent.getInstance().getEndpointManager().add(endpoint)) {
					this.endpoint_preferences.addPreference(this.createPreferenceFrom(endpoint));
					
					Toast.makeText(this.getApplicationContext(), this.getString(R.string.endpoint_created), Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(this.getApplicationContext(), this.getString(R.string.endpoint_create_error), Toast.LENGTH_SHORT).show();
				}
				break;
				
			case SettingsActivity.EDIT_ENDPOINT:
				if(bundle.containsKey(Endpoint.ENDPOINT_DELETED)) {
					Toast.makeText(this.getApplicationContext(), this.getString(R.string.endpoint_removed), Toast.LENGTH_SHORT).show();
					Preference preference = this.endpoint_preferences.findPreference("endpoint_" + bundle.getInt(Endpoint.ENDPOINT_ID));
					
					preference.setEnabled(false);
				}
				else {
					endpoint = new Endpoint(
							bundle.getInt(Endpoint.ENDPOINT_ID),
							bundle.getString(Endpoint.ENDPOINT_NAME),
							bundle.getString(Endpoint.ENDPOINT_HOST),
							bundle.getInt(Endpoint.ENDPOINT_PORT),
							bundle.getBoolean(Endpoint.ENDPOINT_SSL),
							bundle.getString(Endpoint.ENDPOINT_TRUSTSTORE_PATH),
							bundle.getString(Endpoint.ENDPOINT_TRUSTSTORE_PASSWORD),
							bundle.getString(Endpoint.ENDPOINT_PASSWORD),
							false);
					
					if(Agent.getInstance().getEndpointManager().update(endpoint)) {
						Preference preference = this.endpoint_preferences.findPreference("endpoint_" + endpoint.getId());
						preference.setTitle(endpoint.getName());
						preference.setSummary(endpoint.toConnectionString());
						
						Toast.makeText(this.getApplicationContext(), this.getString(R.string.endpoint_updated), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(this.getApplicationContext(), this.getString(R.string.endpoint_update_error), Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.preferences);
		
		this.about_preference = (Preference)this.findPreference(ABOUT_DROZER_PREFERENCE);
		this.about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				
				return true;
			}
			
		});
		
		this.endpoint_preferences = (PreferenceCategory)this.getPreferenceManager().findPreference(ENDPOINT_SETTINGS_PREFERENCE);
		
		this.endpoint_preferences.addPreference(this.createNewEndpointPreference());
		
		for(Endpoint endpoint : Agent.getInstance().getEndpointManager().all())
			this.endpoint_preferences.addPreference(this.createPreferenceFrom(endpoint));
	}
	
	private Preference createNewEndpointPreference() {
		Preference preference = new Preference(this);
		preference.setKey("endpoint_new");
		preference.setTitle(R.string.endpoint_new);
		preference.setOrder(1000);
		
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				SettingsActivity.this.startActivityForResult(new Intent(SettingsActivity.this, EndpointSettingsActivity.class), SettingsActivity.NEW_ENDPOINT);
				
				return true;
			}
			
		});
		
		return preference;
	}
	
	private Preference createPreferenceFrom(Endpoint endpoint) {
		Preference preference = new Preference(this);
		preference.setKey("endpoint_" + endpoint.getId());
		preference.setTitle(endpoint.getName());
		preference.setSummary(endpoint.toConnectionString());
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle bundle = new Bundle();
				bundle.putInt(Endpoint.ENDPOINT_ID, Integer.parseInt(preference.getKey().split("_")[1]));
				
				Intent intent = new Intent(SettingsActivity.this, EndpointSettingsActivity.class);
				intent.putExtras(bundle);
				
				SettingsActivity.this.startActivityForResult(intent, SettingsActivity.EDIT_ENDPOINT);
				
				return true;
			}
			
		});
		endpoint.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				Endpoint endpoint = (Endpoint)observable;
				
				Preference pref = SettingsActivity.this.endpoint_preferences.findPreference("endpoint_" + endpoint.getId());
				pref.setTitle(endpoint.getName());
				pref.setSummary(endpoint.toConnectionString());
			}
			
		});
		
		return preference;
	}
	
}
