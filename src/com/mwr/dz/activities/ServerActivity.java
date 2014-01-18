package com.mwr.dz.activities;

import java.util.Observable;
import java.util.Observer;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.dz.views.CheckListItemView;
import com.mwr.dz.views.ConnectorStatusIndicator;
import com.mwr.dz.views.logger.LogMessageAdapter;
import com.mwr.jdiesel.api.connectors.Connector;
import com.mwr.jdiesel.api.connectors.Endpoint;
import com.mwr.jdiesel.api.connectors.Server;

import android.os.Bundle;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

public class ServerActivity extends ConnectorActivity implements Observer, Server.OnDetailedStatusListener {

	private Server parameters = null;
	
	private CompoundButton server_enabled = null;
	private ListView server_messages = null;
	private ConnectorStatusIndicator server_status_indicator = null;
	
	private CheckListItemView status_enabled = null;
	private CheckListItemView status_listening = null;
	private CheckListItemView status_password = null;
	private CheckListItemView status_sessions = null;
	private CheckListItemView status_ssl = null;
	
	private volatile boolean setting_server = false;
	
	protected void getDetailedServerStatus() {
		try {
			Agent.getInstance().getServerService().getDetailedServerStatus(Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_server);
        
        this.server_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.server_status_indicator);
        
        this.server_enabled = (CompoundButton)this.findViewById(R.id.server_enabled);
        this.server_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(ServerActivity.this.setting_server)
					return;
				
				if(isChecked)
					ServerActivity.this.startServer();
				else
					ServerActivity.this.stopServer();
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
        
        
        this.setServerParameters(Agent.getInstance().getServerParameters());
        this.refreshStatus();
    }

	@Override
	public void onDetailedStatus(Bundle status) {
    	this.status_enabled.setStatus(status.getBoolean(Server.CONNECTOR_ENABLED));
    	this.status_listening.setStatus(status.getBoolean(Server.CONNECTOR_CONNECTED));
    	this.status_password.setStatus(status.getBoolean(Server.SERVER_PASSWORD));
    	this.status_sessions.setStatus(status.getBoolean(Server.CONNECTOR_OPEN_SESSIONS));
    	this.status_ssl.setStatus(status.getBoolean(Server.SERVER_SSL));
	}
    
    @Override
    /**
     * Refresh the status indicators, to show the current status of the Endpoint.
     */
    protected void refreshStatus() {
    	this.getDetailedServerStatus();
    }
    
    /**
     * 
     * @param parameters the server parameters
     * @param auto_start start the server automatically
     */
    		
    private void setServerParameters(Server parameters) {
    	if(this.parameters != null)
    		this.parameters.deleteObserver(this);
    	
    	this.setting_server = true;
    	this.parameters = parameters;
    	
    	this.server_enabled.setChecked(this.parameters.isEnabled());
    	this.server_messages.setAdapter(new LogMessageAdapter<Connector>(this.getApplicationContext(), this.parameters.getLogger()));
    	this.server_status_indicator.setConnector(this.parameters);
    	this.setting_server = false;
    	
    	this.parameters.addObserver(this);
    	this.parameters.setOnDetailedStatusListener(this);
    }
    
    private Dialog spinner;
    
    @Override
    protected void showFingerprintDialog() {
    	if(!this.parameters.isSSL()) {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_disabled).show();
		}
		else if(this.parameters.getStatus() != Endpoint.Status.ACTIVE && this.parameters.getStatus() != Endpoint.Status.ONLINE) {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.endpoint_offline_no_fingerprint).show();
		}
		else {
			this.spinner = ProgressDialog.show(this, getString(R.string.ssl_fingerprint), getString(R.string.calculating), true);
			
			try {
				Agent.getInstance().getServerService().getHostFingerprint(new Messenger(new IncomingFingerprintHandler(this)));
			}
			catch(RemoteException e) {
				spinner.dismiss();
				
				this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_fingerprint_error);
			}
		}
    }
    
    @Override
    public void receiveFingerprint(String fingerprint) {
    	if(this.spinner != null)
			this.spinner.dismiss();
		
    	if(fingerprint != null)
    		this.createInformationDialog(R.string.ssl_fingerprint, fingerprint).show();
    	else
    		this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_fingerprint_error);
    }
    
    protected void startServer() {
		try {
			this.parameters.enabled = true;
			this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.UPDATING);
			
			Agent.getInstance().getServerService().startServer(Agent.getInstance().getServerParameters(), Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {			
			this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.OFFLINE);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
    }
    
    protected void stopServer() {
		try {
			this.parameters.enabled = false;
			this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.UPDATING);
			
			Agent.getInstance().getServerService().stopServer(Agent.getInstance().getServerParameters(), Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {			
			this.parameters.setStatus(com.mwr.jdiesel.api.connectors.Server.Status.OFFLINE);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
    }

	@Override
	public void update(Observable observable, Object data) {
		this.setServerParameters((Server)observable);
		this.refreshStatus();
	}

}
