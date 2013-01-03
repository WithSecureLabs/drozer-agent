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
import android.view.View;
import android.widget.Button;

public class EndpointSettingsActivity extends PreferenceActivity {
	
	private Endpoint endpoint;

	private EditTextPreference endpoint_host;
	private EditTextPreference endpoint_name;
	private EditTextPreference endpoint_password;
	private EditTextPreference endpoint_port;
	private CheckBoxPreference endpoint_ssl;
	private EditTextPreference endpoint_ssl_truststore_password;
	private EditTextPreference endpoint_ssl_truststore_path;
	
	private Button button_forget;

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_endpoint_settings);
		
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
		this.endpoint_ssl.setKey("endpoint_ssl_enabled");
		this.endpoint_ssl.setTitle(R.string.ssl_enable);
		this.endpoint_ssl.setSummary(R.string.ssl_enable_description);
		this.endpoint_ssl.setDefaultValue(this.endpoint.isSSL());
		this.endpoint_ssl.setDisableDependentsState(false);
		
		((PreferenceCategory)this.findPreference("security_settings")).addPreference(this.endpoint_ssl);
		
		this.endpoint_ssl_truststore_path = new EditTextPreference(this);
		
		this.endpoint_ssl_truststore_path.setKey("endpoint_ssl_truststore_path");
		this.endpoint_ssl_truststore_path.setTitle(R.string.endpoint_ssl_truststore_path);
		this.endpoint_ssl_truststore_path.setSummary(R.string.endpoint_ssl_truststore_path_description);
		this.endpoint_ssl_truststore_path.setDefaultValue(this.endpoint.getSSLTrustStorePath());
		
		((PreferenceCategory)this.findPreference("security_settings")).addPreference(this.endpoint_ssl_truststore_path);
		this.endpoint_ssl_truststore_path.setDependency(this.endpoint_ssl.getKey());
		
		this.endpoint_ssl_truststore_password = new EditTextPreference(this);
		this.endpoint_ssl_truststore_password.setKey("endpoint_ssl_truststore_password");
		this.endpoint_ssl_truststore_password.setTitle(R.string.endpoint_ssl_truststore_password);
		this.endpoint_ssl_truststore_password.setSummary(R.string.endpoint_ssl_truststore_password_description);
		this.endpoint_ssl_truststore_password.setDefaultValue(this.endpoint.getSSLTrustStorePassword());
		
		((PreferenceCategory)this.findPreference("security_settings")).addPreference(this.endpoint_ssl_truststore_password);
		this.endpoint_ssl_truststore_password.setDependency(this.endpoint_ssl.getKey());
		
		this.endpoint_password = new EditTextPreference(this);
		this.endpoint_password.setTitle(R.string.endpoint_password);
		this.endpoint_password.setSummary(R.string.endpoint_password_description);
		this.endpoint_password.setDefaultValue(this.endpoint.getPassword());
		
		((PreferenceCategory)this.findPreference("security_settings")).addPreference(this.endpoint_password);
		
		this.button_forget = (Button)this.findViewById(R.id.button_forget);
		
		if(this.endpoint.isNew()) {
			this.button_forget.setVisibility(View.GONE);
		}
		else {
			this.button_forget.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Agent.stopEndpoint(EndpointSettingsActivity.this.endpoint);
					
					if(Agent.getEndpointManager().remove(EndpointSettingsActivity.this.endpoint)) {
						Bundle bundle = new Bundle();
			    		bundle.putBoolean("endpoint:deleted", true);
			    		bundle.putInt("endpoint:id", EndpointSettingsActivity.this.endpoint.getId());
			    		
			    		Intent intent = EndpointSettingsActivity.this.getIntent();
			    		intent.putExtras(bundle);
			    		
			    		EndpointSettingsActivity.this.setResult(RESULT_OK, intent);
						EndpointSettingsActivity.this.finish();
					}
				}
				
			});
		}
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
    		bundle.putString("endpoint:password", this.endpoint_password.getText());
    		bundle.putInt("endpoint:port", Integer.parseInt(this.endpoint_port.getText()));
    		bundle.putBoolean("endpoint:ssl", this.endpoint_ssl.isChecked());
    		bundle.putString("endpoint:ssl_truststore_path", this.endpoint_ssl_truststore_path.getText());
    		bundle.putString("endpoint:ssl_truststore_password", this.endpoint_ssl_truststore_password.getText());
    		
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
