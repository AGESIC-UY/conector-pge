package biz.ideasoft.soa.esb.actions.smooks;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import biz.ideasoft.soa.esb.actions.SmooksContextAction;

public class ConfigPropertiesAccessor implements Map<String, Object> {
	public void clear() {
	}

	public boolean containsKey(Object key) {
		ConfigTree tree = SmooksContextAction.getConfigTree();
		return tree == null ? false : tree.getChildren(key.toString()) != null;
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new RuntimeException("Not implemented");
	}

	public Object get(Object key) {
		ConfigTree tree = SmooksContextAction.getConfigTree();
		return tree == null ? null : tree.getAttribute(key.toString());
	}

	public boolean isEmpty() {
		return false;
	}

	public Set<String> keySet() {
		throw new RuntimeException("Not implemented");
	}

	public Object put(String key, Object value) {
		throw new RuntimeException("Not implemented");
	}

	public void putAll(Map<? extends String, ? extends Object> t) {
		throw new RuntimeException("Not implemented");
	}

	public Object remove(Object key) {
		throw new RuntimeException("Not implemented");
	}

	public int size() {
		Message msg = SmooksContextAction.getCurrentThreadMessage(); 
		return msg == null ? 0 : msg.getProperties().getNames().length;
	}

	public Collection<Object> values() {
		return null;
	}
	
}
