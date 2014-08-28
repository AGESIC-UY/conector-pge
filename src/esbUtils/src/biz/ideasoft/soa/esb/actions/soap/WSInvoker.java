package biz.ideasoft.soa.esb.actions.soap;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPathException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.jboss.soa.esb.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EncodingUtils;
import org.apache.log4j.Logger;
import org.jaxen.saxpath.SAXPathException;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.http.HttpClientFactory;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.ResponseHeader;
import org.xml.sax.InputSource;

import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.util.PropertiesConfiguration;
import biz.ideasoft.soa.esb.util.SoapUtil;

import com.exalead.io.failover.FailoverHttpClient;


/**
 * Configuration Example:
 *<pre>{@code
 *  <action name="wsInvoker-action" class="biz.ideasoft.soa.esb.actions.soap.WSInvoker">
 *      <property name="urlPropertyLocation" value="physicalWsaTo"/>
 *      <property name="testUrlPropertyLocation" value="testUrl"/>
 *      <!--
 *      <property name="endpointUrl" value="http://localhost:8080/sampleWS"/>
 *      -->
 *      <property name="SOAPActionPropertyLocation" value="wsaAction"/>
 *      <property name="bodyLocation" value="get-body" />
 *      <property name="responseLocation" value="get-response" />
 *      <property name="maxConnectionsPerHost" value="2" />
 *      <property name="appendProperty" value="false" />
 *      <property name="http-configuration">
 *			<http-client-property name="file" value="/proxy.properties" />      
 *      	<http-client-property name="configurators" value="biz.ideasoft.soa.esb.http.configurators.HttpProxy"/>
 *      	<http-client-property name="proxyURL" value="http://www.ideasoft.biz/proxy"/>
 *      	<http-client-property name="proxyPort" value="1818"/>
 *      	<http-client-property name="proxyUser" value="proxyUser"/>
 *      	<http-client-property name="proxyPassword" value="proxyUser"/>
 *      </property>
 *  </action>
 * }</pre>
 */

public class WSInvoker extends AbstractActionPipelineProcessor {
	public static final String NAMESPACE_TAG = "namespace";
	public static final String XPATH_TAG = "xpath";
	
	private Logger logger = Logger.getLogger(WSInvoker.class);
	protected String actionInfo;

	private String soapActionPropertyLocation;
	private Properties httpClientProps;
	private String bodyLocation;
	private String responseLocation;
	private String endpointUrl;
	private String endpointPropertyLocation;
	private String testEndpointPropertyLocation;
	private String contentType = "text/xml;charset=UTF-8";
	private int maxConnectionsPerHost = 2;
	private String appendProperty;

	protected String keyStoreFilePathProperty;
	protected String keyStorePwdProperty;
	protected String trustStoreFilePathProperty;
	protected String trustStorePwdProperty;
	
	protected String urlValidatorProperty;
	
    private FailoverHttpClient failoverHttpClient;
    private int failoverConnectTimeout = 300;

    private boolean propagateFaultExceptionError = true;

    private int timeoutMillis = 1000 * 60; //1 min
    private String currentCostBodyName = "currentCostBodyName";

	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	private Map<String, Expression> exprsCache;

	public WSInvoker(ConfigTree config) throws ConfigurationException {
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "[" + serviceCategory + " - " + serviceName + " - " + action + "] ";

		keyStoreFilePathProperty = config.getAttribute("keyStoreFilePathProperty");
		keyStorePwdProperty = config.getAttribute("keyStorePwdProperty");
		trustStoreFilePathProperty = config.getAttribute("trustStoreFilePathProperty");
		trustStorePwdProperty = config.getAttribute("trustStorePwdProperty");

		soapActionPropertyLocation = PropertiesConfiguration.getProperty(config.getAttribute("SOAPActionPropertyLocation"));
		endpointUrl = PropertiesConfiguration.getProperty(config.getAttribute("endpointUrl"), null);
		appendProperty = config.getAttribute("appendProperty");
		endpointPropertyLocation = PropertiesConfiguration.getProperty(config.getAttribute("urlPropertyLocation"));
		testEndpointPropertyLocation = PropertiesConfiguration.getProperty(config.getAttribute("testUrlPropertyLocation"));
		bodyLocation = config.getAttribute("bodyLocation");
		responseLocation = config.getAttribute("responseLocation");
		String maxConn = PropertiesConfiguration.getProperty(config.getAttribute("maxConnectionsPerHost"));
		maxConnectionsPerHost = Integer.parseInt(maxConn == null ? "2" : maxConn);
		
		urlValidatorProperty = config.getAttribute("urlValidator", "false");

		String failoverHosts = PropertiesConfiguration.getProperty(config.getAttribute("failoverHosts"));
		if (failoverHosts != null) {
			String connectTimeoutStr = PropertiesConfiguration.getProperty(config.getAttribute("failoverConnectTimeout"));
			if (connectTimeoutStr != null) {
				failoverConnectTimeout = Integer.parseInt(connectTimeoutStr);
			}
			failoverHttpClient = new FailoverHttpClient();
			logger.info(actionInfo + "Failover Client Initialized, connection timeout: " + failoverConnectTimeout);
			failoverHttpClient.setConnectionAcquireFailTimeout(failoverConnectTimeout);

//        	failoverHttpClient.setConnectTimeout(10000);

        	String[] hosts = failoverHosts.split(",");
        	for (String hostPort : hosts) {
        		hostPort = hostPort.trim();

        		String host;
        		int port;

        		int sepIndex = hostPort.indexOf(':');
        		if (sepIndex >= 0) {
        			host = hostPort.substring(0, sepIndex);
        			port = Integer.parseInt(hostPort.substring(sepIndex + 1));
        		} else {
        			host = hostPort;
        			port = 80;
        		}

        		failoverHttpClient.addHost(host, port, 1);

        		logger.info(actionInfo + "Added Failover Host: " + host + ":" + port);
        	}
        	failoverHttpClient.startMonitoring(1);
    	} else {
    		extractHttpClientProps(config);
    	}
		String contentType = PropertiesConfiguration.getProperty(config.getAttribute("contentType"));
		if (contentType != null) {
			this.contentType = contentType;
		}

		String propagateFaultExceptionError = config.getAttribute("propagateFaultExceptionError", "true");
		if (propagateFaultExceptionError != null) {
			this.propagateFaultExceptionError = Boolean.parseBoolean(propagateFaultExceptionError);
		}

		String timeout = config.getAttribute("timeoutMillis");
		if (timeout == null) {
			timeout = PropertiesConfiguration.getProperty("WSInvoker.failoverHttpClient.timeout", "" + timeoutMillis);
		}
		if (timeout != null) {
			try {
				timeoutMillis = Integer.parseInt(timeout);
			} catch (Exception e) {
				logger.warn(e.getLocalizedMessage(), e);
			}
		}
		
		currentCostBodyName = config.getAttribute("currentCostAction");		

		//XMLDOG
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

		exprsCache = new HashMap<String, Expression>();
		ConfigTree[] xpathList = config.getChildren(XPATH_TAG);
		if (xpathList != null && xpathList.length > 0) {
			for (ConfigTree ct : xpathList) {
				String xpath = ct.getRequiredAttribute("expr");
				try {
					Expression xpathExpr = dog.addXPath(xpath);
					exprsCache.put(xpath, xpathExpr);
				} catch (SAXPathException e) {
					logger.error("Error compiling xpath expression", e);
				}
			}
		}
		if (exprsCache.size() == 0) {
			String xpath = "count(/soapenv:Envelope/soapenv:Body/soapenv:Fault) != 0";
			try {
				Expression xpathExpr = dog.addXPath(xpath);
				exprsCache.put(xpath, xpathExpr);
			} catch (SAXPathException e) {
				logger.error("Error compiling xpath expression", e);
			}
		}
	}

	public void initialise() throws ActionLifecycleException {
		super.initialise();
	}

	@Override
	public void destroy() throws ActionLifecycleException {
		super.destroy();
		if (failoverHttpClient != null) {
			failoverHttpClient.shutdown();
		}
		PropertiesConfiguration.resetProperties();
	}

    private void extractHttpClientProps(ConfigTree config) {
        ConfigTree[] httpClientConfigTrees = config.getChildren("http-client-property");

        final ConfigTree parent = config.getParent();
        httpClientProps = new Properties();
        if (parent != null) {
            final String maxThreads = config.getParent().getAttribute(ListenerTagNames.MAX_THREADS_TAG);
            if (maxThreads != null) {
                httpClientProps.setProperty("max-total-connections", maxThreads);
            }
        }

        // The HttpClient properties are attached under the factory class/impl property as <http-client-property name="x" value="y" /> nodes
        for (ConfigTree httpClientProp : httpClientConfigTrees) {
            String propName = httpClientProp.getAttribute("name");
            String propValue = httpClientProp.getAttribute("value");

			if (propName != null && propValue != null) {
				httpClientProps.setProperty(propName, propValue);
			}
		}

        String fileConfig = (String) httpClientProps.remove("file");
        if(fileConfig != null) {
            try {
            	httpClientProps.load(StreamUtils.getResource(fileConfig));
            } catch (IOException e) {
            	logger.error(actionInfo + e.getLocalizedMessage(), e);
			} catch (ConfigurationException e) {
            	logger.error(actionInfo + e.getLocalizedMessage(), e);
			}
        }

    }

    public Message process(final Message message) throws ActionProcessingFaultException {
		URI msgID = message.getHeader().getCall().getMessageID();
    	try {
    		String endpoint = null;
    		boolean isTest = false;
    		if (testEndpointPropertyLocation != null) {
    			endpoint = (String) message.getProperties().getProperty(testEndpointPropertyLocation);
    			isTest = endpoint != null;
    		}
    		if (endpoint == null) {
		    	if (endpointPropertyLocation != null) {
		        	endpoint = (String) message.getProperties().getProperty(endpointPropertyLocation);
		        	if (endpointUrl != null) {
		        		int index = endpoint.lastIndexOf(":");
		        		if (index != -1) {
		        			String endpointAux = endpoint.substring(index);
			        		index = endpointAux.indexOf("/");
			        		if (index != -1) {
			        			endpoint = endpointUrl + endpointAux.substring(index);
			        		}
		        		}
		        	}
		    	} else {
		    		endpoint = endpointUrl;
		    		if (appendProperty != null) {
		    			String toAppend = (String) message.getProperties().getProperty(appendProperty);
		    			if (toAppend != null) {
			    			endpoint += toAppend;
			    			logger.debug(actionInfo + " [" + msgID + "] "  + "Endpoint modified to: " + endpoint);
		    			} else {
		    				logger.warn(actionInfo + " [" + msgID + "] "  + "Property " + appendProperty + " not found, endpoint is: " + endpoint);
		    			}
		    		}
		    	}
    		}

    		if (Boolean.valueOf(urlValidatorProperty)) {
	    		
	    		UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
	    	    if (urlValidator.isValid(endpoint)) {
			    	String soapAction = null;
			    	if (soapActionPropertyLocation != null) {
			    		soapAction = (String) message.getProperties().getProperty(soapActionPropertyLocation);
			    	}
		
			    	if (failoverHttpClient == null || isTest) {
			    		if (isTest) {
			    			logger.debug(actionInfo + " [" + msgID + "] "  + "Sending message to test endpoint: " + endpoint);
			    		}
			    		if (endpoint.startsWith("https://")) {
			    			invokeWSHTTPS(endpoint, soapAction, message);
			    		} else {
				    		invokeWS(endpoint, soapAction, message);
			    		}
			    	} else {
			    		invokeWSWithFailover(endpoint, soapAction, message);
			    	}
	    	    } else {    	    	
	    	    	throw SoapUtil.createActionPipelineException("ERROR en la validacion de la URL: " + endpoint, SoapUtil.SERVER_ERROR, 
	    	    			SoapUtil.SOAP_NAME_TYPE, null, AgesicLoggerConstants.INVALID_URL);
	//    	    	throw new Exception("ERROR en la validacion de la URL: " + endpoint);
	    	    }
    		} else {
    			String soapAction = null;
		    	if (soapActionPropertyLocation != null) {
		    		soapAction = (String) message.getProperties().getProperty(soapActionPropertyLocation);
		    	}
	
		    	if (failoverHttpClient == null || isTest) {
		    		if (isTest) {
		    			logger.debug(actionInfo + " [" + msgID + "] "  + "Sending message to test endpoint: " + endpoint);
		    		}
		    		if (endpoint.startsWith("https://")) {
		    			invokeWSHTTPS(endpoint, soapAction, message);
		    		} else {
			    		invokeWS(endpoint, soapAction, message);
		    		}
		    	} else {
		    		invokeWSWithFailover(endpoint, soapAction, message);
		    	}
    		}
    	} catch (Exception e) {
    		logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);

    		ActionProcessingFaultException ap;
    		if (!(e instanceof ActionProcessingFaultException)) {
				ap = SoapUtil.createActionPipelineException(null, SoapUtil.SERVER_ERROR, SoapUtil.SOAP_NAME_TYPE, e);
    		} else {
				ap = (ActionProcessingFaultException) e;
    		}

    		if (ap.getFaultMessage() != null) {
    			Object o = propagateFaultExceptionError; //message.getBody().get("faultWithSameMessage");
    			if (o != null && Boolean.TRUE.equals(o)) {
//    				message.getBody().add("wsFault", ap.getFaultMessage().getBody().get());
    				message.getBody().add(ap.getFaultMessage().getBody().get());
    			}
    			ActionProcessingFaultException newAp = new ActionProcessingFaultException(message, ap.getMessage());
    			newAp.setStackTrace(ap.getStackTrace());
    			throw newAp;
    		}

    		throw ap;
    	}

        return message;
    }

    protected void invokeWS(String endpoint, String soapAction, Message message) throws ActionProcessingFaultException {
    	Properties props = new Properties(httpClientProps);
    	props.setProperty(HttpClientFactory.TARGET_HOST_URL, endpoint);
    	HttpClient httpclient = null;

        try {
			httpclient = HttpClientFactory.createHttpClient(props);
			((MultiThreadedHttpConnectionManager) httpclient.getHttpConnectionManager()).getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);

	        invokeEndpoint(soapAction, endpoint, httpclient, message);

        } catch (ConfigurationException e) {
    		URI msgID = message.getHeader().getCall().getMessageID();
        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
        	String error = actionInfo + "Failed to create httpClient";
        	throw SoapUtil.createActionPipelineException(error, null, null, e);
		} finally {
			if (httpclient != null) {
				HttpClientFactory.shutdown(httpclient);
			}        
		}
    }

    protected void invokeWSHTTPS(String endpoint, String soapAction, Message message) throws ActionProcessingFaultException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		URI msgID = message.getHeader().getCall().getMessageID();
		try {
			
			if (endpoint.startsWith("https://")) {

				KeyStore keyStore = null;
	            if (keyStoreFilePathProperty != null) {
	            	String filePath;
	            	if ((filePath = (String) message.getProperties().getProperty(keyStoreFilePathProperty)) != null) {
			            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			            FileInputStream instream = new FileInputStream(new File(filePath));
			            try {
			            	String keyStorePassword = message.getProperties().getProperty(keyStorePwdProperty).toString();
			            	keyStore.load(instream, keyStorePassword.toCharArray());
			            } catch (NoSuchAlgorithmException e) {
			            	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			            	String error = actionInfo + "Failed to invoke https";
			            	throw SoapUtil.createActionPipelineException(error, null, null, e);
						} catch (CertificateException e) {
				        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
				        	String error = actionInfo + "Failed to invoke https";
				        	throw SoapUtil.createActionPipelineException(error, null, null, e);
						} finally {
			                try { 
			                	instream.close(); 
			                } catch (Exception ignore) {}
			            }
	            	}
	            }

	            Object filePath;
	            KeyStore trustStore;
	            if ((filePath = message.getProperties().getProperty(trustStoreFilePathProperty)) != null) {
		            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		            FileInputStream instream = new FileInputStream(new File(filePath.toString()));
		            try {
		            	String password = message.getProperties().getProperty(trustStorePwdProperty).toString();
		                trustStore.load(instream, password.toCharArray());
		            } catch (NoSuchAlgorithmException e) {
			        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			        	String error = actionInfo + "Failed to invoke https";
			        	throw SoapUtil.createActionPipelineException(error, null, null, e);
					} catch (CertificateException e) {
			        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			        	String error = actionInfo + "Failed to invoke https";
			        	throw SoapUtil.createActionPipelineException(error, null, null, e);
					} finally {
		                try {
		                	instream.close();
		                } catch (Exception ignore) {}
		            }
	            } else {
		        	String error = actionInfo + " [" + msgID + "] "  + "The trustore was not found";
		        	logger.error(error);
		        	Exception e = new Exception(error);
		        	throw SoapUtil.createActionPipelineException(error, null, null, e);
	            }

				String keyStorePassword = message.getProperties().getProperty(keyStorePwdProperty).toString();
	            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore,  keyStorePassword, trustStore);
	            Scheme sch = new Scheme("https", 443, socketFactory);
	            httpclient.getConnectionManager().getSchemeRegistry().register(sch);
			}

            String contentType = (String) message.getProperties().getProperty("content-type");
            contentType = contentType == null ? this.contentType : contentType;
			String requestSecurityTokenMessage = getPayload(message);
            HttpEntity entity = null;
            if (!isMultiPart(contentType)) {
	            String charSet = getCharSet(contentType);

	            httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charSet);
	            
	            int timeout = calculateTimeout(message);
	            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
	            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
	            
	            HttpParams params = httpclient.getParams();
	            HttpProtocolParams.setContentCharset(params, charSet);

	            if (logger.isDebugEnabled()) {
	            	logger.debug(actionInfo + " [" + msgID + "] "  + "Invokation Message: " + requestSecurityTokenMessage);
	            }

				InputStream instream = new ByteArrayInputStream(requestSecurityTokenMessage.getBytes());
				InputStreamEntity streamEntity = new InputStreamEntity(instream, -1);
				streamEntity.setContentType(contentType);
				streamEntity.setContentEncoding(charSet);
				entity = streamEntity;
            } else {
            	//TODO: aca se podria pasar de MimeMultipart a MultipartEntity de httpcomponents para enviar el mtom.
            	throw SoapUtil.createActionPipelineException("Operacion invalida. No se puede mandar un MTOM por https", null, null, null);
            }
            HttpPost httpMethod = new HttpPost(endpoint);
			httpMethod.setEntity(entity);
			
			if (soapAction != null) {
				httpMethod.addHeader("SOAPAction", "\"" + soapAction + "\"");
			}
			
			HttpResponse httpResponse = httpclient.execute(httpMethod);
			StatusLine statusLine = httpResponse.getStatusLine();
			int result = statusLine.getStatusCode();
            if(result != org.apache.http.HttpStatus.SC_OK && result != org.apache.http.HttpStatus.SC_ACCEPTED) {
            	String error = actionInfo + " [" + msgID + "] "  + "Received status code '" + result + "' on " + statusLine.getProtocolVersion().getProtocol() + " SOAP (POST) request to '" + endpoint + "'."; 
                logger.warn(actionInfo + " [" + msgID + "] "  + error);
                HttpEntity httpEntity = httpResponse.getEntity();
                String statusMessage = statusLine.getReasonPhrase();
				org.apache.http.Header[] header = httpResponse.getHeaders(HttpHeaders.CONTENT_TYPE);
				String contentTypeResponse = header != null ? header[0].getValue() : this.contentType;
				processErrorResponse(error, message, httpEntity.getContent(), httpEntity.getContentType().getValue(), result, statusMessage, contentTypeResponse, isMultiPart(contentTypeResponse));
				return;
            }

            processResponse(message, httpResponse);
		} catch (ActionProcessingFaultException e) {
			String error = actionInfo + " [" + msgID + "] "  + "Invokation error: " + e.getLocalizedMessage();
			logger.error(error, e);
			throw e;
		} catch (Exception e) {
			String error = actionInfo + " [" + msgID + "] "  + "Invokation error: " + e.getLocalizedMessage();
			logger.error(error, e);
			throw SoapUtil.createActionPipelineException(error, null, null, e, AgesicLoggerConstants.ERROR_CONNECTION_URL);
		} finally {
			try {
				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				String error = actionInfo + " [" + msgID + "] "  + "Can not close http connection";
				logger.error(error, e);
				throw SoapUtil.createActionPipelineException(error, null, null, e);
			}
		}
	}

    protected void invokeWSWithFailover(String endpoint, String soapAction, Message message) throws ActionProcessingFaultException {
    	String httpschema = "http://";
    	if (endpoint.startsWith(httpschema)) {
    		endpoint = endpoint.substring(httpschema.length());
    		int index = endpoint.indexOf("/");
    		endpoint = endpoint.substring(index);
    	}
    	PostMethod post = new PostMethod(endpoint);
        String contentType = (String) message.getProperties().getProperty("content-type");
        post.setRequestHeader("Content-Type", contentType == null ? this.contentType : contentType);
        if (soapAction != null) {
        	post.setRequestHeader("SOAPAction", "\"" + soapAction + "\""); //Customization to add quotes to Soap action
        }
        String request = getPayload(message);
        post.setRequestEntity(new StringRequestEntity(request));

		URI msgID = message.getHeader().getCall().getMessageID();
		
        try {
        	int result = failoverHttpClient.executeMethod(post, calculateTimeout(message), 3);
            if(result != HttpStatus.SC_OK && result != HttpStatus.SC_ACCEPTED) {
            	String error = "Received status code '" + result + "' on HTTP SOAP (POST) request to '" + endpoint + "'.";
                logger.warn(actionInfo + " [" + msgID + "] "  + error);
                processErrorResponse(error, message, post);
                return;
            }

            processResponse(message, post);
		} catch (IOException e) {
        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
        	String se = soapAction == null ? "" : (" ' - '" + soapAction);
        	String error = actionInfo + "ESB internal faultError generation. Failed to invoke SOAP Endpoint: '" + endpoint + se + "': " + e;
        	throw SoapUtil.createActionPipelineException(error, "DestinationUnreachable", SoapUtil.WSA_NAME_TYPE, e, AgesicLoggerConstants.ERROR_CONNECTION_URL);
		}
    }

    protected void invokeEndpoint(String soapAction, String endpoint, HttpClient httpclient, Message message) throws ActionProcessingFaultException {
        PostMethod post = new PostMethod(endpoint);
        
        String contentTypeValue = (String) message.getProperties().getProperty("content-type");
		if (contentTypeValue == null) {
			HttpRequest request = (HttpRequest) message.getProperties().getProperty(HttpRequest.REQUEST_KEY);
			contentTypeValue = request != null ? request.getContentType() : this.contentType;

			if (request == null) {
				logger.debug("No se pudo obtener HttpRequest, se resolvio el content-type = " + this.contentType);
			}
		}
		String contentType = contentTypeValue;
        
        //TODO:aca queda mergear codigo poder setear el content-type para soa-p-5
        post.setRequestHeader("Content-Type", contentType == null ? this.contentType : contentType);
        if (soapAction != null) {
        	post.setRequestHeader("SOAPAction", "\"" + soapAction + "\"");  /// Customization to add quotes to Soap action
        }

//        String request = getPayload(message);   
//        post.setRequestEntity(new StringRequestEntity(request));
		URI msgID = message.getHeader().getCall().getMessageID();

        try {
            post.setRequestEntity(getRequestEntity(message, contentType));
            int timeout = calculateTimeout(message);
        	httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
        	httpclient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
        	httpclient.getHttpConnectionManager().getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, 1);
        	httpclient.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, 1);
            int result = httpclient.executeMethod(post);
            if(result != HttpStatus.SC_OK && result != HttpStatus.SC_ACCEPTED) {
            	String error = actionInfo + "Received status code '" + result + "' on HTTP SOAP (POST) request to '" + endpoint + "'."; 
                logger.warn(actionInfo + " [" + msgID + "] "  + error);
                processErrorResponse(error, message, post);
                return;
            }
            processResponse(message, post);
        } catch (Exception e) {
        	logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
        	String se = soapAction == null ? "" : (" ' - '" + soapAction);
        	String error = actionInfo + "Failed to invoke SOAP Endpoint: '" + endpoint + se + "': " + e;
        	throw SoapUtil.createActionPipelineException(error, "DestinationUnreachable", SoapUtil.WSA_NAME_TYPE, e, AgesicLoggerConstants.ERROR_CONNECTION_URL);
        } finally {
            post.releaseConnection();
        }
    }

    protected RequestEntity getRequestEntity(Message message, String contentType) throws ActionProcessingFaultException, UnsupportedEncodingException {
    	Object payload;
    	if (bodyLocation != null) {
    		payload = message.getBody().get(bodyLocation);
    	} else {
    		payload = message.getBody().get();
    	}

    	if (payload == null) {
    		String error = actionInfo + "Payload not found at message location : '" + bodyLocation;
    		throw SoapUtil.createActionPipelineException(error, null, null, null);
    	}
    	
    	if (payload instanceof String) {
//			InputStream instream = new ByteArrayInputStream(payload.toString().getBytes(getCharSet(contentType)));
//			InputStreamRequestEntity requestEntity = new InputStreamRequestEntity(instream);
    		ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(((String) payload).getBytes());
    		return requestEntity;
    	} else if (payload instanceof byte[]) {
       		return new ByteArrayRequestEntity((byte[]) payload);
//           	boolean isMultipart = contentType.indexOf("multipart") >= 0;
//           	if (isMultipart) {
//        	} else {
//        	}
    	} else {
    		String error = actionInfo + "Only process string payload : '" + endpointUrl;
    		throw SoapUtil.createActionPipelineException(error, null, null, null);
    	}
    }

    protected String getPayload(Message message) throws ActionProcessingFaultException {
    	Object payload;
    	if (bodyLocation != null) {
    		payload = message.getBody().get(bodyLocation);
    	} else {
    		payload = message.getBody().get();
    	}

    	if (payload == null) {
    		String error = actionInfo + "Payload not found at message location : '" + bodyLocation;
    		throw SoapUtil.createActionPipelineException(error, null, null, null);
    	}
    	
    	if (payload instanceof String) {
    		return payload.toString();
    	} else if (payload instanceof byte[]) {
    		return new String((byte[]) payload);
    	} else {
    		String error = actionInfo + "Only process string payload : '" + endpointUrl;
    		throw SoapUtil.createActionPipelineException(error, null, null, null);
    	}
    }

    protected void processResponse(Message message, HttpResponse httpResponse) throws IOException {
    	boolean isMultipart = false;
        org.apache.http.Header[] responseHeaders = httpResponse.getAllHeaders();
		String contentType = this.contentType;
		for (org.apache.http.Header responseHeader : responseHeaders) {
        	String name = responseHeader.getName();
        	String value = responseHeader.getValue();
        	if (name.equals(HttpHeaders.CONTENT_TYPE)) {
        		isMultipart = value.indexOf("multipart") >= 0;
        		if (isMultipart) { //Revisar si el for esta deprecated por el metodo setHTTPHeaders
        			message.getProperties().setProperty(name, new ResponseHeader(name, value));
        		}
				contentType = value;
        	}
        }

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

		setHTTPHeaders(message, statusCode, statusMessage, contentType);

    	InputStream stream = null;
        try {
            HttpEntity httpEntity = httpResponse.getEntity();
        	stream = httpEntity.getContent();
        	byte[] rawdata = getByteArray(stream);
        	processResponse(message, rawdata, httpEntity.getContentType().getValue(), isMultipart);
	    } finally {
	    	if (stream != null) {
	    		try {
					closeStream(stream);
				} catch (IOException e) {
		    		URI msgID = message.getHeader().getCall().getMessageID();
					logger.warn(actionInfo + " [" + msgID + "] " , e);
				}
	    	}
	    }

    }

    protected void processResponse(Message message, HttpMethodBase httpMethod) throws IOException {
    	boolean isMultipart = false;
    	String contentType = this.contentType;
        Header[] responseHeaders = httpMethod.getResponseHeaders();
        for(Header responseHeader : responseHeaders) {
        	String name = responseHeader.getName();
        	String value = responseHeader.getValue();
        	if (name.equals(HttpHeaders.CONTENT_TYPE)) {
        		isMultipart = value.indexOf("multipart") >= 0;
        		if (isMultipart) { //Revisar si el for esta deprecated por el metodo setHTTPHeaders
        			message.getProperties().setProperty(name, new ResponseHeader(name, value));
        		}
        		contentType = value;
        	}
        }

        int statusCode = httpMethod.getStatusCode();
        String statusMessage = httpMethod.getStatusText();
        setHTTPHeaders(message, statusCode, statusMessage, contentType);

    	InputStream stream = null;
        try {
        	stream = httpMethod.getResponseBodyAsStream();
        	byte[] rawdata = getByteArray(stream);
        	processResponse(message, rawdata, httpMethod.getResponseCharSet(), isMultipart);
	    } finally {
	    	if (stream != null) {
	    		try {
					closeStream(stream);
				} catch (IOException e) {
		    		URI msgID = message.getHeader().getCall().getMessageID();
					logger.warn(actionInfo + " [" + msgID + "] " , e);
				}
	    	}
	    }
    }
    
    protected void processResponse(Message message, byte[] rawdata, String charSet, boolean isMultipart) {
    	Object response = isMultipart ? rawdata : new String(rawdata);//EncodingUtils.getString(rawdata, charSet);
    	
    	if (responseLocation != null) {
    		message.getBody().add(responseLocation, response);
    	} else {
    		message.getBody().add(response);
    	}
    }

    private void processErrorResponse(String error, Message message, HttpMethodBase httpMethod) throws ActionProcessingFaultException {
    	InputStream stream = null;
    	try {
			Header header = httpMethod.getResponseHeader(HttpHeaders.CONTENT_TYPE);
			String contentType = header != null ? header.getValue() : this.contentType;
    		stream = httpMethod.getResponseBodyAsStream();
        	String charSet = httpMethod.getResponseCharSet();
        	int statusCode = httpMethod.getStatusCode();
        	String statusMessage = httpMethod.getStatusText();
			processErrorResponse(error, message, stream, charSet, statusCode, statusMessage, contentType, isMultiPart(contentType));
        } catch (IOException ioexc) {
        	logger.error(actionInfo + "  "  + ioexc.getLocalizedMessage(), ioexc);
        	Throwable th = new Throwable(error);
	        throw SoapUtil.createActionPipelineException(null, "DestinationUnreachable", SoapUtil.WSA_NAME_TYPE, th);
        } finally {
        	if (stream != null) {
        		try {
					closeStream(stream);
				} catch (IOException e) {
		    		URI msgID = message.getHeader().getCall().getMessageID();
		    		logger.warn(actionInfo + " [" + msgID + "] " , e);
				}
        	}
        }

    }

    private boolean isMultiPart(String contentType) {
		return contentType.indexOf("multipart") >= 0;
	}
    
  //TODO:Hay que revisar el caso MTOM.
    private String getCharSet(String contentType) {
    	String strCS = "charset";
    	int index = contentType.toLowerCase().indexOf(strCS);
    	if (index > 0) {
    		String charset = contentType.substring(index + strCS.length());
    		index = charset.indexOf("=");
    		return charset = charset.substring(index + 1).trim();
    	}
    	
    	return isMultiPart(contentType) ? null : "UTF-8";
    }

    private void processErrorResponse(String error, Message message, InputStream stream, String charSet, int statusCode, String statusMessage, String contentType, boolean isMultiPart) throws ActionProcessingFaultException {
		URI msgID = message.getHeader().getCall().getMessageID();
        try {
        	byte[] rawdata = getByteArray(stream);
	    	String response = EncodingUtils.getString(rawdata, charSet);
			setHTTPHeaders(message, statusCode, statusMessage, contentType);
			logger.warn(actionInfo + " [" + msgID + "] "  + " Response: " + response);
			if (isMultiPart || isFaultResponse(message, response)) {
				processResponse(message, rawdata, charSet, isMultiPart);
	        } else {
//		        message.getBody().add("faultWithSameMessage", true);
		        throw SoapUtil.createActionPipelineException(error, "DestinationUnreachable", SoapUtil.WSA_NAME_TYPE, null);
	        }
        } catch (IOException ioexc) {
        	logger.error(actionInfo + "  "  + ioexc.getLocalizedMessage(), ioexc);
        	Throwable th = new Throwable(error);
	        throw SoapUtil.createActionPipelineException(null, "DestinationUnreachable", SoapUtil.WSA_NAME_TYPE, th);
        } finally {
        	if (stream != null) {
        		try {
					closeStream(stream);
				} catch (IOException e) {
					logger.warn(actionInfo + " [" + msgID + "] " , e);
				}
        	}
        }

    }
    
	private void setHTTPHeaders(Message message, int statusCode, String codeMessage, String contentType) {
		message.getProperties().setProperty(org.jboss.soa.esb.listeners.gateway.HttpMessageComposer.HTTP_RESPONSE_STATUS, statusCode);
		message.getProperties().setProperty(org.jboss.remoting.transport.http.HTTPMetadataConstants.RESPONSE_CODE, statusCode);
		message.getProperties().setProperty(org.jboss.remoting.transport.http.HTTPMetadataConstants.RESPONSE_CODE_MESSAGE, codeMessage);

		message.getProperties().setProperty(org.jboss.remoting.transport.http.HTTPMetadataConstants.CONTENTTYPE, contentType);
		message.getProperties().setProperty("content-type", contentType);
		ResponseHeader header = new ResponseHeader("Content-Type", contentType);
		message.getProperties().setProperty("ResponseHeader-Content-Type", header);
		org.jboss.soa.esb.http.HttpResponse response = new org.jboss.soa.esb.http.HttpResponse(statusCode);
		response.setContentType(contentType);
		response.setResponse(message);
	}
	
	private int calculateTimeout(Message message) {
		try {
			if (currentCostBodyName != null) {
				Long currentCost = System.currentTimeMillis() - (Long) message.getBody().get(currentCostBodyName);
				if (currentCost != null) {				
					int timeout = timeoutMillis - (int) currentCost.longValue();
		    		URI msgID = message.getHeader().getCall().getMessageID();
					if (timeout > 0) {
						if (logger.isDebugEnabled()) {
							logger.debug(actionInfo + " [" + msgID + "] "  + "Calculated timeout = " + timeout);
						}
						return timeout;
					} else {						
			    		logger.warn(actionInfo + " [" + msgID + "] "  + "Timeout = " + timeout);
						logger.warn(actionInfo + " [" + msgID + "] "  + "currentCost = " + currentCost);
						return 1;
					}
				}
			}
			return timeoutMillis;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.warn("There is no more time.");
		return 1;
	}

	protected boolean isFaultResponse(Message message, String response) {
		URI msgID = message.getHeader().getCall().getMessageID();
		try {
			InputSource source = new InputSource(new ByteArrayInputStream(response.getBytes()));
			XPathResults results = dog.sniff(source);
			for (String xpath : exprsCache.keySet()) {
				Expression expr = exprsCache.get(xpath);
				Object obj = results.getResult(expr);
				if (logger.isTraceEnabled()) {
					logger.trace(actionInfo + " [" + msgID + "] "  + "fault_result Object = " + obj);
				}
				boolean ok = false;
				if (obj instanceof Boolean) {
					ok = (Boolean) obj;
				} else if (obj instanceof List) {
					ok = ((List) obj).size() > 0;
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(actionInfo + " [" + msgID + "] "  + "fault_result = " + ok);
				}
				return ok;
			}
		} catch (XPathException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return false;
	}

	static byte[] getByteArray(final InputStream stream) throws IOException {
		if (stream != null) {
			return StreamUtils.readStream(stream);
		} else
			return new byte[0];
	}

	static void closeStream(final Closeable c) throws IOException {
		if (c != null) {
			c.close();
		}
	}

//    static byte[] getByteArray(InputStream in) throws IOException {
//    	ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//    	streamcopy(in, out);
//    	return out.toByteArray();
//    }
//    
//    static void streamcopy(InputStream input, OutputStream output) throws IOException {
//		byte[] b = new byte[2048];
//		int count;
//	
//		do {
//			count = input.read(b);
//			if (count != -1) {
//				output.write(b,0,count);
//			}
//		} while (count != -1);
//	}

}
