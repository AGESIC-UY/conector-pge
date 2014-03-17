package biz.ideasoft.soa.esb.util;

import org.jboss.soa.esb.message.Message;

public class DslSupport {
	public static boolean propertyEquals(Message msg, String propertyName, String propertyValue) {
		Object value = msg.getProperties().getProperty(propertyName);
		if (value == null) {
			return false;
		}
		
		return String.valueOf(value).equals(propertyValue);
	}
}
