package com.mwr.droidhg.connector;

import com.mwr.droidhg.api.ConnectorParameters;

public interface Logger {

	public void log(ConnectorParameters connector, String message);
	
}
