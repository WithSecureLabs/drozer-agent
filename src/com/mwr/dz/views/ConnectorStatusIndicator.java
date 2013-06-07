package com.mwr.dz.views;

import java.util.Observable;
import java.util.Observer;

import com.mwr.dz.R;
import com.mwr.jdiesel.api.connectors.Connector;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ConnectorStatusIndicator extends LinearLayout implements Observer {
	
	private AnimationDrawable animation = null;
	private Connector connector_parameters = null;
	private ImageView status_image = null;
	
	public ConnectorStatusIndicator(Context context) {
		super(context);
		
		this.setUpView();
	}

	public ConnectorStatusIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setUpView();
	}
	
	public void setConnector(Connector connector_parameters) {
		if(this.connector_parameters != null)
			this.connector_parameters.deleteObserver(this);
		
		this.connector_parameters = connector_parameters;
		
		this.connector_parameters.addObserver(this);
	}
	
	private void setUpView() {
		this.animation = (AnimationDrawable)getResources().getDrawable(R.drawable.ic_stat_connecting);
		
		this.status_image = new ImageView(this.getContext());
		this.addView(this.status_image);
		
		this.setGravity(Gravity.CENTER_VERTICAL);
	}

	@Override
	public void update(Observable observable, Object data) {
		Connector connector_parameters = (Connector)observable;
		
		switch(connector_parameters.getStatus()) {
		case ACTIVE:
			this.status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_active));
			break;
			
		case CONNECTING:
			this.status_image.setImageDrawable(this.animation);
			
			this.status_image.post(new Runnable() {

				@Override
				public void run() {
					if(animation.isRunning())
						animation.stop();
					
					animation.start();
				}
				
			});
			break;
			
		case OFFLINE:
			this.status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_offline));
			break;
			
		case ONLINE:
			this.status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_online));
			break;
			
		case UNKNOWN:
			this.status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_unknown));
			break;
			
		case UPDATING:
			this.status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_unknown));
			break;
		}
	}

}
