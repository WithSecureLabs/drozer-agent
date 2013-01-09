package com.mwr.droidhg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import com.mwr.common.logging.LogMessage;
import com.mwr.droidhg.agent.ClientService;
import com.mwr.droidhg.agent.ConnectorService;
import com.mwr.droidhg.agent.EndpointManager;
import com.mwr.droidhg.agent.R;
import com.mwr.droidhg.agent.ServerService;
import com.mwr.droidhg.agent.service_connectors.ClientServiceConnection;
import com.mwr.droidhg.agent.service_connectors.ServerServiceConnection;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.ServerParameters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;

public class Agent {

	public static class IncomingHandler extends Handler {

		// private final WeakReference<Context> context;

		public IncomingHandler(Context context) {
			// this.context = new WeakReference<Context>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			// Context context = this.context.get();
			Bundle data = msg.getData();

			switch(msg.what) {
			case ClientService.MSG_GET_ENDPOINT_DETAILED_STATUS:
				getEndpointManager().get(data.getInt("endpoint:id"))
						.setDetailedStatus(data);
				break;

			case ClientService.MSG_GET_ENDPOINTS_STATUS:
				for (Endpoint e : getEndpointManager().all())
					if (data.containsKey("endpoint-" + e.getId()))
						e.setStatus(Endpoint.Status.values()[data.getInt("endpoint-" + e.getId())]);
				break;

			case ServerService.MSG_GET_SERVER_DETAILED_STATUS:
				getServerParameters().setDetailedStatus(data);
				break;

			case ServerService.MSG_GET_SERVER_STATUS:
				getServerParameters().setStatus(ServerParameters.Status.values()[data.getInt("server")]);
				break;

			case ConnectorService.MSG_LOG_MESSAGE:
				if (data.containsKey("endpoint:id"))
					getEndpointManager().get(data.getInt("endpoint:id")).log(LogMessage.fromBundle(data.getBundle("message")));
				else
					getServerParameters().log(LogMessage.fromBundle(data.getBundle("message")));
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	private static Context context = null;

	private static ClientServiceConnection client_service_connection = null;
	private static EndpointManager endpoint_manager = null;
	private static Messenger messenger = null;
	private static ServerParameters server_parameters = null;
	private static ServerServiceConnection server_service_connection = null;
	private static String uid = null;

	public static void bindServices() {
		ClientService.startAndBindToService(context, Agent.getClientService());
		ServerService.startAndBindToService(context, Agent.getServerService());
	}

	public static void createDefaultKeyMaterial() {
		try {
			if(!new File(context.getFilesDir().toString(), "agent.bks").exists())
				copyResourceToFile(R.raw.agent, context.openFileOutput("agent.bks", Context.MODE_PRIVATE));
			if(!new File(context.getFilesDir().toString(), "ca.bks").exists())
				copyResourceToFile(R.raw.ca, context.openFileOutput("ca.bks", Context.MODE_PRIVATE));
		}
		catch(FileNotFoundException e) {
			Log.e("Agent", "Failed to write default key material.");
		}
		catch(IOException e) {
			Log.e("Agent", "Failed to write default key material.");
		}
	}

	private static void copyResourceToFile(int resId, FileOutputStream file) throws IOException {
		InputStream in = context.getResources().openRawResource(resId);

		byte[] buf = new byte[1024];

		int len = in.read(buf);
		while(len != -1) {
			file.write(buf, 0, len);

			len = in.read(buf);
		}
	}

	public static ClientServiceConnection getClientService() {
		if(client_service_connection == null)
			client_service_connection = new ClientServiceConnection();

		return client_service_connection;
	}

	public static Context getContext() {
		return context;
	}

	public static EndpointManager getEndpointManager() {
		if(endpoint_manager == null && context != null)
			endpoint_manager = new EndpointManager(context);
		
		return endpoint_manager;
	}

	public static Messenger getMessenger() {
		if(messenger == null && context != null)
			messenger = new Messenger(new IncomingHandler(context));
		
		return messenger;
	}

	public static ServerParameters getServerParameters() {
		if(server_parameters == null)
			server_parameters = new ServerParameters();
		
		return server_parameters;
	}

	public static ServerServiceConnection getServerService() {
		if(server_service_connection == null)
			server_service_connection = new ServerServiceConnection();

		return server_service_connection;
	}

	public static SharedPreferences getSettings() {
		return context.getSharedPreferences(context.getPackageName()
				+ "_preferences", Context.MODE_MULTI_PROCESS);
	}

	public static String getUID() {
		if (uid == null)
			uid = Settings.Secure.getString(Agent.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

		// sometimes, a device will not have an ANDROID_ID, particularly if we
		// are
		// in lower API versions; in that case we generate one at random
		if (uid == null)
			uid = new BigInteger(64, new SecureRandom()).toString(32);

		return uid;
	}

	public static void setContext(Context context) {
		Agent.context = context.getApplicationContext();
		
		createDefaultKeyMaterial();
	}

	public static void unbindServices() {
		Agent.getClientService().unbind(context);
		Agent.getServerService().unbind(context);
	}

}
