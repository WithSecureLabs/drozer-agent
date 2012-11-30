package com.mwr.droidhg.reflection;

import android.util.SparseArray;

public class ObjectStore {
	
	private SparseArray<Object> objects = new SparseArray<Object>();

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
