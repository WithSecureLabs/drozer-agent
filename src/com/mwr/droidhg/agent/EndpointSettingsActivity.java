package com.mwr.droidhg.agent;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.api.Endpoint;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

public class EndpointSettingsActivity extends PreferenceActivity {
	
	private Endpoint endpoint;

	private EditTextPreference endpoint_host;
	private EditTextPreference endpoint_name;
	private EditTextPreference endpoint_port;
	private CheckBoxPreference endpoint_ssl;

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		
		if(bundle == null)
			this.endpoint = new Endpoint();
		else {
			int endpoint_id = bundle.getInt("endpoint:id");
			if(endpoint_id > 0) {
				this.endpoint = Agent.getEndpointManager().get(endpoint_id);
				this.setTitle(this.endpoint.getName());
			}
			else
				this.endpoint = new Endpoint();
		}
		
		this.addPreferencesFromResource(R.xml.endpoint_headers);
		
		this.endpoint_name = new EditTextPreference(this);
		this.endpoint_name.setTitle(R.string.endpoint_name);
		this.endpoint_name.setDefaultValue(this.endpoint.getName());
		
		((PreferenceCategory)this.findPreference("endpoint_settings")).addPreference(this.endpoint_name);
		
		this.endpoint_host = new EditTextPreference(this);
		this.endpoint_host.setTitle(R.string.endpoint_host);
		this.endpoint_host.setDefaultValue(this.endpoint.getHost());
		
		((PreferenceCategory)this.findPreference("endpoint_settings")).addPreference(this.endpoint_host);
		
		this.endpoint_port = new EditTextPreference(this);
		this.endpoint_port.setTitle(R.string.endpoint_port);
		this.endpoint_port.setDefaultValue(Integer.valueOf(this.endpoint.getPort()).toString());
		this.endpoint_port.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		((PreferenceCategory)this.findPreference("endpoint_settings")).addPreference(this.endpoint_port);
		
		this.endpoint_ssl = new CheckBoxPreference(this);
		this.endpoint_ssl.setTitle(R.string.enable_ssl);
		this.endpoint_ssl.setDefaultValue(this.endpoint.isSSL());
		
		((PreferenceCategory)this.findPreference("security_settings")).addPreference(this.endpoint_ssl);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_endpoint_settings, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.cancel_endpoint:
    		this.finish();
    		return true;
    		
    	case R.id.save_endpoint:
    		Bundle bundle = new Bundle();
    		bundle.putInt("endpoint:id", this.endpoint.getId());
    		bundle.putString("endpoint:name", this.endpoint_name.getText());
    		bundle.putString("endpoint:host", this.endpoint_host.getText());
    		bundle.putInt("endpoint:port", Integer.parseInt(this.endpoint_port.getText()));
    		
    		Intent intent = this.getIntent();
    		intent.putExtras(bundle);
    		
    		this.setResult(RESULT_OK, intent);
    		this.finish();
    		return true;
    	
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
	
}
