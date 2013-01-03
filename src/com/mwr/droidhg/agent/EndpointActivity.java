package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.CompoundButton;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.api.Endpoint;

public class EndpointActivity extends Activity implements Observer {
	
	private Endpoint endpoint = null;
	private CompoundButton endpoint_enabled = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = this.getIntent().getExtras();
        
        this.setContentView(R.layout.activity_endpoint);
        
        this.endpoint_enabled = (CompoundButton)this.findViewById(R.id.endpoint_enabled);
        this.endpoint_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.endpoint_status_indicator);
        
        this.setEndpoint(Agent.getEndpointManager().get(extras.getInt("endpoint_id")));
        
        this.endpoint_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					Agent.startEndpoint(endpoint);
				else
					Agent.stopEndpoint(endpoint);
			}
        	
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
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
    
    private void setEndpoint(Endpoint endpoint) {
    	if(this.endpoint != null)
    		this.endpoint.deleteObserver(this);
    	
    	this.endpoint = endpoint;
    	
    	this.setTitle(this.endpoint.getName());
    	
    	this.endpoint_enabled.setChecked(this.endpoint.isEnabled());
    	this.endpoint_status_indicator.setConnector(this.endpoint);
    	
    	this.endpoint.addObserver(this);
    }

	@Override
	public void update(Observable observable, Object data) {
		this.setEndpoint((Endpoint)observable);
	}

}
