package com.mwr.droidhg.agent.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.agent.R;

public class LogMessageRowView extends LinearLayout {
	
	private LogMessage message = null;
	private TextView message_message = null;
	
	public LogMessageRowView(Context context) {
		super(context);
		
		this.setUpView();
	}

	public LogMessageRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setUpView();
	}
	
	public void setLogMessage(LogMessage message) {
		this.message = message;
		
		this.message_message.setText(this.message.getMessage());
	}
	
	private void setUpView() {
		this.addView(View.inflate(this.getContext(), R.layout.list_view_row_log_message, null));
		
		this.message_message = (TextView)this.findViewById(R.id.log_message_message);
	}

}
