package com.mwr.dz.views;

import java.util.Observable;
import java.util.Observer;

import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EndpointListRowView extends LinearLayout implements Observer {
	
	private Endpoint endpoint = null;
	private TextView endpoint_connection_string_field = null;
	private TextView endpoint_name_field = null;
	private ConnectorStatusIndicator endpoint_status_indicator = null;
	
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
		
		this.endpoint = endpoint;
		
		this.endpoint_connection_string_field.setText(this.endpoint.toConnectionString());
		this.endpoint_name_field.setText(this.endpoint.getName());
		this.endpoint_status_indicator.setConnector(this.endpoint);
		
		this.endpoint.addObserver(this);
	}
	
	private void setUpView() {
		this.addView(View.inflate(this.getContext(), R.layout.list_view_row_endpoint, null));
		
		this.endpoint_connection_string_field = (TextView)this.findViewById(R.id.endpoint_connection_string);
		this.endpoint_name_field = (TextView)this.findViewById(R.id.endpoint_name);
		this.endpoint_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.endpoint_status_indicator);
	}

	@Override
	public void update(Observable observable, Object data) {
		this.setEndpoint((Endpoint)observable);
	}

}
