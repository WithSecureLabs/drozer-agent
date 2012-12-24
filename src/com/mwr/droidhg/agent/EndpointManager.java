package com.mwr.droidhg.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.mwr.droidhg.api.ConnectorParameters.Status;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.Endpoint.EndpointSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EndpointManager extends SQLiteOpenHelper {
	
	// 1 -> 2 add ssl field to endpoints
	private static final int DATABASE_VERSION = 2;
	
	public interface OnDatasetChangeListener {
		
		public void onDatasetChange(EndpointManager manager);
		
	}
	
	public interface OnEndpointStatusChangeListener {
		
		public void onEndpointStarted(Endpoint endpoint);
		public void onEndpointStopped(Endpoint endpoint);
		public void onEndpointStatusChanged(Endpoint endpoint);
		
	}
	
	private class SQLiteSerializer implements EndpointSerializer {

		@Override
		public Endpoint deserialize(Object ser) {
			Cursor cur = (Cursor)ser;
			
			return new Endpoint(cur.getInt(0), cur.getString(1), cur.getString(2), cur.getInt(3), cur.getInt(4) == 1);
		}

		@Override
		public Object serialize(Endpoint endpoint) {
			ContentValues cv = new ContentValues(3);
			
			cv.put("name", endpoint.getName());
			cv.put("host", endpoint.getHost());
			cv.put("port", endpoint.getPort());
			cv.put("ssl", endpoint.isSSL() ? 1 : 0);
			
			return cv;
		}
		
	}
	
	private static final String DB_FILE_NAME = "droidhg.db";
	private static final String TABLE_NAME = "endpoints";
	
	private ArrayList<Endpoint> endpoints = null;
	private OnDatasetChangeListener on_dataset_change_listener = null;
	private OnEndpointStatusChangeListener on_endpoint_status_change_listener = null;
	private final SQLiteSerializer serializer = new SQLiteSerializer();
	
	public EndpointManager(Context context) {
		super(context, DB_FILE_NAME, null, DATABASE_VERSION);
	}
	
	public int activeSize() {
		int ctr = 0;
		
		for(Endpoint e : this.all()) {
			if(e.getStatus() == Endpoint.Status.ONLINE)
				ctr++;
		}
		
		return ctr;
	}
	
	public boolean add(Endpoint endpoint) {
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_NAME, null, (ContentValues)endpoint.serialize(this.serializer));
		db.close();
		
		if(id != -1) {
			endpoint.setId((int)id);
			this.endpoints.add(endpoint);
			
			if(this.on_dataset_change_listener != null)
				this.on_dataset_change_listener.onDatasetChange(this);
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public List<Endpoint> all() {
		if(this.endpoints == null) {
			this.endpoints = new ArrayList<Endpoint>();
			
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(TABLE_NAME, null, null, null, null, null, null, null);
			
			for(boolean has_item = cur.moveToFirst(); has_item; has_item = cur.moveToNext()) {
				Endpoint e = Endpoint.deserialize(this.serializer, cur);
				e.addObserver(new Observer() {

					@Override
					public void update(Observable observable, Object data) {
						Endpoint endpoint = (Endpoint)observable;
						
						if(EndpointManager.this.on_endpoint_status_change_listener != null) {
							if(endpoint.getStatus() == Endpoint.Status.ONLINE) {
								EndpointManager.this.on_endpoint_status_change_listener.onEndpointStarted(endpoint);
							}
							else if(endpoint.getStatus() == Endpoint.Status.OFFLINE) {
								EndpointManager.this.on_endpoint_status_change_listener.onEndpointStopped(endpoint);
							}
							
							EndpointManager.this.on_endpoint_status_change_listener.onEndpointStatusChanged(endpoint);
						}
					}
					
				});
			
				this.endpoints.add(e);
			}
			
			cur.close();
			db.close();
		}
		
		return this.endpoints;
	}
	
	public boolean anyActive() {
		for(Endpoint e : this.all()) {
			if(e.getStatus() == Endpoint.Status.ONLINE)
				return true;
		}
		
		return false;
	}
	
	public Endpoint get(int id) {
		for(Endpoint endpoint : this.all()) {
			if(endpoint.getId() == id)
				return endpoint;
		}
		
		// if we get here, we haven't found the Endpoint in the cached data - it may be new
		return this.getFromDatabase(id);
	}
	
	public Endpoint get(int id, boolean reload) {
		Endpoint endpoint = this.get(id);
		
		if(endpoint != null && reload) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cur = db.query(TABLE_NAME, null, "id=?", new String[] { Integer.valueOf(endpoint.getId()).toString() }, null, null, null);
			
			Endpoint fresh = null;
			
			if(cur.moveToFirst())
				fresh = Endpoint.deserialize(this.serializer, cur);
			
			cur.close();
			db.close();
			
			if(fresh != null)
				endpoint.setAttributes(fresh);
			// TODO: raise an exception if fresh does not exist
		}
		
		return endpoint;
	}
	
	private Endpoint getFromDatabase(int id) {
		Endpoint endpoint = null;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(TABLE_NAME, null, "id=?", new String[] { Integer.valueOf(id).toString() }, null, null, null, null);
		
		for(boolean has_item = cur.moveToFirst(); has_item; has_item = cur.moveToNext()) {
			endpoint = Endpoint.deserialize(this.serializer, cur);
			endpoint.addObserver(new Observer() {

				@Override
				public void update(Observable observable, Object data) {
					Endpoint endpoint = (Endpoint)observable;
					
					if(EndpointManager.this.on_endpoint_status_change_listener != null) {
						if(endpoint.getStatus() == Endpoint.Status.ONLINE) {
							EndpointManager.this.on_endpoint_status_change_listener.onEndpointStarted(endpoint);
						}
						else if(endpoint.getStatus() == Endpoint.Status.OFFLINE) {
							EndpointManager.this.on_endpoint_status_change_listener.onEndpointStopped(endpoint);
						}
						
						EndpointManager.this.on_endpoint_status_change_listener.onEndpointStatusChanged(endpoint);
					}
				}
				
			});
			
			// update the status to UNKNOWN, but probably offline
			endpoint.setStatus(Status.OFFLINE);
			// finally, add the new endpoint to the collection
			this.endpoints.add(endpoint);
		}
		
		cur.close();
		db.close();
		
		return endpoint;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				"id integer primary key autoincrement," +
				"name varchar(255) not null," +
				"host varchar(255) not null,"+
				"port integer not null,+" +
				"ssl integer not null)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("EndpointManager", "Upgrading database from version " + oldVersion + " to " + newVersion);
		
		if(oldVersion <= 1)
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD ssl integer default 0 not null");
	}
	
	public boolean remove(Endpoint endpoint) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rows = db.delete(TABLE_NAME, "id=?", new String[] { Integer.valueOf(endpoint.getId()).toString() });
		db.close();
		
		if(rows == 1) {
			this.all().remove(endpoint);
			
			if(this.on_dataset_change_listener != null)
				this.on_dataset_change_listener.onDatasetChange(this);
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setOnDatasetChangeListener(OnDatasetChangeListener listener) {
		this.on_dataset_change_listener = listener;
	}
	
	public void setOnEndpointStatusChangeListener(OnEndpointStatusChangeListener listener) {
		this.on_endpoint_status_change_listener = listener;
	}
	
	public int size() {
		return this.all().size();
	}
	
	public boolean update(Endpoint endpoint) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rows = db.update(TABLE_NAME, (ContentValues)endpoint.serialize(this.serializer), "id=?", new String[] { Integer.valueOf(endpoint.getId()).toString() });
		db.close();
		
		if(rows == 1) {
			this.get(endpoint.getId()).setAttributes(endpoint);
			
			if(this.on_dataset_change_listener != null)
				this.on_dataset_change_listener.onDatasetChange(this);
			
			return true;
		}
		else {
			return false;
		}
	}

	
}
