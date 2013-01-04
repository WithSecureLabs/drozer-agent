package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.CheckListItemView;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.api.Endpoint;

public class EndpointActivity extends Activity implements Observer, Endpoint.OnDetailedStatusListener, Endpoint.OnLogMessageListener {
	
	private Endpoint endpoint = null;
	private CompoundButton endpoint_enabled = null;
	private TextView endpoint_messages = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;
	
	private CheckListItemView status_connected = null;
	private CheckListItemView status_enabled = null;
	private CheckListItemView status_password = null;
	private CheckListItemView status_sessions = null;
	private CheckListItemView status_ssl = null;

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
				if(isChecked)
					Agent.startEndpoint(endpoint);
				else
					Agent.stopEndpoint(endpoint);
			}
        	
        });
        
        this.endpoint_messages = (TextView)this.findViewById(R.id.endpoint_messages);
        
        this.status_connected = (CheckListItemView)this.findViewById(R.id.endpoint_status_connected);
        this.status_enabled = (CheckListItemView)this.findViewById(R.id.endpoint_status_enabled);
        this.status_password = (CheckListItemView)this.findViewById(R.id.endpoint_status_password);
        this.status_sessions = (CheckListItemView)this.findViewById(R.id.endpoint_status_sessions);
        this.status_ssl = (CheckListItemView)this.findViewById(R.id.endpoint_status_ssl);
        
        this.setEndpoint(Agent.getEndpointManager().get(extras.getInt("endpoint_id")));
        this.refreshStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

	@Override
	public void onDetailedStatus(Bundle status) {
		this.status_connected.setStatus(status.getBoolean("endpoint:connected"));
    	this.status_enabled.setStatus(status.getBoolean("endpoint:enabled"));
    	this.status_password.setStatus(status.getBoolean("endpoint:password_enabled"));
    	this.status_sessions.setStatus(status.getBoolean("endpoint:sessions"));
    	this.status_ssl.setStatus(status.getBoolean("endpoint:ssl_enabled"));
	}
	
	@Override
	public void onLogMessage(String message) {
		String log = this.endpoint_messages.getText() + "\n" + message;
		
		this.endpoint_messages.setText(log);
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
    	this.status_connected.setLabel(R.string.connected);
    	this.status_enabled.setLabel(R.string.enabled);
    	this.status_password.setLabel(R.string.endpoint_password_protected);
    	this.status_sessions.setLabel(R.string.session_active);
    	this.status_ssl.setLabel(R.string.ssl_enabled);
    	
    	Agent.getEndpointDetailedStatus(this.endpoint);
    }
    
    /**
     * Set the Endpoint displayed in the Activity.
     * 
     * This keeps a local copy of the Endpoint, updates various UI components with
     * the latest information, and sets us up as an observer of the Endpoint model
     * to get future changes.
     */
    private void setEndpoint(Endpoint endpoint) {
    	if(this.endpoint != null)
    		this.endpoint.deleteObserver(this);
    	
    	this.endpoint = endpoint;
    	
    	this.setTitle(this.endpoint.getName());
    	
    	this.endpoint_enabled.setChecked(this.endpoint.isEnabled());
    	this.endpoint_status_indicator.setConnector(this.endpoint);
    	
    	this.endpoint.addObserver(this);
    	this.endpoint.setOnDetailedStatusListener(this);
    	this.endpoint.setOnLogMessageListener(this);
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
