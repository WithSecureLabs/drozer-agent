package com.mwr.dz.views.logger;

import com.mwr.jdiesel.logger.LogMessage;
import com.mwr.jdiesel.logger.Logger;
import com.mwr.jdiesel.logger.OnLogMessageListener;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LogMessageAdapter<T> extends BaseAdapter implements OnLogMessageListener<T> {
	
	private Context context = null;
	private Logger<T> logger = null;
	
	public LogMessageAdapter(Context context, Logger<T> logger) {
		this.context = context;
		this.logger = logger;
		
		this.logger.addOnLogMessageListener(this);
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
	
	@Override
	public void onLogMessage(Logger<T> logger, LogMessage message) {
		this.notifyDataSetChanged();
	}

}
