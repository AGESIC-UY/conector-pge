package biz.ideasoft.soa.esb.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.http.HttpRequest;
//import org.jboss.soa.esb.http.HttpResponse;
import org.jboss.soa.esb.message.Message;

import biz.ideasoft.soa.esb.util.SoapUtil;

/**
* Configuration Example:
 *<pre>{@code
 *
 *<action name="set-wsa-properties" class="biz.ideasoft.soa.esb.actions.SetWsaPropertiesAction"/>
 *
 * }</pre>
*/
public class SaveMTOMBodyAction extends AbstractActionLifecycle {
	private Logger logger = Logger.getLogger(SaveMTOMBodyAction.class);
	protected ConfigTree _config;
	
	public SaveMTOMBodyAction(ConfigTree config) {
		_config = config;
		
	}

    public Message process(Message message) throws Exception {
    	Object payload = message.getBody().get();
    	if (payload instanceof byte[]) {
    		message.getBody().add("MTOM_BODY", payload);
    		String contentTypeValue = (String) message.getProperties().getProperty("content-type");
    		if (contentTypeValue == null) {
    			HttpRequest request = (HttpRequest) message.getProperties().getProperty(HttpRequest.REQUEST_KEY);
    			contentTypeValue = request.getContentType();
    		}
    		final String contentType = contentTypeValue; 

    		message.getProperties().setProperty("MTOM_CONTENT_TYPE", contentType);
    		final byte[] data = (byte[]) payload;
    		MimeMultipart m = new MimeMultipart(new DataSource() {
				
    			public OutputStream getOutputStream() throws IOException {
    				throw new IOException("not supported");
    			}
    			
    			public String getName() {
    				return "name";
    			}
    			
    			public InputStream getInputStream() throws IOException {
    				return new ByteArrayInputStream(data);
    			}
    			
    			public String getContentType() {
    				return contentType;
    			}
    		});

//    		For test only
//			File file = new File("/home/apereiro/Desktop/test.part");
//			OutputStream out = new FileOutputStream(file);
//			out.write(data, 0, data.length);
//			out.close();
//        	
			BodyPart bodyPart = m.getBodyPart(0);
    		if (bodyPart != null) {
    			Object content = bodyPart.getInputStream();
    			if (content instanceof StreamSource || content instanceof InputStream) {
    				InputStream is = null;
    				if (content instanceof StreamSource) {
    	    			StreamSource source = (StreamSource) bodyPart.getContent();
    	    			is = source.getInputStream();
    				} else {
    					is = (InputStream) content;
    				}
	    			if (is != null) {
	    				Writer writer = new StringWriter();
	    				char[] buffer = new char[1024];
	    				try {
//	    					Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    					Reader reader = new BufferedReader(new InputStreamReader(is));
	    					int n;
	    					while ((n = reader.read(buffer)) != -1) {
	    						writer.write(buffer, 0, n);
	    					}
	    				} finally {
	    					is.close();
	    				}
	    				message.getBody().add(writer.toString());
	    			}
    			} else if (content instanceof String) {
    				message.getBody().add(content);
    				message.getProperties().setProperty("content-type", bodyPart.getContentType());
    			} else {
    				SoapUtil.throwFaultException(new ActionProcessingFaultException("Invalid BodyPart 0 content: " + content));
    			}
    		}
    	}
		return message;
	}

}