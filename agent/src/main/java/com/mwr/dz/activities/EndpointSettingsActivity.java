package com.mwr.dz.activities;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
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
	
	public static final String ENDPOINT_SETTINGS_PREFERENCE = "endpoint_settings";
	public static final String SECURITY_SETTINGS_PREFERENCE = "security_settings";
	public static final String SSL_ENABLED_PREFERENCE = "endpoint_ssl_enabled";
	public static final String SSL_TRUSTSTORE_PASSWORD_PREFERENCE = "endpoint_ssl_truststore_password";
	public static final String SSL_TRUSTSTORE_PATH_PREFERENCE = "endpoint_ssl_truststore_path";
	
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
			int endpoint_id = bundle.getInt(Endpoint.ENDPOINT_ID);
			if(endpoint_id > 0) {
				this.endpoint = Agent.getInstance().getEndpointManager().get(endpoint_id);
				this.setTitle(this.endpoint.getName());
			}
			else
				this.endpoint = new Endpoint();
		}
		
		this.addPreferencesFromResource(R.xml.endpoint_headers);
		
		this.endpoint_name = new EditTextPreference(this);
		this.endpoint_name.setTitle(R.string.endpoint_name);
		this.endpoint_name.setDefaultValue(this.endpoint.getName());
		
		((PreferenceCategory)this.findPreference(ENDPOINT_SETTINGS_PREFERENCE)).addPreference(this.endpoint_name);
		
		this.endpoint_host = new EditTextPreference(this);
		this.endpoint_host.setTitle(R.string.endpoint_host);
		this.endpoint_host.setDefaultValue(this.endpoint.getHost());
		
		((PreferenceCategory)this.findPreference(ENDPOINT_SETTINGS_PREFERENCE)).addPreference(this.endpoint_host);
		
		this.endpoint_port = new EditTextPreference(this);
		this.endpoint_port.setTitle(R.string.endpoint_port);
		this.endpoint_port.setDefaultValue(Integer.valueOf(this.endpoint.getPort()).toString());
		this.endpoint_port.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		((PreferenceCategory)this.findPreference(ENDPOINT_SETTINGS_PREFERENCE)).addPreference(this.endpoint_port);
		
		this.endpoint_password = new EditTextPreference(this);
		this.endpoint_password.setTitle(R.string.endpoint_password);
		this.endpoint_password.setSummary(R.string.endpoint_password_description);
		this.endpoint_password.setDefaultValue(this.endpoint.getPassword());
		
		((PreferenceCategory)this.findPreference(SECURITY_SETTINGS_PREFERENCE)).addPreference(this.endpoint_password);
		
		this.endpoint_ssl = new CheckBoxPreference(this);
		this.endpoint_ssl.setKey(SSL_ENABLED_PREFERENCE);
		this.endpoint_ssl.setTitle(R.string.ssl_enable);
		this.endpoint_ssl.setSummary(R.string.ssl_enable_description);
		this.endpoint_ssl.setDefaultValue(this.endpoint.isSSL());
		this.endpoint_ssl.setDisableDependentsState(false);
		
		((PreferenceCategory)this.findPreference(SECURITY_SETTINGS_PREFERENCE)).addPreference(this.endpoint_ssl);
		
		this.endpoint_ssl_truststore_path = new EditTextPreference(this);
		
		this.endpoint_ssl_truststore_path.setKey(SSL_TRUSTSTORE_PATH_PREFERENCE);
		this.endpoint_ssl_truststore_path.setTitle(R.string.endpoint_ssl_truststore_path);
		this.endpoint_ssl_truststore_path.setSummary(R.string.endpoint_ssl_truststore_path_description);
		this.endpoint_ssl_truststore_path.setDefaultValue(this.endpoint.getSSLTrustStorePath());
		
		((PreferenceCategory)this.findPreference(SECURITY_SETTINGS_PREFERENCE)).addPreference(this.endpoint_ssl_truststore_path);
		this.endpoint_ssl_truststore_path.setDependency(SSL_ENABLED_PREFERENCE);
		
		this.endpoint_ssl_truststore_password = new EditTextPreference(this);
		this.endpoint_ssl_truststore_password.setKey(SSL_TRUSTSTORE_PASSWORD_PREFERENCE);
		this.endpoint_ssl_truststore_password.setTitle(R.string.endpoint_ssl_truststore_password);
		this.endpoint_ssl_truststore_password.setSummary(R.string.endpoint_ssl_truststore_password_description);
		this.endpoint_ssl_truststore_password.setDefaultValue(this.endpoint.getSSLTrustStorePassword());
		
		((PreferenceCategory)this.findPreference(SECURITY_SETTINGS_PREFERENCE)).addPreference(this.endpoint_ssl_truststore_password);
		this.endpoint_ssl_truststore_password.setDependency(SSL_ENABLED_PREFERENCE);
		
		this.button_forget = (Button)this.findViewById(R.id.button_forget);
		
		if(this.endpoint.isNew()) {
			this.button_forget.setVisibility(View.GONE);
		}
		else {
			this.button_forget.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					EndpointSettingsActivity.this.endpoint.enabled = false;
					EndpointSettingsActivity.this.endpoint.setStatus(Endpoint.Status.UPDATING);
					
					try {
						Agent.getInstance().getClientService().stopEndpoint(EndpointSettingsActivity.this.endpoint, Agent.getInstance().getMessenger());
					}
					catch(RemoteException e) {}
					
					if(Agent.getInstance().getEndpointManager().remove(EndpointSettingsActivity.this.endpoint)) {
						Bundle bundle = new Bundle();
			    		bundle.putBoolean(Endpoint.ENDPOINT_DELETED, true);
			    		bundle.putInt(Endpoint.ENDPOINT_ID, EndpointSettingsActivity.this.endpoint.getId());
			    		
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
    		bundle.putInt(Endpoint.ENDPOINT_ID, this.endpoint.getId());
    		bundle.putString(Endpoint.ENDPOINT_NAME, this.endpoint_name.getText());
    		bundle.putString(Endpoint.ENDPOINT_HOST, this.endpoint_host.getText());
    		bundle.putString(Endpoint.ENDPOINT_PASSWORD, this.endpoint_password.getText());
    		bundle.putInt(Endpoint.ENDPOINT_PORT, Integer.parseInt(this.endpoint_port.getText()));
    		bundle.putBoolean(Endpoint.ENDPOINT_SSL, this.endpoint_ssl.isChecked());
    		bundle.putString(Endpoint.ENDPOINT_TRUSTSTORE_PATH, this.endpoint_ssl_truststore_path.getText());
    		bundle.putString(Endpoint.ENDPOINT_TRUSTSTORE_PASSWORD, this.endpoint_ssl_truststore_password.getText());
    		
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
