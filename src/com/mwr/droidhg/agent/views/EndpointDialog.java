package com.mwr.droidhg.agent.views;

import com.mwr.droidhg.agent.R;
import com.mwr.droidhg.api.Endpoint;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EndpointDialog extends Builder {
	
	public interface OnSaveListener {
		
		public boolean onSave(Endpoint endpoint);
		
	}
	
	private Endpoint endpoint = null;
	private EditText host_field = null;
	private EditText name_field = null;
	private OnSaveListener on_save_listener = null;
	private EditText port_field = null;

	public EndpointDialog(Context context) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_endpoint, null);
		
		this.setView(layout);
		
		this.host_field = (EditText)layout.findViewById(R.id.endpoint_host);
		this.name_field = (EditText)layout.findViewById(R.id.endpoint_name);
		this.port_field = (EditText)layout.findViewById(R.id.endpoint_port);
		
		this.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(EndpointDialog.this.on_save_listener != null) {
					if(EndpointDialog.this.on_save_listener.onSave(EndpointDialog.this.toEndpoint()))
						dialog.dismiss();
				}
				else {
					dialog.dismiss();
				}
			}
			
		});
		this.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
	}
	
	public String getHost() {
		return this.host_field.getText().toString();
	}
	
	public String getName() {
		return this.name_field.getText().toString();
	}
	
	public int getPort() {
		return Integer.parseInt(this.port_field.getText().toString());
	}
	
	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
		
		this.setTitle(endpoint.isNew() ? R.string.new_endpoint : R.string.edit_endpoint);
		
		this.setHost(endpoint.getHost());
		this.setName(endpoint.getName());
		this.setPort(endpoint.getPort());
	}
	
	protected void setHost(String host) {
		this.host_field.setText(host);
	}
	
	protected void setName(String name) {
		this.name_field.setText(name);
	}
	
	public void setOnSaveListener(OnSaveListener listener) {
		this.on_save_listener = listener;
	}
	
	protected void setPort(int port) {
		this.port_field.setText(Integer.valueOf(port).toString());
	}
	
	public Endpoint toEndpoint() {
		return new Endpoint(this.endpoint.getId(), this.getName(), this.getHost(), this.getPort());
	}

}
