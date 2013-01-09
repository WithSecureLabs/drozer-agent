package com.mwr.droidhg.agent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Observable;
import java.util.Observer;

import com.mwr.common.tls.X509Fingerprint;
import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.CheckListItemView;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.api.ServerParameters;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;

public class ServerActivity extends Activity implements Observer, ServerParameters.OnDetailedStatusListener {

	private ServerParameters parameters = null;
	
	private CompoundButton server_enabled = null;
	private ListView server_messages = null;
	private ConnectorStatusIndicator server_status_indicator = null;
	
	private CheckListItemView status_enabled = null;
	private CheckListItemView status_listening = null;
	private CheckListItemView status_password = null;
	private CheckListItemView status_sessions = null;
	private CheckListItemView status_ssl = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_server);
        
        this.server_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.server_status_indicator);
        
        this.server_enabled = (CompoundButton)this.findViewById(R.id.server_enabled);
        this.server_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					Agent.startServer();
				else
					Agent.stopServer();
			}
        	
        });
        
        this.server_messages = (ListView)this.findViewById(R.id.server_messages);
        this.server_messages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.server_messages.setStackFromBottom(true);
        
        this.status_enabled = (CheckListItemView)this.findViewById(R.id.server_status_enabled);
        this.status_listening = (CheckListItemView)this.findViewById(R.id.server_status_listening);
        this.status_password = (CheckListItemView)this.findViewById(R.id.server_status_password);
        this.status_sessions = (CheckListItemView)this.findViewById(R.id.server_status_sessions);
        this.status_ssl = (CheckListItemView)this.findViewById(R.id.server_status_ssl);
        
        this.setServerParameters(Agent.getServerParameters());
        this.refreshStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server, menu);
        return true;
    }

	@Override
	public void onDetailedStatus(Bundle status) {
    	this.status_enabled.setStatus(status.getBoolean("server:enabled"));
    	this.status_listening.setStatus(status.getBoolean("server:connected"));
    	this.status_password.setStatus(status.getBoolean("server:password_enabled"));
    	this.status_sessions.setStatus(status.getBoolean("server:sessions"));
    	this.status_ssl.setStatus(status.getBoolean("server:ssl_enabled"));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.server_show_fingerprint:
			this.showFingerprintDialog();
			return true;
			
		case R.id.server_refresh_status:
			this.refreshStatus();
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
    
    /**
     * Refresh the status indicators, to show the current status of the Endpoint.
     */
    private void refreshStatus() {
    	Agent.getServerDetailedStatus();
    }
    
    private void setServerParameters(ServerParameters parameters) {
    	if(this.parameters != null)
    		this.parameters.deleteObserver(this);
    	
    	this.parameters = parameters;
    	
    	this.server_enabled.setChecked(this.parameters.isEnabled());
    	this.server_messages.setAdapter(new LogMessageAdapter(this.getApplicationContext(), this.parameters));
    	this.server_status_indicator.setConnector(this.parameters);
    	
    	this.parameters.addObserver(this);
    	this.parameters.setOnDetailedStatusListener(this);
    }
    
    class FingerprintCalculation extends AsyncTask<String, String, String> {
    	
    	private Dialog spinner;

		@Override
		protected String doInBackground(String... params) {
			try {
				return ServerActivity.this.parameters.getCertificateFingerprint();
			}
			catch(UnrecoverableKeyException e) {}
			catch(CertificateException e) {}
			catch(FileNotFoundException e) {}
			catch(KeyStoreException e) {}
			catch(NoSuchAlgorithmException e) {}
			catch(IOException e) {}
			
			return "Unable to calculate.";
		}
		
		@Override
		protected void onPostExecute(String fingerprint) {
			if(this.spinner != null)
				this.spinner.dismiss();
			
			ServerActivity.this.createInformationDialog(R.string.ssl_fingerprint, fingerprint).show();
		}
		
		@Override
		protected void onPreExecute() {
			this.spinner = ProgressDialog.show(ServerActivity.this, "", getString(R.string.calculating), true);
		}
    	
    }
    
    private void showFingerprintDialog() {
		if(this.parameters.isSSL()) {
			new FingerprintCalculation().execute();
		}
		else {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_disabled).show();
		}
    }
    
    private Dialog createInformationDialog(int titleId, int messageId) {
    	return new AlertDialog.Builder(this)
    		.setTitle(titleId)
    		.setMessage(messageId)
    		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    		
    			public void onClick(DialogInterface dialog, int id) {}
            
    		})
    		.create();
    }
    
    private Dialog createInformationDialog(int titleId, String message) {
    	return new AlertDialog.Builder(this)
    		.setTitle(titleId)
    		.setMessage(message)
    		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    		
    			public void onClick(DialogInterface dialog, int id) {}
            
    		})
    		.create();
    }

	@Override
	public void update(Observable observable, Object data) {
		this.setServerParameters((ServerParameters)observable);
		this.refreshStatus();
	}

}
