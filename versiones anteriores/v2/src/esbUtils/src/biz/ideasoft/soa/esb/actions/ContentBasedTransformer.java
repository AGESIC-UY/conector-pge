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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.log4j.Logger;
import org.jaxen.saxpath.SAXPathException;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.mapping.ObjectMapper;
import org.jboss.soa.esb.message.mapping.ObjectMappingException;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;


import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathUtil;

public class ContentBasedTransformer extends AbstractActionPipelineProcessor {
	public static final String NAMESPACE_TAG = "namespace";
	public static final String XPATH_TAG = "xpath";
	public static final String DEFAULT_ACTION_TAG = "default";
	public static final String XSLT_TAG = "xslt";
		
	private static Logger log = Logger.getLogger(ContentBasedTransformer.class);
	
	private String actionInfo;
	
	private XSLTInformation defaultXsltAction;
	private Map<String, XSLTInformation> xslts = new HashMap<String, XSLTInformation>();
	
	private Map<String, String> xpaths;
	private List<String> xpathsList = new LinkedList<String>();
	
	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	private Map<String, Expression> exprsCache;

	private Map<String, Transformer> transformers = new HashMap<String, Transformer>();	

	public ContentBasedTransformer(ConfigTree config) throws ConfigurationException, RegistryException, MessageRouterException {
		nsContext.declarePrefix("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
		ConfigTree[] nsList = config.getChildren(NAMESPACE_TAG);
		if (nsList != null && nsList.length > 0) {
			for (ConfigTree ct : nsList) {
				String url = ct.getRequiredAttribute("url");
				String prefix = ct.getRequiredAttribute("prefix");
				nsContext.declarePrefix(prefix, url);
			}
		}
		dog = new XMLDog(nsContext, null, null);

		xpaths = new HashMap<String, String>();
		exprsCache = new HashMap<String, Expression>();
		ConfigTree[] xpathList = config.getChildren(XPATH_TAG);
		if (xpathList != null && xpathList.length > 0) {
			for (ConfigTree ct : xpathList) {
				String xpath = ct.getRequiredAttribute("expr");
				String transformation = ct.getRequiredAttribute("transformation");
				xpaths.put(transformation, xpath);
				
				xpathsList.add(transformation);
				
				try {
					Expression xpathExpr = dog.addXPath(xpath);
					exprsCache.put(xpath, xpathExpr);
				} catch (SAXPathException e) {
					log.error("Error compiling xpath expression", e);
				}
			}
		}		

		ConfigTree[] xsltConfig = config.getChildren(XSLT_TAG);
		if (xsltConfig != null && xsltConfig.length > 0) {
			for (ConfigTree ct : xsltConfig) {
				String name = ct.getAttribute("name");
				String path = ct.getRequiredAttribute("path");
				
				XSLTInformation xsltInfo = new XSLTInformation();
				xslts.put(name, xsltInfo);
				xsltInfo.setName(name);
				xsltInfo.setPath(path);
				
				
				ConfigTree[] xsltConfigParameters = ct.getChildren("parameter");
				if (xsltConfigParameters != null && xsltConfigParameters.length > 0) {
					for (ConfigTree ctParam : xsltConfigParameters) {
						String nameParam = ctParam.getAttribute("name");
						String valueParam = ctParam.getRequiredAttribute("value");
						
						xsltInfo.getParameters().put(nameParam, valueParam);
					}
				}
			}
		}

		ConfigTree[] defaultList = config.getChildren(DEFAULT_ACTION_TAG);
		if (defaultList != null && defaultList.length > 0) {
			ConfigTree ct = defaultList[0];
			String path = ct.getRequiredAttribute("path");
			
			defaultXsltAction = new XSLTInformation();
			defaultXsltAction.setName(DEFAULT_ACTION_TAG);
			defaultXsltAction.setPath(path);

			ConfigTree[] xsltConfigParameters = ct.getChildren("parameter");
			if (xsltConfigParameters != null && xsltConfigParameters.length > 0) {
				for (ConfigTree ctParam : xsltConfigParameters) {
					String nameParam = ctParam.getAttribute("name");
					String valueParam = ctParam.getRequiredAttribute("value");
					
					defaultXsltAction.getParameters().put(nameParam, valueParam);
				}
			}
		}
		
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";
		
	}

	public Message process(Message message) throws ActionProcessingException {
		XSLTInformation xsltAction = null;
		URI msgID = message.getHeader().getCall().getMessageID();
		try {
			xsltAction = executeRules(message);
		} catch (Exception e) {
			log.warn(actionInfo + " [" + msgID + "] " + "Error when executing the rules", e);
		}
		try {
			if (xsltAction == null) {
				if (defaultXsltAction != null) {
					xsltAction = defaultXsltAction;
					if (log.isDebugEnabled()) {
						log.debug(actionInfo + " [" + msgID + "] " + "Xslt default action: " + defaultXsltAction);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug(actionInfo + " [" + msgID + "] " + "No xslts Action was found to transform message");
					}
					return message;
				}
			}
			
			String result = xslt(message, xsltAction);
			message.getBody().add(result);
			
			return message;
			
		} catch (Exception e) {
			log.error(actionInfo + " [" + msgID + "] " + e.getLocalizedMessage() + "Mensaje que genera error: " + message.toString(), e);
			//SoapUtil.throwFaultException(e);
			String cause = "Mensaje con formato invalido";
			Message faultMessage = SoapUtil.getFaultMessage(
					SoapUtil.CLIENT_ERROR, cause, null, SoapUtil.SOAP_NAME_TYPE, AgesicLoggerConstants.BAD_MESSAGE_FORMAT);
			throw new ActionProcessingFaultException(faultMessage, cause);
		}
	}
	
	protected XSLTInformation executeRules(Message message) {
		XSLTInformation xsltAction = null;
		URI msgID = message.getHeader().getCall().getMessageID();
		try {
			XPathResults results = dog.sniff(XPathUtil.getInputSource(message));
			for (String transformationName : xpathsList){
				String xpath =  xpaths.get(transformationName);
				
				if (log.isTraceEnabled()) {
					log.trace(actionInfo + " [" + msgID + "] " + "transformationName = "+ transformationName);
				}
				
				Expression expr = exprsCache.get(xpath);
				Object obj = results.getResult(expr);
				if (log.isTraceEnabled()) {
					log.trace(actionInfo + " [" + msgID + "] " + "transformation_result Object = "+ obj);
				}
				boolean ok = false;
				if (obj instanceof Boolean) {
					ok = (Boolean) obj;
				} else if (obj instanceof List) {
					ok = ((List) obj).size() > 0;
				}
				
				if (log.isTraceEnabled()) {
					log.trace(actionInfo + " [" + msgID + "] " + "transformation_result = "+ ok);
				}
				if (ok) {
					if (log.isDebugEnabled()) {
						log.debug(actionInfo + " [" + msgID + "] " + "XSLT Action " + transformationName);
					}
					xsltAction = xslts.get(transformationName);
					if (xsltAction != null) {
						break;
					}
				}
			}
		} catch (XPathException e) {
			e.printStackTrace();
			log.error(actionInfo + " [" + msgID + "] " + e.getLocalizedMessage(), e);
		}		
		
		return xsltAction;
	}
	

	private String xslt(Message message, XSLTInformation xsltInformation) throws UnsupportedEncodingException, 
		TransformerFactoryConfigurationError, TransformerException, ActionProcessingFaultException, 
		ConfigurationException, ObjectMappingException {
		
		String xml = getPayload(message);
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		Source source = new StreamSource(new InputStreamReader(in, "UTF-8"));

		if (!transformers.containsKey(xsltInformation.getPath())) {
			InputStream stream = StreamUtils.getResource(xsltInformation.getPath());
			StreamSource transformSource = new StreamSource(stream);
			
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer transformer = transFact.newTransformer(transformSource);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformers.put(xsltInformation.getPath(), transformer);
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStreamWriter resultWriter = new OutputStreamWriter(outputStream, "UTF-8");
		StreamResult transformResult = new StreamResult(resultWriter);

		Transformer transformer = transformers.get(xsltInformation.getPath());
		synchronized (transformer) {
			resolveParameters(message, transformer, xsltInformation);
			transformer.transform(source, transformResult);
		}		
		
		return new String(outputStream.toByteArray());
		
	}
	
	private void resolveParameters(Message message, Transformer transformer, XSLTInformation xsltInformation) throws ObjectMappingException {
		transformer.clearParameters();
		Iterator<String> iter = xsltInformation.getParameters().keySet().iterator();
		ObjectMapper mapper = new ObjectMapper();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = xsltInformation.getParameters().get(key);
			Object eval = mapper.getObjectFromMessage(message, value);
			if (eval != null && eval.toString().length() > 0) {
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