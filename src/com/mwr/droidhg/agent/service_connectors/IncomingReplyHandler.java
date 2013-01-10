package com.mwr.droidhg.agent.service_connectors;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.ClientService;
import com.mwr.droidhg.agent.ConnectorService;
import com.mwr.droidhg.agent.ServerService;
import com.mwr.droidhg.api.ConnectorParameters;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.ServerParameters;

public class IncomingReplyHandler extends Handler {
	
	private final WeakReference<Agent> agent;

	public IncomingReplyHandler(Agent agent) {
		 this.agent = new WeakReference<Agent>(agent);
	}

	@Override
	public void handleMessage(Message msg) {
		Agent agent = this.agent.get();
		Bundle data = msg.getData();

		switch(msg.what) {
		case ClientService.MSG_GET_ENDPOINT_DETAILED_STATUS:
			agent.getEndpointManager().get(data.getInt(Endpoint.ENDPOINT_ID)).setDetailedStatus(data);
			break;

		case ClientService.MSG_GET_ENDPOINTS_STATUS:
			for(Endpoint e : agent.getEndpointManager().all())
				if(data.containsKey("endpoint-" + e.getId()))
					e.setStatus(Endpoint.Status.values()[data.getInt("endpoint-" + e.getId())]);
			break;

		case ServerService.MSG_GET_SERVER_DETAILED_STATUS:
			agent.getServerParameters().setDetailedStatus(data);
			break;

		case ServerService.MSG_GET_SERVER_STATUS:
			agent.getServerParameters().setStatus(ServerParameters.Status.values()[data.getInt("server")]);
			break;

		case ConnectorService.MSG_LOG_MESSAGE:
			if (data.containsKey(Endpoint.ENDPOINT_ID))
				agent.getEndpointManager().get(data.getInt(Endpoint.ENDPOINT_ID)).log(LogMessage.fromBundle(data.getBundle(ConnectorParameters.CONNECTOR_LOG_MESSAGE)));
			else
				agent.getServerParameters().log(LogMessage.fromBundle(data.getBundle(ConnectorParameters.CONNECTOR_LOG_MESSAGE)));
			break;

		default:
			super.handleMessage(msg);
		}
	}

}
