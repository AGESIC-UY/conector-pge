package biz.ideasoft.soa.esb.util;

import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtilities {
	protected static Logger _logger = Logger.getLogger(PropertiesUtilities.class);
	
	public static void replaceAllProperties(Properties basicProperties, Properties p) {
		Iterator it = p.keySet().iterator();
		while (it.hasNext()) {
			String s = (String) it.next();
			String value = p.getProperty(s);
			if (value != null) {
				value = PropertiesUtilities.replaceProperties(value, basicProperties, p);
				p.setProperty(s, value);
			}
		}
	}

	public static String replaceProperties(String val, Properties props)
			throws IllegalArgumentException {
		return replaceProperties(val, null, props);
	}

	public static String replaceProperties(String val,
			Properties basicProperties, Properties props)
			throws IllegalArgumentException {
		if (val == null) {
			return null;
		}
		StringBuffer sbuf = new StringBuffer();

		int i = 0;
		int j, k;

		while (true) {
			j = val.indexOf(DELIM_START, i);
			if (j == -1) {
				// no more variables
				break;
			} else {
				sbuf.append(val.substring(i, j));
				k = val.indexOf(DELIM_STOP, j);
				if (k == -1) {
					// throw new IllegalArgumentException
					_logger.warn('"'
							+ val
							+ "\" has no closing brace. Opening brace at position "
							+ j + '.');
					break;
				} else {
					j += DELIM_START_LEN;
					String key = val.substring(j, k);
					// first try in Global Properties
					String replacement = basicProperties == null ? null
							: basicProperties.getProperty(key);

					// then try props parameter
					if (replacement == null && props != null) {
						replacement = props.getProperty(key);
					}

					if (replacement != null) {
						// Do variable substitution on the replacement string
						// such that we can solve "Hello ${x2}" as "Hello p1"
						// the where the properties are
						// x1=p1
						// x2=${x1}
						String recursiveReplacement = replaceProperties(
								replacement, basicProperties, props);

						sbuf.append(recursiveReplacement);
					}
					i = k + DELIM_STOP_LEN;
				}
			}
		}

		if (i == 0) { // this is a simple string
			return val;
		} else { // add the tail string which contails no variables and return
					// the result.
			sbuf.append(val.substring(i, val.length()));
			return sbuf.toString();
		}
	}

	public static String replaceSpecialChars(String s) {
		char c;
		int len = s.length();
		StringBuffer sbuf = new StringBuffer(len);

		int i = 0;

		while (i < len) {
			c = s.charAt(i++);
			if (c == '\\' && i < len) {
				c = s.charAt(i++);
				if (c == 'n')
					c = '\n';
				else if (c == 'r')
					c = '\r';
				else if (c == 't')
					c = '\t';
				else if (c == 'f')
					c = '\f';
				else if (c == '\b')
					c = '\b';
				else if (c == '\"')
					c = '\"';
				else if (c == '\'')
					c = '\'';
				else if (c == '\\')
					c = '\\';
			}
			sbuf.append(c);
		}
		return sbuf.toString();
	}

	static String DELIM_START = "${";
	static char DELIM_STOP = '}';
	static int DELIM_START_LEN = 2;
	static int DELIM_STOP_LEN = 1;
}
