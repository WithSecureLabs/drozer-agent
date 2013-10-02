package com.mwr.dz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;

import com.mwr.dz.R;
import com.mwr.dz.models.EndpointManager;
import com.mwr.dz.models.ServerSettings;
import com.mwr.dz.service_connectors.ClientServiceConnection;
import com.mwr.dz.service_connectors.IncomingReplyHandler;
import com.mwr.dz.service_connectors.ServerServiceConnection;
import com.mwr.dz.services.ClientService;
import com.mwr.dz.services.ServerService;
import com.mwr.jdiesel.api.DeviceInfo;
import com.mwr.jdiesel.api.connectors.Server;

public class Agent {
	
	private static String[] DEFAULT_UIDS = new String[]{
		"9774d56d682e549c",
		"0000000000000000" };
	
	private static final Agent INSTANCE = new Agent();
	
	public static final String DEFAULT_KEYSTORE = "agent.bks";
	public static final String DEFAULT_TRUSTSTORE = "ca.bks";

	private static final String AGENT_ID = "agent:uid";
	
	public static final String TAG = "agent";

	private ClientServiceConnection client_service_connection = null;
	private Context context = null;
	private EndpointManager endpoint_manager = null;
	private Messenger messenger = null;
	private Server server_parameters = null;
	private ServerServiceConnection server_service_connection = null;
	private String uid = null;
	
	private Agent() {}
	
	public static Context getContext() {
		return Agent.getInstance().getMercuryContext();
	}
	
	public static Agent getInstance() {
		return INSTANCE;
	}
	
	private static boolean isDefaultUID(String uid) {
		if(uid == null)
			return false;
		
		for(String default_uid : Agent.DEFAULT_UIDS) {
			if(uid.equals(default_uid))
				return true;
		}
		
		return false;
	}

	public void bindServices() {
		ClientService.startAndBindToService(this.context, this.getClientService());
		ServerService.startAndBindToService(this.context, this.getServerService());
	}

	public void createDefaultKeyMaterial() {
		try {
			if(!new File(this.context.getFilesDir().toString(), DEFAULT_KEYSTORE).exists())
				copyResourceToFile(R.raw.agent, this.context.openFileOutput(DEFAULT_KEYSTORE, Context.MODE_PRIVATE));
			if(!new File(this.context.getFilesDir().toString(), DEFAULT_TRUSTSTORE).exists())
				copyResourceToFile(R.raw.ca, this.context.openFileOutput(DEFAULT_TRUSTSTORE, Context.MODE_PRIVATE));
		}
		catch(IOException e) {
			Log.e(TAG, "Failed to write default key material.");
		}
	}
	
	private String createRandomUID(){
		return new BigInteger(64, new SecureRandom()).toString(32);
	}

	private void copyResourceToFile(int resId, FileOutputStream file) throws IOException {
		InputStream in = this.context.getResources().openRawResource(resId);

		byte[] buf = new byte[1024];

		int len = in.read(buf);
		while(len != -1) {
			file.write(buf, 0, len);

			len = in.read(buf);
		}
	}

	public ClientServiceConnection getClientService() {
		if(this.client_service_connection == null)
			this.client_service_connection = new ClientServiceConnection();

		return this.client_service_connection;
	}
	
	private String getCustomUID() {
		return this.getSettings().getString(Agent.AGENT_ID, null);
	}
	
	public DeviceInfo getDeviceInfo() {
		return new DeviceInfo(this.getUID(),
				android.os.Build.MANUFACTURER,
				android.os.Build.MODEL,
				android.os.Build.VERSION.RELEASE);
	}

	public Context getMercuryContext() {
		return this.context;
	}

	public EndpointManager getEndpointManager() {
		if(this.endpoint_manager == null && this.context != null)
			this.endpoint_manager = new EndpointManager(this.context);
		
		return this.endpoint_manager;
	}

	public Messenger getMessenger() {
		if(this.messenger == null)
			this.messenger = new Messenger(new IncomingReplyHandler(Agent.getInstance()));
		
		return this.messenger;
	}

	public Server getServerParameters() {
		if(this.server_parameters == null) {
			this.server_parameters = new Server();
			
			(new ServerSettings()).load(this.server_parameters);
		}
		
		return this.server_parameters;
	}

	public ServerServiceConnection getServerService() {
		if(this.server_service_connection == null)
			this.server_service_connection = new ServerServiceConnection();

		return this.server_service_connection;
	}

	public SharedPreferences getSettings() {
		return this.context.getSharedPreferences(this.context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}

	public String getUID() {
		this.uid = this.getCustomUID();
		// if the UID is set in preferences, return it immediately
		if(this.uid != null && !this.uid.equals(""))
			return this.uid;
		
		// otherwise, try to read the ANDROID_ID from Settings.Secure
		this.uid = Settings.Secure.getString(this.getMercuryContext().getContentResolver(), Settings.Secure.ANDROID_ID);
		// sometimes, a device will not have an ANDROID_ID, particularly if we
		// are in lower API versions; in that case we generate one at random
		if(this.uid == null || Agent.isDefaultUID(this.uid))
			this.uid = this.createRandomUID();
		// store whatever UID we have created in the Preferences
		Editor edit = this.getSettings().edit();
		edit.putString(Agent.AGENT_ID, this.uid);
		edit.commit();
		
		return this.uid;
	}

	public void setContext(Context context) {
		this.context = context.getApplicationContext();
		
		this.createDefaultKeyMaterial();
	}

	public void unbindServices() {
		this.getClientService().unbind(this.context);
		this.getServerService().unbind(this.context);
	}

}
