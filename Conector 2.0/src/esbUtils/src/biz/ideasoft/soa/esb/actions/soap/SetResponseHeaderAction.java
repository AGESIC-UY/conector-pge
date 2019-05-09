package biz.ideasoft.soa.esb.actions.soap;

import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.http.HttpHeader;
import org.jboss.soa.esb.http.HttpResponse;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.ResponseHeader;


/**
* Configuration Example:
 *<pre>{@code
 *
 *<action name="set-contenttype" class="biz.ideasoft.soa.esb.actions.soap.SetResponseHeaderAction">
 *  <property name="header-name" value="Content-Type"/>
 *  <property name="header-value" value="text/xml;charset=UTF-8"/>
 *</action>
 *
 * }</pre>
*/
public class SetResponseHeaderAction extends AbstractActionLifecycle {
	protected ConfigTree _config;
	
	protected String headerName;
	protected String headerValue;
	
	public SetResponseHeaderAction(ConfigTree config) {
		_config = config;
	}

	@Override
	public void initialise() throws ActionLifecycleException {

		headerName = _config.getAttribute("header-name");
		if (headerName == null) {
			throw new ActionLifecycleException(getClass().getName() + ": header-name is required");
		}
		headerValue = _config.getAttribute("header-value");
		if (headerValue == null) {
			throw new ActionLifecycleException(getClass().getName() + ": header-value is required");
		}
	}


    public Message process(Message message) throws Exception {
		message.getProperties().setProperty(headerName, new ResponseHeader(headerName, headerValue));
		
		org.jboss.soa.esb.http.HttpHeader httpHeader = new org.jboss.soa.esb.http.HttpHeader(headerName, headerValue);
		HttpResponse responseInfo = org.jboss.soa.esb.http.HttpResponse.getResponse(message);
		if (responseInfo == null) {
			responseInfo = new org.jboss.soa.esb.http.HttpResponse(200);
		}		
		responseInfo.addHeader(httpHeader);		
		responseInfo.setResponse(message);
		return message;
	}
}