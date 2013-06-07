package com.mwr.dz.activities;

import java.lang.ref.WeakReference;

import com.mwr.dz.Agent;
import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Connector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

public abstract class ConnectorActivity extends Activity {
    
    public static class IncomingFingerprintHandler extends Handler {
		
		private final WeakReference<Context> context;
		
		public IncomingFingerprintHandler(Context context) {
			this.context = new WeakReference<Context>(context);
		}
		
		@Override
		public void handleMessage(Message msg) {
			ConnectorActivity context = (ConnectorActivity)this.context.get();
			Bundle data = msg.getData();
			
			if(data.getString(Connector.CONNECTOR_SSL_FINGERPRINT) != null)
				context.receiveFingerprint(data.getString(Connector.CONNECTOR_SSL_FINGERPRINT));
			else
				context.receiveFingerprint(context.getString(R.string.ssl_no_fingerprint));
		}
		
	}
    
    protected Dialog createInformationDialog(int titleId, int messageId) {
    	return new AlertDialog.Builder(this)
    		.setTitle(titleId)
    		.setMessage(messageId)
    		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    		
    			public void onClick(DialogInterface dialog, int id) {}
            
    		})
    		.create();
    }
    
    protected Dialog createInformationDialog(int titleId, String message) {
    	return new AlertDialog.Builder(this)
    		.setTitle(titleId)
    		.setMessage(message)
    		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    		
    			public void onClick(DialogInterface dialog, int id) {}
            
    		})
    		.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_connector, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.show_fingerprint:
			this.showFingerprintDialog();
			return true;
			
		case R.id.refresh_status:
			this.refreshStatus();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
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
	
    public abstract void receiveFingerprint(String fingerprint); 
	protected abstract void refreshStatus();
	protected abstract void showFingerprintDialog();

}
