package com.mwr.droidhg.agent;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.EndpointListView;
import com.mwr.droidhg.agent.views.ServerListRowView;
import com.mwr.droidhg.api.Endpoint;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	
	private EndpointListView endpoint_list_view = null;
	private ServerListRowView server_list_row_view = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Agent.setContext(this.getApplicationContext());
        
        this.endpoint_list_view = (EndpointListView)this.findViewById(R.id.endpoint_list_view);
        this.server_list_row_view = (ServerListRowView)this.findViewById(R.id.server_list_row_view);
        
        this.endpoint_list_view.setAdapter(new EndpointAdapter(this.getApplicationContext(), Agent.getEndpointManager()));
        this.endpoint_list_view.setOnEndpointSelectListener(new EndpointListView.OnEndpointSelectListener() {
			
			@Override
			public void onEndpointSelect(Endpoint endpoint) {
				Intent intent = new Intent(MainActivity.this, EndpointActivity.class);
				
				intent.putExtra("endpoint_id", endpoint.getId());
				
				MainActivity.this.startActivity(intent);
			}
			
		});
        
        this.server_list_row_view.setServerParameters(Agent.getServerParameters());
        
        this.server_list_row_view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, ServerActivity.class));
			}
        	
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_refresh:
    		Agent.updateEndpointStatuses();
    		Agent.updateServerStatus();
    		return true;
    	
    	case R.id.menu_settings:
    		this.startActivity(new Intent(this, SettingsActivity.class));
    		return true;
    	
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
    
}
 