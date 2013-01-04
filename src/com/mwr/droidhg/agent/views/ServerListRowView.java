package com.mwr.droidhg.agent.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mwr.droidhg.agent.R;
import com.mwr.droidhg.api.ServerParameters;
import com.mwr.droidhg.api.ServerParameters.OnChangeListener;

public class ServerListRowView extends LinearLayout {
	
	private TextView adb_server_port_field = null;
	private ConnectorStatusIndicator adb_server_status_indicator = null;
	private ServerParameters server_parameters = null;
	
	public ServerListRowView(Context context) {
		super(context);
		
		this.initView();
	}

	public ServerListRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.initView();
	}
	
	private void initView() {
		this.addView(View.inflate(this.getContext(), R.layout.list_view_row_server, null));

        this.setBackgroundResource(android.R.drawable.list_selector_background);
		
		this.adb_server_port_field = (TextView)this.findViewById(R.id.adb_server_port);
		this.adb_server_status_indicator = (ConnectorStatusIndicator)this.findViewById(R.id.adb_server_status_indicator);
	}
	
	public void setServerParameters(ServerParameters server_parameters) {
		this.server_parameters = server_parameters;
		
		this.server_parameters.setOnChangeListener(new OnChangeListener() {

			@Override
			public void onChange(ServerParameters parameters) {
				ServerListRowView.this.adb_server_port_field.setText(Integer.valueOf(parameters.getPort()).toString());
			}
			
		});
		
		this.adb_server_port_field.setText(Integer.valueOf(this.server_parameters.getPort()).toString());
		this.adb_server_status_indicator.setConnector(this.server_parameters);
	}
	
}
