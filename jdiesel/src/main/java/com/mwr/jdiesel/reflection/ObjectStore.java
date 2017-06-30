package com.mwr.jdiesel.reflection;

import java.util.List;

import android.util.SparseArray;

public class ObjectStore {
	
	private SparseArray<Object> objects = new SparseArray<Object>();
	
	public ObjectStore() {}
	
	public ObjectStore(List<Object> objects) {
		for(Object obj : objects)
			this.objects.put(obj.hashCode(), obj);
	}

	public void clear() {
		this.objects.clear();
	}
	
	public Object get(int ref) {
		return this.objects.get(ref);
	}
	
	public int put(Object object) {
		try {
			this.objects.put(object.hashCode(), object);
			
			if(object.getClass().isArray()) {
				for(Object o : (Object[])object)
					this.put(o);
			}
			
			return object.hashCode();
		}
		catch(ClassCastException e) {
			return 0;
		}
	}
	
	public void remove(int ref) {
		this.objects.remove(ref);
	}
	
	public int size() {
		return this.objects.size();
	}
	
}
