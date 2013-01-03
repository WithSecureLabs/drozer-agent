package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.api.Endpoint;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	
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
						bundle.getString(("endpoint:name")),
						bundle.getString("endpoint:host"),
						bundle.getInt("endpoint:port"),
						bundle.getBoolean("endpoint:ssl"),
						bundle.getString("endpoint:ssl_truststore_path"),
						bundle.getString("endpoint:ssl_truststore_password"),
						bundle.getString("endpoint:password"));
				
				if(Agent.getEndpointManager().add(endpoint)) {
					this.endpoint_preferences.addPreference(this.createPreferenceFrom(endpoint));
					
					Toast.makeText(this.getApplicationContext(), "Created new Endpoint", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(this.getApplicationContext(), "Error creating Endpoint", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case SettingsActivity.EDIT_ENDPOINT:
				if(bundle.containsKey("endpoint:deleted")) {
					Toast.makeText(this.getApplicationContext(), "Removed Endpoint", Toast.LENGTH_SHORT).show();
					Preference preference = this.endpoint_preferences.findPreference("endpoint_" + bundle.getInt("endpoint:id"));
					
					preference.setEnabled(false);
				}
				else {
					endpoint = new Endpoint(
							bundle.getInt("endpoint:id"),
							bundle.getString(("endpoint:name")),
							bundle.getString("endpoint:host"),
							bundle.getInt("endpoint:port"),
							bundle.getBoolean("endpoint:ssl"),
							bundle.getString("endpoint:ssl_truststore_path"),
							bundle.getString("endpoint:ssl_truststore_password"),
							bundle.getString("endpoint:password"));
					
					if(Agent.getEndpointManager().update(endpoint)) {
						Preference preference = this.endpoint_preferences.findPreference("endpoint_" + endpoint.getId());
						preference.setTitle(endpoint.getName());
						preference.setSummary(endpoint.toConnectionString());
						
						Toast.makeText(this.getApplicationContext(), "Updated " + endpoint.getName(), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(this.getApplicationContext(), "There was a problem whilst updating " + endpoint.getName(), Toast.LENGTH_SHORT).show();
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
		
		this.about_preference = (Preference)this.findPreference("about_mercury");
		this.about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
				
				return true;
			}
			
		});
		
		this.endpoint_preferences = (PreferenceCategory)this.getPreferenceManager().findPreference("endpoint_settings");
		
		this.endpoint_preferences.addPreference(this.createNewEndpointPreference());
		
		for(Endpoint endpoint : Agent.getEndpointManager().all()) {
			
			
			this.endpoint_preferences.addPreference(this.createPreferenceFrom(endpoint));
		}
	}
	
	private Preference createNewEndpointPreference() {
		Preference preference = new Preference(this);
		preference.setKey("endpoint_new");
		preference.setTitle(R.string.endpoint_new);
		preference.setSummary("");
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
				bundle.putInt("endpoint:id", Integer.parseInt(preference.getKey().split("_")[1]));
				
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
