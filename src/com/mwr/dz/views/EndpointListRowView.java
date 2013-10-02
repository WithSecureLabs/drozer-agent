package com.mwr.dz.views;

import java.util.Observable;
import java.util.Observer;

import com.mwr.dz.EndpointAdapter;
import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class EndpointListRowView extends LinearLayout implements Observer, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
	
	private Endpoint endpoint = null;
	private TextView endpoint_connection_string_field = null;
	private RelativeLayout endpoint_detail_layout = null;
	private TextView endpoint_name_field = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;
	private ToggleButton endpoint_toggle_button = null;
	
	private EndpointAdapter.OnEndpointSelectListener endpoint_listener = null;
	
	private volatile boolean setting_endpoint = false; 
	
	public EndpointListRowView(Context context) {
		super(context);
		
		this.setUpView();
	}

	public EndpointListRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setUpView();
	}
	
	public void setEndpoint(Endpoint endpoint) {
		if(this.endpoint != null)
    		this.endpoint.deleteObserver(this);
		
		this.setting_endpoint = true;
		this.endpoint = endpoint;
		
		this.endpoint_connection_string_field.setText(this.endpoint.toConnectionString());
		this.endpoint_name_field.setText(this.endpoint.getName());
		this.endpoint_status_indicator.setConnector(this.endpoint);
		this.endpoint_toggle_button.setChecked(this.endpoint.isEnabled());
		this.setting_endpoint = false;
		
		this.endpoint.addObserver(this);
	}
	
	public void setEndpointListener(EndpointAdapter.OnEndpointSelectListener endpoint_listener) {
		this.endpoint_listener = endpoint_listener;
	}
	
	private void setUpView() {
		this.addView(View.inflate(this.getContext(), R.layout.list_view_row_endpoint, null));
		
		this.endpoint_connection_string_field = (TextView)this.findViewById(R.id.endpoint_connection_string);
		this.endpoint_detail_layout = (RelativeLayout)this.findViewById(R.id.list_view_row_endpoint);
		this.endpoint_name_field = (TextView)this.findViewById(R.id.endpoint_name);
		this.endpoint_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.endpoint_status_indicator);
		this.endpoint_toggle_button = (ToggleButton)this.findViewById(R.id.endpoint_toggle);
		
		this.endpoint_detail_layout.setOnClickListener(this);
		this.endpoint_toggle_button.setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(!this.setting_endpoint && this.endpoint_listener != null)
			this.endpoint_listener.onEndpointToggle(this.endpoint, isChecked);
	}

	@Override
	public void onClick(View v) {
		if(this.endpoint_listener != null)
			this.endpoint_listener.onEndpointSelect(this.endpoint);
	}

	@Override
	public void update(Observable observable, Object data) {
		this.setEndpoint((Endpoint)observable);
	}

}
