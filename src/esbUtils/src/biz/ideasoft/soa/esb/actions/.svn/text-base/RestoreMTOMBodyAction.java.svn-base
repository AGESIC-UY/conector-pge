package biz.ideasoft.soa.esb.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

/**
* Configuration Example:
 *<pre>{@code
 *
 *<action name="set-wsa-properties" class="biz.ideasoft.soa.esb.actions.SetWsaPropertiesAction"/>
 *
 * }</pre>
*/
public class RestoreMTOMBodyAction extends AbstractActionLifecycle {
	private Logger logger = Logger.getLogger(RestoreMTOMBodyAction.class);
	protected ConfigTree _config;
	
	public RestoreMTOMBodyAction(ConfigTree config) {
		_config = config;
		
	}

    public Message process(Message message) throws Exception {
    	Object payload = message.getBody().get("MTOM_BODY");
    	Object defaultPayload = message.getBody().get();
    	if (payload instanceof byte[] && defaultPayload instanceof String) {
    		String contentType = (String) message.getProperties().getProperty("MTOM_CONTENT_TYPE");
    		byte[] data = (byte[]) payload;
    		MimeMultipart m = new MimeMultipart(new BodyDataSource(data, contentType));
    		
    		BodyPart envelope = m.getBodyPart(0);
    		m.removeBodyPart(0);
    		
    		String body = (String) defaultPayload;
    		String partContentType = envelope.getContentType();
    		data = body.getBytes();
    		DataHandler dh2 = new DataHandler(new BodyDataSource(data, partContentType));    		
    		MimeBodyPart mimePart = new MimeBodyPart();
    		mimePart.setDataHandler(dh2);

    		Enumeration enumeration = envelope.getAllHeaders();
    		while (enumeration.hasMoreElements()) {
    			Header h = (Header) enumeration.nextElement();
    			mimePart.setHeader(h.getName(), h.getValue());
    		}
    		m.addBodyPart(mimePart, 0);    		
    		
    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
    		m.writeTo(stream);    		
    		message.getBody().add(stream.toByteArray());
    		message.getProperties().setProperty("content-type", contentType);
    		
    		message.getBody().remove("MTOM_BODY");
    		message.getProperties().remove("MTOM_CONTENT_TYPE");
    		
    	}
		return message;
	}
    
    private class BodyDataSource implements DataSource {
    	
    	private byte[] data;
    	private String contentType;
    	
    	public BodyDataSource(byte[] data, String contentType) {
    		this.data = data;
    		this.contentType = contentType;   		
    	}

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
    	
    }

}