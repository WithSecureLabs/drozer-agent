package com.mwr.droidhg.agent.views;

import com.mwr.droidhg.agent.R;
import com.mwr.droidhg.api.ServerParameters;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ServerParametersDialog extends Builder {
	
	public interface OnSaveListener {
		
		public boolean onSave(ServerParameters parameters);
		
	}
	
	private OnSaveListener on_save_listener = null;
	private EditText port_field = null;

	public ServerParametersDialog(Context context) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_server, null);
		
		this.setView(layout);

		this.port_field = (EditText)layout.findViewById(R.id.server_port);
		
		this.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(ServerParametersDialog.this.on_save_listener != null) {
					if(ServerParametersDialog.this.on_save_listener.onSave(ServerParametersDialog.this.toServerParameters()))
						dialog.dismiss();
				}
				else {
					dialog.dismiss();
				}
			}
			
		});
		this.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
	}
	
	public int getPort() {
		return Integer.parseInt(this.port_field.getText().toString());
	}
	
	public void setParameters(ServerParameters parameters) {
		this.setTitle(R.string.edit_server_parameters);

		this.setPort(parameters.getPort());
	}
	
	public void setOnSaveListener(OnSaveListener listener) {
		this.on_save_listener = listener;
	}
	
	protected void setPort(int port) {
		this.port_field.setText(Integer.valueOf(port).toString());
	}
	
	public ServerParameters toServerParameters() {
		return new ServerParameters(this.getPort());
	}

}
