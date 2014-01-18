package com.mwr.dz.activities;

import java.util.Observable;
import java.util.Observer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.dz.views.CheckListItemView;
import com.mwr.dz.views.ConnectorStatusIndicator;
import com.mwr.dz.views.logger.LogMessageAdapter;
import com.mwr.jdiesel.api.connectors.Connector;
import com.mwr.jdiesel.api.connectors.Endpoint;

public class EndpointActivity extends ConnectorActivity implements Observer, Endpoint.OnDetailedStatusListener {
	
	private Endpoint endpoint = null;
	private CompoundButton endpoint_enabled = null;
	private ListView endpoint_messages = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;
	
    protected Dialog spinner;
	
	private CheckListItemView status_connected = null;
	private CheckListItemView status_enabled = null;
	private CheckListItemView status_password = null;
	private CheckListItemView status_sessions = null;
	private CheckListItemView status_ssl = null;
	
	private volatile boolean setting_endpoint = false; 
	
	protected void getDetailedEndpointStatus() {
		try {
			Agent.getInstance().getClientService().getDetailedEndpointStatus(this.endpoint.getId(), Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = this.getIntent().getExtras();
        
        this.setContentView(R.layout.activity_endpoint);
        
        this.endpoint_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.endpoint_status_indicator);
        
        this.endpoint_enabled = (CompoundButton)this.findViewById(R.id.endpoint_enabled);
        this.endpoint_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(EndpointActivity.this.setting_endpoint)
					return;
				
				if(isChecked)
					EndpointActivity.this.startEndpoint();
				else
					EndpointActivity.this.stopEndpoint();
			}
        	
        });
        
        this.endpoint_messages = (ListView)this.findViewById(R.id.endpoint_messages);
        this.endpoint_messages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.endpoint_messages.setStackFromBottom(true);
        
        this.status_connected = (CheckListItemView)this.findViewById(R.id.endpoint_status_connected);
        this.status_enabled = (CheckListItemView)this.findViewById(R.id.endpoint_status_enabled);
        this.status_password = (CheckListItemView)this.findViewById(R.id.endpoint_status_password);
        this.status_sessions = (CheckListItemView)this.findViewById(R.id.endpoint_status_sessions);
        this.status_ssl = (CheckListItemView)this.findViewById(R.id.endpoint_status_ssl);
        
        this.setEndpoint(Agent.getInstance().getEndpointManager().get(extras.getInt(Endpoint.ENDPOINT_ID)));
        this.refreshStatus();
    }

	@Override
	public void onDetailedStatus(Bundle status) {
		this.status_connected.setStatus(status.getBoolean(Endpoint.CONNECTOR_CONNECTED));
    	this.status_enabled.setStatus(status.getBoolean(Endpoint.CONNECTOR_ENABLED));
    	this.status_password.setStatus(status.getBoolean(Endpoint.ENDPOINT_PASSWORD));
    	this.status_sessions.setStatus(status.getBoolean(Endpoint.CONNECTOR_OPEN_SESSIONS));
    	this.status_ssl.setStatus(status.getBoolean(Endpoint.ENDPOINT_SSL));
	}
    
	@Override
    /**
     * Refresh the status indicators, to show the current status of the Endpoint.
     */
    protected void refreshStatus() {
		this.getDetailedEndpointStatus();
    }
    
    /**
     * Set the Endpoint displayed in the Activity.
     * 
     * This keeps a local copy of the Endpoint, updates various UI components with
     * the latest information, and sets us up as an observer of the Endpoint model
     * to get future changes.
     * 
     * @param endpoint the endpoint given to this activity from the intent
     */
    private void setEndpoint(Endpoint endpoint) {
    	if(this.endpoint != null)
    		this.endpoint.deleteObserver(this);
    	
    	this.setting_endpoint = true;
    	this.endpoint = endpoint;
    	
    	this.setTitle(this.endpoint.getName());
    	
    	this.endpoint_enabled.setChecked(this.endpoint.isEnabled());
    	this.endpoint_messages.setAdapter(new LogMessageAdapter<Connector>(this.getApplicationContext(), this.endpoint.getLogger()));
    	this.endpoint_status_indicator.setConnector(this.endpoint);
    	this.setting_endpoint = false;
    	
    	this.endpoint.addObserver(this);
    	this.endpoint.setOnDetailedStatusListener(this);
    }
    
	@Override
	protected void showFingerprintDialog() {
		if(!this.endpoint.isSSL()) {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_disabled).show();
		}
		else if(this.endpoint.getStatus() != Endpoint.Status.ACTIVE && this.endpoint.getStatus() != Endpoint.Status.ONLINE) {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.endpoint_offline_no_fingerprint).show();
		}
		else {
			this.spinner = ProgressDialog.show(this, getString(R.string.ssl_fingerprint), getString(R.string.calculating), true);
			
			try {
				Agent.getInstance().getClientService().getPeerFingerprint(this.endpoint.getId(), new Messenger(new IncomingFingerprintHandler(this)));
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
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_fingerprint_error).show();
	}
	
	protected void startEndpoint() {
		try {
			this.endpoint.enabled = true;
			this.endpoint.setStatus(Endpoint.Status.UPDATING);
			
			Agent.getInstance().getClientService().startEndpoint(this.endpoint, Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			this.endpoint.setStatus(Endpoint.Status.OFFLINE);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void stopEndpoint() {
		try {
			this.endpoint.enabled = false;
			this.endpoint.setStatus(Endpoint.Status.UPDATING);
			
			Agent.getInstance().getClientService().stopEndpoint(this.endpoint, Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			this.endpoint.setStatus(Endpoint.Status.OFFLINE);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	/**
	 * Triggered by the Observer pattern, whenever the related Endpoint is updated.
	 * 
	 * We trigger an update of the local Endpoint, and refresh all of the status
	 * indicators with the most up-to-date information.
	 */
	public void update(Observable observable, Object data) {
		this.setEndpoint((Endpoint)observable);
		this.refreshStatus();
	}

}
