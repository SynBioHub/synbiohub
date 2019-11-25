package org.oboparser.obo;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class OBOStanza {

	private LinkedHashMap<String,LinkedList<OBOValue>> keyValues;
	private String type;
	private String id;
	
	public OBOStanza(String t) { 
		type = t;
		keyValues = new LinkedHashMap<String,LinkedList<OBOValue>>();
		id = null;
	}
	
	@Override
	public Object clone() {
		return clone(OBOStanza.class);
	}
	
	public Object clone(Class<? extends OBOStanza> cls) {
		try {
			Constructor<? extends OBOStanza> constructor = cls.getConstructor();
			OBOStanza s = constructor.newInstance();
			s.id = id;
			for(String key : keyValues.keySet()) { 
				s.keyValues.put(key, new LinkedList<OBOValue>(keyValues.get(key))); 
			}
			return s;

		} catch (InstantiationException e) {
			throw new IllegalArgumentException(cls.getCanonicalName(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(cls.getCanonicalName(), e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(cls.getCanonicalName(), e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(cls.getCanonicalName(), e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(cls.getCanonicalName(), e);
		}
	}
	
	public String getType() { return type; }
	public Set<String> keys() { return keyValues.keySet(); }
	public List<OBOValue> values(String key) { return keyValues.get(key); }
	public boolean hasValue(String key) { return keyValues.containsKey(key); }
	
	public void add(OBOStanza s) { 
		if(id != null && s.getId() != null && !s.getId().equals(id)) { 
			throw new IllegalArgumentException(String.format("%s != %s", id, s.getId()));
		}
		
		for(String key : s.keyValues.keySet()) {
			if(!keyValues.containsKey(key)) { 
				keyValues.put(key, new LinkedList<OBOValue>());
			}
			List<OBOValue> myValues = keyValues.get(key);
			for(OBOValue theirValue : s.keyValues.get(key)) { 
				if(!myValues.contains(theirValue)) { 
					myValues.add(theirValue);
				}
			}
		}
	}
	
	protected void addValue(String k, OBOValue v) {
		if(k.equals("id")) {
			if(keyValues.containsKey(k)) { 
				throw new OBOException(String.format("Duplicate ID %s for Stanza %s (%s)", 
						v.getValue(), id, keyValues));
			} else { 
				id = v.getValue();
			}
		}
		if(!keyValues.containsKey(k)) { 
			keyValues.put(k, new LinkedList<OBOValue>());
		} 
		
		keyValues.get(k).addLast(v);
	}
	
	public void print(PrintWriter w) { 
		w.println(String.format("[%s]", type));
		for(String k : keyValues.keySet()) { 
			for(OBOValue v : keyValues.get(k)) { 
				w.println(String.format("%s : %s", k, v.toFullString()));
			}
		}
		w.println();
	}
	
	public String getId() { 
		return id;
	}
	
	public String getName() {
		LinkedList<OBOValue> nameList = keyValues.get("name");
		// nameList should be ALWAYS size 1. 
		OBOValue name = nameList.get(0);
		return name.toFullString();
	}

	@Override
	public String toString() { 
		return id != null ? id : String.format("%s OBOStanza", type);
	}

}
