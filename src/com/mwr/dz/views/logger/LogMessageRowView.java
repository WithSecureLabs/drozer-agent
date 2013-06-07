package com.mwr.dz.views.logger;

import com.mwr.dz.R;
import com.mwr.jdiesel.logger.LogMessage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogMessageRowView extends LinearLayout {
	
	private LogMessage message = null;
	private TextView message_label = null;
	private TextView message_message = null;
	
	public LogMessageRowView(Context context) {
		super(context);
		
		this.setUpView();
	}

	public LogMessageRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setUpView();
	}
	
	private void setLevel(int level) {
		switch(level) {
		case LogMessage.ASSERT:
			this.message_label.setText(R.string.log_level_tag_assert);
			this.message_label.setBackgroundColor(0xffff0000);
			this.message_label.setTextColor(0xffffffff);
			break;
			
		case LogMessage.DEBUG:
			this.message_label.setText(R.string.log_level_tag_debug);
			this.message_label.setBackgroundColor(0xff00ff00);
			this.message_label.setTextColor(0xff000000);
			break;
		
		case LogMessage.ERROR:
			this.message_label.setText(R.string.log_level_tag_error);
			this.message_label.setBackgroundColor(0xffff0000);
			this.message_label.setTextColor(0xffffffff);
			break;
			
		case LogMessage.INFO:
			this.message_label.setText(R.string.log_level_tag_info);
			break;
			
		case LogMessage.VERBOSE:
			this.message_label.setText(R.string.log_level_tag_verbose);
			this.message_label.setBackgroundColor(0xff00ff00);
			this.message_label.setTextColor(0xff000000);
			break;
			
		case LogMessage.WARN:
			this.message_label.setText(R.string.log_level_tag_warn);
			this.message_label.setBackgroundColor(0xffffa500);
			this.message_label.setTextColor(0xff000000);
			break;
			
		default:
			this.message_label.setText(R.string.log_level_tag_unknown);
			break;
		}
	}
	
	public void setLogMessage(LogMessage message) {
		this.message = message;
		
		this.setLevel(this.message.getLevel());
		this.message_message.setText(this.message.getMessage());
	}
	
	private void setUpView() {
		this.addView(View.inflate(this.getContext(), R.layout.list_view_row_log_message, null));
		
		this.message_label = (TextView)this.findViewById(R.id.log_message_level);
		this.message_message = (TextView)this.findViewById(R.id.log_message_message);
	}

}
