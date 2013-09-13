package com.mwr.dz;

import com.mwr.dz.models.EndpointManager;
import com.mwr.dz.views.EndpointListRowView;
import com.mwr.dz.views.EndpointListView;
import com.mwr.jdiesel.api.connectors.Endpoint;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class EndpointAdapter extends BaseAdapter implements OnClickListener, OnCheckedChangeListener{
	
	private Context context = null;
	private EndpointManager endpoint_manager = null;
	private EndpointListView view = null;
	
	public EndpointAdapter(Context context, EndpointManager endpoint_manager, EndpointListView view) {
		this.context = context;
		this.endpoint_manager = endpoint_manager;
		this.view = view;
		
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
		
		ToggleButton toggle = (ToggleButton)view.findViewById(R.id.endpoint_toggle);
		RelativeLayout description = (RelativeLayout)view.findViewById(R.id.list_view_row_endpoint);
		
		toggle.setOnCheckedChangeListener(this);
		description.setOnClickListener(this);
		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int pos = this.view.getPositionForView((View) buttonView.getParent());
		this.view.onToggle(this.view, pos, isChecked);
		
	}

	@Override
	public void onClick(View v) {
		int pos = this.view.getPositionForView(v);
		this.view.onItemClick(this.view, v, pos, -1);
		
	}
	
}
