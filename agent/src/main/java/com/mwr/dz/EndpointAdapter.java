package com.mwr.dz;

import com.mwr.dz.models.EndpointManager;
import com.mwr.dz.views.EndpointListRowView;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class EndpointAdapter extends BaseAdapter {
	
	public interface OnEndpointSelectListener {
		
		public void onEndpointSelect(Endpoint endpoint);
		public void onEndpointToggle(Endpoint endpoint, boolean toggle);
		
	}
	
	private Context context = null;
	private EndpointManager endpoint_manager = null;
	private OnEndpointSelectListener endpoint_listener = null;
	
	public EndpointAdapter(Context context, EndpointManager endpoint_manager, OnEndpointSelectListener endpoint_listener) {
		this.context = context;
		this.endpoint_listener = endpoint_listener;
		this.endpoint_manager = endpoint_manager;
		
		this.endpoint_manager.setOnDatasetChangeListener(new EndpointManager.OnDatasetChangeListener() {
			
			@Override
			public void onDatasetChange(EndpointManager manager) {
				EndpointAdapter.this.notifyDataSetChanged();
			}
			
		});
	}

	@Override
	public int getCount() {
		return this.endpoint_manager.size();
	}

	@Override
	public Object getItem(int pos) {
		return this.endpoint_manager.all().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View copyView, ViewGroup parent) {
		EndpointListRowView view = new EndpointListRowView(this.context);
		view.setEndpoint((Endpoint)this.getItem(pos));
		view.setEndpointListener(this.endpoint_listener);
		
		return view;
	}
	
}
