package com.mwr.droidhg.agent;

import com.mwr.common.logging.LogMessage;
import com.mwr.common.logging.Logger;
import com.mwr.common.logging.OnLogMessageListener;
import com.mwr.droidhg.agent.views.LogMessageRowView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LogMessageAdapter extends BaseAdapter {
	
	private Context context = null;
	private Logger logger = null;
	
	public LogMessageAdapter(Context context, Logger logger) {
		this.context = context;
		this.logger = logger;
		
		this.logger.setOnLogMessageListener(new OnLogMessageListener() {

			@Override
			public void onLogMessage(Logger logger, LogMessage message) {
				LogMessageAdapter.this.notifyDataSetChanged();
			}
			
		});
	}

	@Override
	public int getCount() {
		return this.logger.getLogMessages().size();
	}

	@Override
	public Object getItem(int pos) {
		return this.logger.getLogMessages().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View copyView, ViewGroup parent) {
		LogMessageRowView view = new LogMessageRowView(this.context);
		
		view.setLogMessage((LogMessage)this.getItem(pos));
		
		return view;
	}

}
