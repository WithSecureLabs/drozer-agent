package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.CheckListItemView;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.ServerParameters;

import android.os.Bundle;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

public class ServerActivity extends ConnectorActivity implements Observer, ServerParameters.OnDetailedStatusListener {

	private ServerParameters parameters = null;
	
	private CompoundButton server_enabled = null;
	private ListView server_messages = null;
	private ConnectorStatusIndicator server_status_indicator = null;
	
	private CheckListItemView status_enabled = null;
	private CheckListItemView status_listening = null;
	private CheckListItemView status_password = null;
	private CheckListItemView status_sessions = null;
	private CheckListItemView status_ssl = null;
	
	protected void getDetailedServerStatus() {
		try {
			Agent.getInstance().getServerService().getDetailedServerStatus(Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			Toast.makeText(this, "problem, server service not running", Toast.LENGTH_SHORT).show();
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
    	this.status_enabled.setStatus(status.getBoolean("server:enabled"));
    	this.status_listening.setStatus(status.getBoolean("server:connected"));
    	this.status_password.setStatus(status.getBoolean("server:password_enabled"));
    	this.status_sessions.setStatus(status.getBoolean("server:sessions"));
    	this.status_ssl.setStatus(status.getBoolean("server:ssl_enabled"));
	}
    
    @Override
    /**
     * Refresh the status indicators, to show the current status of the Endpoint.
     */
    protected void refreshStatus() {
    	this.getDetailedServerStatus();
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
    
    private Dialog spinner;
    
    @Override
    protected void showFingerprintDialog() {
    	if(!this.parameters.isSSL()) {
			this.createInformationDialog(R.string.ssl_fingerprint, R.string.ssl_disabled).show();
		}
		else if(this.parameters.getStatus() != Endpoint.Status.ACTIVE && this.parameters.getStatus() != Endpoint.Status.ONLINE) {
			this.createInformationDialog(R.string.ssl_fingerprint, "offline").show();
		}
		else {
			this.spinner = ProgressDialog.show(this, "", getString(R.string.calculating), true);
			
			try {
				Agent.getInstance().getServerService().getHostFingerprint(new Messenger(new IncomingFingerprintHandler(this)));
			}
			catch(RemoteException e) {
				spinner.dismiss();
				
				this.createInformationDialog(R.string.ssl_fingerprint, "error");
			}
		}
    }
    
    @Override
    public void receiveFingerprint(String fingerprint) {
    	if(this.spinner != null)
			this.spinner.dismiss();
		
		this.createInformationDialog(R.string.ssl_fingerprint, fingerprint).show();
    }
    
    protected void startServer() {
		try {
			this.parameters.enabled = true;
			this.parameters.setStatus(ServerParameters.Status.UPDATING);
			
			Agent.getInstance().getServerService().startServer(Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {			
			this.parameters.setStatus(ServerParameters.Status.OFFLINE);
			
			Toast.makeText(this, "problem, server service not running", Toast.LENGTH_SHORT).show();
		}
    }
    
    protected void stopServer() {
		try {
			this.parameters.enabled = true;
			this.parameters.setStatus(ServerParameters.Status.UPDATING);
			
			Agent.getInstance().getServerService().stopServer(Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {			
			this.parameters.setStatus(ServerParameters.Status.OFFLINE);
			
			Toast.makeText(this, "problem, server service not running", Toast.LENGTH_SHORT).show();
		}
    }

	@Override
	public void update(Observable observable, Object data) {
		this.setServerParameters((ServerParameters)observable);
		this.refreshStatus();
	}

}
