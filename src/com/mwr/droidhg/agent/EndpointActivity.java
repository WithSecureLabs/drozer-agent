package com.mwr.droidhg.agent;

import java.util.Observable;
import java.util.Observer;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.ConnectorStatusIndicator;
import com.mwr.droidhg.agent.views.EndpointDialog;
import com.mwr.droidhg.api.Endpoint;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class EndpointActivity extends Activity implements Observer {
	
	private Endpoint endpoint = null;
	private CheckBox endpoint_enabled = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = this.getIntent().getExtras();
        
        this.setContentView(R.layout.activity_endpoint);
//        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.endpoint_enabled = (CheckBox)this.findViewById(R.id.endpoint_enabled);
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
    	getMenuInflater().inflate(R.menu.activity_endpoint, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_delete_endpoint:
    		new AlertDialog.Builder(this)
    			.setIcon(android.R.drawable.ic_dialog_alert)
    			.setTitle(R.string.delete_endpoint)
    			.setMessage(R.string.confirm_delete_endpoint)
    			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					Agent.stopEndpoint(endpoint);
    					
    					if(Agent.getEndpointManager().remove(EndpointActivity.this.endpoint)) {
    						Toast.makeText(EndpointActivity.this.getApplicationContext(), "Removed Endpoint", Toast.LENGTH_SHORT).show();
    						
    						EndpointActivity.this.finish();
    					}
    					else {
    						Toast.makeText(EndpointActivity.this.getApplicationContext(), "Error removing Endpoint", Toast.LENGTH_SHORT).show();
    					}
    				}
    				
    			})
    			.setNegativeButton(R.string.no, null)
    			.show();
    		return true;
    		
    	case R.id.menu_edit_endpoint:
    		Agent.stopEndpoint(endpoint);
			
    		EndpointDialog dialog = new EndpointDialog(this);
    		dialog.setEndpoint(this.endpoint);
        	dialog.setOnSaveListener(new EndpointDialog.OnSaveListener() {
				
				@Override
				public boolean onSave(Endpoint endpoint) {
					if(Agent.getEndpointManager().update(endpoint)) {
						Toast.makeText(EndpointActivity.this.getApplicationContext(), "Updated Endpoint", Toast.LENGTH_SHORT).show();
						
						return true;
					}
					else {
						Toast.makeText(EndpointActivity.this.getApplicationContext(), "Error updating Endpoint", Toast.LENGTH_SHORT).show();
						
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
