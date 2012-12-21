package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.agent.views.ServerParametersDialog;
import com.mwr.droidhg.api.ServerParameters;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity implements Observer {

	private ServerParameters parameters = null;
	
	private TextView label_server_fingerprint = null;
	private TextView label_server_ssl = null;
	private CheckBox server_enabled = null;
	private ConnectorStatusIndicator server_status_indicator = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_server);
        
        this.label_server_fingerprint = (TextView)this.findViewById(R.id.label_server_fingerprint);
        this.label_server_ssl = (TextView)this.findViewById(R.id.label_server_ssl);
        this.server_enabled = (CheckBox)this.findViewById(R.id.server_enabled);
        this.server_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.server_status_indicator);
        
        this.setServerParameters(Agent.getServerParameters());
        
        this.server_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					Agent.startServer();
				else
					Agent.stopServer();
			}
        	
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_server, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_edit_server:
    		Agent.stopServer();
			
    		ServerParametersDialog dialog = new ServerParametersDialog(this);
    		dialog.setParameters(this.parameters);
        	dialog.setOnSaveListener(new ServerParametersDialog.OnSaveListener() {
				
				@Override
				public boolean onSave(ServerParameters parameters) {
					if(ServerActivity.this.parameters.update(parameters)) {
						Toast.makeText(ServerActivity.this.getApplicationContext(), "Updated Server", Toast.LENGTH_SHORT).show();
						
						return true;
					}
					else {
						Toast.makeText(ServerActivity.this.getApplicationContext(), "Error updating Server", Toast.LENGTH_SHORT).show();
						
						return false;
					}
				}
				
			});
        	dialog.create().show();
    		return true;
    	
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	Agent.unbindServices();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Agent.bindServices();
    }
    
    class CalculateFingerprint extends AsyncTask<Object, Object, String> {

		@Override
		protected String doInBackground(Object... params) {
			return ServerActivity.this.parameters.getCertificateFingerprint();
		}
		
		@Override
		protected void onPreExecute() {
			ServerActivity.this.label_server_fingerprint.setText("Calculating Fingerprint...");
		}
		
		@Override
		protected void onPostExecute(String result) {
			ServerActivity.this.fingerprint_calculation = null;
			ServerActivity.this.label_server_fingerprint.setText(result);
		}
    	
    }
    
    private CalculateFingerprint fingerprint_calculation;
    
    private void setServerParameters(ServerParameters parameters) {
    	if(this.parameters != null)
    		this.parameters.deleteObserver(this);
    	
    	this.parameters = parameters;
    	
    	if(this.fingerprint_calculation != null)
    		this.fingerprint_calculation.cancel(true);

		if(ServerActivity.this.parameters.isSSL()) {
	    	this.fingerprint_calculation = new CalculateFingerprint();
	    	this.fingerprint_calculation.execute();
		}
    	
    	this.label_server_ssl.setText(this.parameters.isSSL() ? R.string.ssl_enabled : R.string.ssl_disabled);
    	this.server_enabled.setChecked(this.parameters.isEnabled());
    	this.server_status_indicator.setConnector(this.parameters);
    	
    	this.parameters.addObserver(this);
    }

	@Override
	public void update(Observable observable, Object data) {
		this.setServerParameters((ServerParameters)observable);
	}

}
