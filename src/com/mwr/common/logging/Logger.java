package com.mwr.common.logging;

import java.util.List;

public interface Logger {

	public List<LogMessage> getLogMessages();
	public void log(LogMessage message);
	public void log(Logger logger, LogMessage message);
	public void setOnLogMessageListener(OnLogMessageListener listener);
	
}
