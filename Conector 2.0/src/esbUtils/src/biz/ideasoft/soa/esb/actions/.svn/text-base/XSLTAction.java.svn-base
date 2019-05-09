package biz.ideasoft.soa.esb.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.mapping.ObjectMapper;
import org.jboss.soa.esb.message.mapping.ObjectMappingException;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class XSLTAction extends AbstractActionPipelineProcessor {
	private Transformer transformer = null;
	private String xsl = null;
	
	private static Logger log = Logger.getLogger(CBRSyncInvoker.class);
	
	private Map<String, String> parameters = new HashMap<String, String>();	
	private Map<String, String> encodings = new HashMap<String, String>();

	private String actionInfo;

	public XSLTAction(ConfigTree config) {	
		xsl = config.getAttribute("xslt");
		
		ConfigTree[] parameters = config.getChildren("parameter");
		for (ConfigTree configTree : parameters) {
			String name = configTree.getAttribute("name");
			String value = configTree.getAttribute("value");
			String encoding = configTree.getAttribute("encoding");
			this.parameters.put(name, value);
			this.encodings.put(name, encoding);
		}
		
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";
	}
	
	public Message process(Message message) throws ActionProcessingException {
		try {
			String result = xslt(message);
			message.getBody().add(result);
		} catch (Exception e) {
			URI msgID = message.getHeader().getCall().getMessageID();
			log.error(actionInfo + " [" + msgID + "] " + e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return message;
	}
	
	private String xslt(Message message) throws UnsupportedEncodingException, 
		TransformerFactoryConfigurationError, TransformerException, ActionProcessingFaultException, 
		ConfigurationException, ObjectMappingException {
		
		String xml = getPayload(message);
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		Source source = new StreamSource(new InputStreamReader(in, "UTF-8"));

		if (transformer == null) {
			InputStream stream = StreamUtils.getResource(xsl);
			StreamSource transformSource = new StreamSource(stream);
			
			TransformerFactory transFact = TransformerFactory.newInstance();
			transformer = transFact.newTransformer(transformSource);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStreamWriter resultWriter = new OutputStreamWriter(outputStream, "UTF-8");
		StreamResult transformResult = new StreamResult(resultWriter);

		synchronized (transformer) {
			resolveParameters(message);
			transformer.transform(source, transformResult);
		}		
		
		return new String(outputStream.toByteArray());
	}
	
	private void resolveParameters(Message message) throws ObjectMappingException, UnsupportedEncodingException {
		transformer.clearParameters();
		Iterator<String> iter = parameters.keySet().iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = parameters.get(key);
			String encoding = encodings.get(key);
			
			Object eval;
			if (encoding != null) {
				String strEncoded = (String) mapper.getObjectFromMessage(message, value); 
				eval = new String (strEncoded.getBytes(),encoding);
			} else {
				eval = (String) mapper.getObjectFromMessage(message, value);	
			}
			
			if (eval != null) {
				transformer.setParameter(key, eval);
			}
		}		
	}
	
	protected String getPayload(Message message) throws ActionProcessingFaultException {
    	Object payload = message.getBody().get();
    	
    	if (payload instanceof String) {
    		return payload.toString();
    	} else if (payload instanceof byte[]) {
    		return new String((byte[]) payload);
    	}
    	return payload.toString();
    }
	
	
}