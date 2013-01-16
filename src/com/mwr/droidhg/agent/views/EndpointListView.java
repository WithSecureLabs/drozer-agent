package com.mwr.droidhg.agent.views;

import com.mwr.droidhg.connector.Endpoint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class EndpointListView extends ListView implements AdapterView.OnItemClickListener {
	
	public interface OnEndpointSelectListener {
		
		public void onEndpointSelect(Endpoint endpoint);
		
	}
	
	private OnEndpointSelectListener on_endpoint_select_listener = null;
	
	public EndpointListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Endpoint endpoint = (Endpoint)parent.getItemAtPosition(position);
		
		if(this.on_endpoint_select_listener != null)
			this.on_endpoint_select_listener.onEndpointSelect(endpoint);
	}
	
	public void setOnEndpointSelectListener(OnEndpointSelectListener listener) {
		this.on_endpoint_select_listener = listener;
	}

}
