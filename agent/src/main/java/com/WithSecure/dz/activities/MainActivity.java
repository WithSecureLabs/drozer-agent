package com.WithSecure.dz.activities;

import com.WithSecure.dz.Agent;
import com.WithSecure.dz.EndpointAdapter;
import com.WithSecure.dz.R;
import com.WithSecure.dz.views.EndpointListView;
import com.WithSecure.dz.views.ServerListRowView;
import com.WithSecure.jsolar.api.connectors.Endpoint;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {
	
	private EndpointListView endpoint_list_view = null;
	private ServerListRowView server_list_row_view = null;
	
	private void launchEndpointActivity(Endpoint endpoint) {
		Intent intent = new Intent(MainActivity.this, EndpointActivity.class);
		intent.putExtra(Endpoint.ENDPOINT_ID, endpoint.getId());
		
		MainActivity.this.startActivity(intent);
	}
	
	private void launchServerActivity() {
		MainActivity.this.startActivity(new Intent(MainActivity.this, ServerActivity.class));
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        this.endpoint_list_view = (EndpointListView)this.findViewById(R.id.endpoint_list_view);
        this.endpoint_list_view.setAdapter(new EndpointAdapter(this.getApplicationContext(), Agent.getInstance().getEndpointManager(),
        		new EndpointAdapter.OnEndpointSelectListener() {

		        	@Override
					public void onEndpointSelect(Endpoint endpoint) {
						MainActivity.this.launchEndpointActivity(endpoint);
					}
		
					@Override
					public void onEndpointToggle(Endpoint endpoint, boolean isChecked) {
						if(isChecked)
							MainActivity.this.startEndpoint(endpoint);
						else
							MainActivity.this.stopEndpoint(endpoint);
					}
					
		}));
        
        this.server_list_row_view = (ServerListRowView)this.findViewById(R.id.server_list_row_view);
        this.server_list_row_view.setServerParameters(Agent.getInstance().getServerParameters());
        this.server_list_row_view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.this.launchServerActivity();
			}
        	
        });
        this.server_list_row_view.setServerViewListener(new ServerListRowView.OnServerViewListener() {
			
			@Override
			public void onToggle(boolean toggle) {
				if(toggle)
					MainActivity.this.startServer();
				else
					MainActivity.this.stopServer();
			}
			
		});

		// request all unrequested perms in manifest
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
			String[] requestedPermissions = pi.requestedPermissions;

			List<String> toRequest = new ArrayList<>();
			for (String p : requestedPermissions) {
				if (ContextCompat.checkSelfPermission(getApplicationContext(), p) != PackageManager.PERMISSION_GRANTED) {
					if (p.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
							Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
									                   Uri.parse("package:" + getPackageName()));
							startActivity(intent);
						}
					}
					else {
						toRequest.add(p);
					}
				}
			}

			if (!toRequest.isEmpty()) {
				String[] asArray = new String[toRequest.size()];
				toRequest.toArray(asArray);
				ActivityCompat.requestPermissions(this, asArray, 1);
			}
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_refresh:
    		this.updateEndpointStatuses();
    		this.updateServerStatus();
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
    	
    	Agent.getInstance().unbindServices();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Agent.getInstance().bindServices();
    }
    
    private void startServer(){
    	try {
			Agent.getInstance().getServerService().startServer(Agent.getInstance().getServerParameters(), Agent.getInstance().getMessenger());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void stopServer(){
    	try {
			Agent.getInstance().getServerService().stopServer(Agent.getInstance().getServerParameters(), Agent.getInstance().getMessenger());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void startEndpoint(Endpoint endpoint){
    	try {
    		Agent.getInstance().getClientService().startEndpoint(endpoint, Agent.getInstance().getMessenger());
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void stopEndpoint(Endpoint endpoint){
    	try {
    		Agent.getInstance().getClientService().stopEndpoint(endpoint, Agent.getInstance().getMessenger());
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    protected void updateEndpointStatuses() {
    	try {
			for(Endpoint e : Agent.getInstance().getEndpointManager().all())
				e.setStatus(Endpoint.Status.UPDATING);
			
			Agent.getInstance().getClientService().getEndpointStatuses(Agent.getInstance().getMessenger());
		}
		catch(RemoteException e) {
			for(Endpoint e2 : Agent.getInstance().getEndpointManager().all())
				e2.setStatus(Endpoint.Status.UNKNOWN);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
    }
    
    protected void updateServerStatus() {
		try {
			Agent.getInstance().getServerParameters().setStatus(com.WithSecure.jsolar.api.connectors.Server.Status.UPDATING);
			
			Agent.getInstance().getServerService().getServerStatus(Agent.getInstance().getMessenger());
		}
		catch (RemoteException e) {
			Agent.getInstance().getServerParameters().setStatus(Endpoint.Status.UNKNOWN);
			
			Toast.makeText(this, R.string.service_offline, Toast.LENGTH_SHORT).show();
		}
    }
    
}
