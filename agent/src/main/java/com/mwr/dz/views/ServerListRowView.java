package com.mwr.dz.views;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Server;
import com.mwr.jdiesel.api.connectors.Server.OnChangeListener;

public class ServerListRowView extends LinearLayout implements Observer, OnCheckedChangeListener {
	
	public interface OnServerViewListener {
		
		public void onToggle(boolean toggle);
		
	}
	
	private TextView adb_server_port_field = null;
	private ConnectorStatusIndicator adb_server_status_indicator = null;
	private ToggleButton adb_server_toggle_button = null;
	private Server server_parameters = null;
	
	private OnServerViewListener server_view_listener;
	
	private volatile boolean setting_server = false;
	
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
		this.adb_server_toggle_button = (ToggleButton)this.findViewById(R.id.adb_server_toggle);
		
		this.adb_server_toggle_button.setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(!this.setting_server)
			this.server_view_listener.onToggle(isChecked);
	}
	
	public void setServerParameters(Server server_parameters) {
		this.setting_server = true;
		this.server_parameters = server_parameters;
		
		this.adb_server_port_field.setText(Integer.valueOf(this.server_parameters.getPort()).toString());
		this.adb_server_status_indicator.setConnector(this.server_parameters);
		this.adb_server_toggle_button.setChecked(this.server_parameters.isEnabled());
		this.setting_server = false;
		
		this.server_parameters.setOnChangeListener(new OnChangeListener() {

			@Override
			public void onChange(Server parameters) {
				ServerListRowView.this.setServerParameters(parameters);
			}
			
		});
		this.server_parameters.addObserver(this);
	}
	
	public void setServerViewListener(OnServerViewListener listener) {
		this.server_view_listener = listener;
	}

	@Override
	public void update(Observable observable, Object data) {
		this.setServerParameters((Server)observable);
	}
	
}
