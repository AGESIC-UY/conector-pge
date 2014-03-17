package biz.ideasoft.soa.esb.composers;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.http.HttpResponse;
import org.jboss.soa.esb.listeners.gateway.http.HttpMessageComposer;
import org.jboss.soa.esb.listeners.gateway.http.HttpRequestWrapper;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;

public class HTTPComposer extends HttpMessageComposer<HttpRequestWrapper>{

	private static final Logger log = Logger.getLogger(HTTPComposer.class);
	
	@Override
	public Object decompose(Message message, HttpRequestWrapper request) throws MessageDeliverException {

		Object msg = message.getBody().get();
		
		if (msg instanceof String) {
			String strmsg = msg.toString();
	   
			String deencoded = null;
			try {
				deencoded = new String(strmsg.getBytes(),this.charSetResponse(message, request));
			} catch (UnsupportedEncodingException e) {
				log.error("No se pudo hacer el deencode del mensaje", e);
				throw new MessageDeliverException("No se pudo hacer el encode del mensaje");
			}
	    
			message.getBody().add(deencoded);
		}
		
		Object obj = super.decompose(message, request);
		return obj;
	}

	@Override
	protected void populateMessage(Message message, HttpRequestWrapper request)	throws MessageDeliverException {
		super.populateMessage(message, request);
		
		Object msg = message.getBody().get();
		
		if (msg instanceof String) {
			String strmsg = msg.toString();
		   
			String encoded = null ;
			try {
				encoded = new String(strmsg.getBytes(this.charSetRequest(message, request)));
			} catch (Exception e) {
				log.error("No se pudo hacer el encode del mensaje", e);
				throw new MessageDeliverException("No se pudo hacer el encode del mensaje");
			}
		   
			message.getBody().add(encoded);
		}
	}
	
	private String charSetRequest(Message message, HttpRequestWrapper requestWrapper){
		HttpServletRequest request = requestWrapper.getRequest();
		String encoding = request.getCharacterEncoding();
		return encoding != null ? encoding : "UTF-8";
	}

	private String charSetResponse(Message message, HttpRequestWrapper requestWrapper){
		HttpServletRequest request = requestWrapper.getRequest();
        String encoding = null;
        HttpResponse responseInfo = org.jboss.soa.esb.http.HttpResponse.getResponse(message);

        String contentType = null;
        if(responseInfo != null) {
            for(org.jboss.soa.esb.http.HttpHeader header : responseInfo.getHttpHeaders()) {
                String headerName = header.getName().toLowerCase();
                if(headerName.equals("content-type")) {
                	contentType = header.getValue();
                	break;
                }
            }

            if(responseInfo.getContentType() != null) {
            	contentType = responseInfo.getContentType();
            }
            
            encoding = getCharSet(contentType);
        }

        if(encoding == null) {
            encoding = request.getCharacterEncoding();
            if(encoding == null) {
                encoding = "UTF-8";
            }
        }
		return encoding;		
	}
	
    private String getCharSet(String contentType) {
    	String strCS = "charset";
    	int index = contentType.toLowerCase().indexOf(strCS);
    	if (index > 0) {
    		String charset = contentType.substring(index + strCS.length());
    		index = charset.indexOf("=");
    		return charset = charset.substring(index + 1).trim();
    	}    	
    	return null;
    }

	
	
}
