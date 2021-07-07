package biz.ideasoft.soa.esb.composers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.common.Environment;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.http.HttpContentTypeUtil;
import org.jboss.soa.esb.http.HttpHeader;
import org.jboss.soa.esb.http.HttpRequest;
import org.jboss.soa.esb.http.HttpResponse;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.listeners.gateway.http.HttpRequestWrapper;
import org.jboss.soa.esb.listeners.message.AbstractMessageComposer;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.MessagePayloadProxy;
import org.jboss.soa.esb.message.MessagePayloadProxy.NullPayloadHandling;
import org.jboss.soa.esb.services.security.PublicCryptoUtil;
import org.jboss.soa.esb.services.security.auth.AuthenticationRequest;
import org.jboss.soa.esb.services.security.auth.ExtractionException;
import org.jboss.soa.esb.services.security.auth.ExtractorUtil;
import org.jboss.soa.esb.services.security.auth.SecurityInfoExtractor;
import org.jboss.soa.esb.services.security.auth.ws.BinarySecurityTokenExtractor;
import org.jboss.soa.esb.services.security.auth.ws.UsernameTokenExtractor;

/**
 * Http Message Composer.
 * <p/>
 * This class is used to compose the HttpServletRquest
 * to ESB aware message and decompse the ESB aware message to HttpServletRespones
 * <p>
 * This class will put the http request header and other requst information in ESB message properties with the key "RequestInfoMap".
 * <p>
 * If the request is the submitted from html form(with the <code>Content-Type: application/x-www-form-urlencoded</code>), HttpServletRequest.getParameterMap() result)
 * will be put in ESB message properties. The key for it is "RequestParamterMap". It put the the byte array read from request inputstream in message payload.
 * <p>
 * In decompose process, the header map in message properties will be added
 * in HttpServletResponse. The value for "ReponseStatus" store in ESB message properties will
 * put in the http response. The message payload byte[] or String object will be wrote to HttpServletResponse.
 * If the object in message payload is not byte[],it will throw exception when the ESB message
 * is decomposed
 *
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class HttpMessageComposer<T extends HttpRequestWrapper> extends AbstractMessageComposer<T> {

    private static final Logger logger = Logger.getLogger(HttpMessageComposer.class);

	/** Message payload proxy */
	private MessagePayloadProxy payloadProxy;

    private String payloadAs;

    private final Set<SecurityInfoExtractor<String>> extractors = new LinkedHashSet<SecurityInfoExtractor<String>>();

    private static Set<String> responseHeaderFilterset;
    static {
        responseHeaderFilterset = new HashSet<String>();
        responseHeaderFilterset.add("transfer-encoding");
        responseHeaderFilterset.add("content-length");
        responseHeaderFilterset.add("server");
    }
    
    private static final Map<String,String> localAddr_to_localName = new ConcurrentHashMap<String,String>();
    
    // request.getLocalName() has proven expensive, so cache it
    private static final String getLocalName(HttpServletRequest request) {
    	String localAddr = request.getLocalAddr();
    	String localName = localAddr_to_localName.get(localAddr);
    	if (localName == null) {
    		localName = request.getLocalName();
    		localAddr_to_localName.put(localAddr, localName);
    	}
    	return localName;
    }

    /*
	 * Method for configue the payload proxy
	 */
	public void setConfiguration(ConfigTree config) {
		super.setConfiguration(config);
		payloadProxy = new MessagePayloadProxy(config);
		payloadProxy.setNullSetPayloadHandling(NullPayloadHandling.NONE);
        payloadAs = config.getAttribute("payloadAs");
        
        final String securityNS = config.getAttribute("securityNS", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        extractors.add(new UsernameTokenExtractor(securityNS));
        extractors.add(new BinarySecurityTokenExtractor(securityNS));
    }

	protected MessagePayloadProxy getPayloadProxy() {
		return payloadProxy;
	}

	/*
	 *Method for populating the ESB aware message from a HttpServletRequest
	 */
	protected void populateMessage(Message message, T requestWrapper) throws MessageDeliverException {
		HttpServletRequest request = requestWrapper.getRequest();
		byte[] bodyBytes = null;

        try {
			bodyBytes = StreamUtils.readStream(request.getInputStream());
		} catch (IOException e) {
			throw new MessageDeliverException("Failed to read body data from http request", e);
		}

        String characterEncoding = request.getCharacterEncoding();
        Charset charset;
        if(characterEncoding == null) {
            charset = Charset.defaultCharset();
        } else {
            charset = Charset.forName(characterEncoding);
        }

        int size = bodyBytes.length;
        message.getProperties().setProperty(Environment.MESSAGE_BYTE_SIZE, "" + size);
        
        if(payloadAs == null) {
            String contentType = request.getContentType();

            if(contentType != null && HttpContentTypeUtil.isTextMimetype(contentType)) {
//                try {
//                    String payload = new String(bodyBytes, charset.name());
                	String payload = new String(bodyBytes);

                    payloadProxy.setPayload(message, payload);

                    // In case it's a SOAP message, we need to check for WS-S info...
                    AuthenticationRequest authRequest = null;
                    try {
	                    authRequest = ExtractorUtil.extract(payload, extractors);
                    } catch (final ExtractionException e) {
                        throw new MessageDeliverException(e.getMessage(), e);
                    }
                    
                    if(authRequest != null) {
                        PublicCryptoUtil.INSTANCE.addAuthRequestToMessage(authRequest, message);
                    }
//                } catch (UnsupportedEncodingException e) {
//                    throw new MessageDeliverException("Invalid Character encoding '" + characterEncoding + "' set on request.", e);
//                }
            } else {
                payloadProxy.setPayload(message, bodyBytes);
            }
        } else if(payloadAs.equals("STRING")) {
            try {
                payloadProxy.setPayload(message, new String(bodyBytes, charset.name()));
            } catch (UnsupportedEncodingException e) {
                throw new MessageDeliverException("Invalid Character encoding '" + characterEncoding + "' set on request.", e);
            }
        } else {
            payloadProxy.setPayload(message, bodyBytes);
        }

        //Get the http request info and set it on the message...
        HttpRequest requestInfo = getRequestInfo(request);
        requestInfo.setRequest(message);
    }

    /*
    * Method for decompsing a esb message to a HttpServletResponse
    */
	public Object decompose(Message message, T requestWrapper) throws MessageDeliverException {
        HttpServletRequest request = requestWrapper.getRequest();
        HttpServletResponse  response = requestWrapper.getResponse();
        Integer status = HttpServletResponse.SC_OK;
        String encoding = null;
        HttpResponse responseInfo = org.jboss.soa.esb.http.HttpResponse.getResponse(message);

        if(responseInfo != null) {
            for(org.jboss.soa.esb.http.HttpHeader header : responseInfo.getHttpHeaders()) {
                String headerName = header.getName().toLowerCase();
                if(headerName.equals("content-type")) {
                    response.setContentType(header.getValue());
                } else if(responseHeaderFilterset.contains(headerName)) {
                    // Filter out!!
                } else {
                    response.setHeader(header.getName(), header.getValue());
                }
            }

            if(responseInfo.getContentType() != null) {
                response.setContentType(responseInfo.getContentType());
            }
            encoding = responseInfo.getEncoding();
            status = responseInfo.getResponseCode();
        }

        if(encoding == null) {
            encoding = request.getCharacterEncoding();
            if(encoding == null) {
                encoding = "UTF-8";
            }
        }

        Object obj = payloadProxy.getPayload(message);
		try {
            byte[] outBytes;

            if (obj instanceof String) {
//                outBytes = ((String) obj).getBytes(encoding);
                outBytes = ((String) obj).getBytes();
            } else if (obj instanceof byte[]) {
				outBytes = (byte[]) obj;
            } else if (obj == null) {
                response.setContentLength(0);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.debug("Expected a response payload from '" + ListenerTagNames.MEP_REQUEST_RESPONSE + "' service '" + requestWrapper.getService() + "', but received none.");
                return null;
            } else {
                response.setContentLength(0);
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                logger.debug("Unsupport HTTP response payload type " + obj.getClass().getName() + " from service '" + requestWrapper.getService() + "'.  Only supports java.lang.String or byte[] payloads.");
                return null;
			}

            response.setCharacterEncoding(encoding);
            response.setContentLength(outBytes.length);
            response.setStatus(status);

            if(outBytes.length > 0) {
                response.getOutputStream().write(outBytes);
            }
            
            message.getProperties().setProperty(Environment.MESSAGE_BYTE_SIZE, "" + outBytes.length);
        } catch (IOException e) {
			throw new MessageDeliverException("Unexpected error when write the message to http response", e);
		}

        return null;
	}

	/**
	 * Method for get request information from a servlet request
	 * The result includes the http header and other servlet request information
	 * @param request ServletRequest
	 * @return Request information includes the http header and other information parsed by
	 *         servlet container from a servlet request
	 */
	@SuppressWarnings("unchecked")
	public HttpRequest getRequestInfo(HttpServletRequest request) {
        HttpRequest requestInfo = new org.jboss.soa.esb.http.HttpRequest();

        requestInfo.setAuthType(request.getAuthType());
        requestInfo.setCharacterEncoding(request.getCharacterEncoding());
        requestInfo.setContentType(request.getContentType());
        requestInfo.setContextPath(request.getContextPath());
        requestInfo.setLocalAddr(request.getLocalAddr());
        requestInfo.setLocalName(getLocalName(request));
        requestInfo.setMethod(request.getMethod());
        requestInfo.setProtocol(request.getProtocol());
        requestInfo.setQueryString(request.getQueryString());
        requestInfo.setRemoteAddr(request.getRemoteAddr());
        requestInfo.setRemoteHost(request.getRemoteHost());
        requestInfo.setRemoteUser(request.getRemoteUser());
        requestInfo.setContentLength(request.getContentLength());
        requestInfo.setRequestSessionId(request.getRequestedSessionId());
        requestInfo.setRequestURI(request.getRequestURI());
        requestInfo.setScheme(request.getScheme());
        requestInfo.setServerName(request.getServerName());
        requestInfo.setRequestPath(request.getServletPath());

        String pathInfo = request.getPathInfo();
        requestInfo.setPathInfo(pathInfo);

        if(pathInfo != null) {
            List<String> pathInfoTokens = requestInfo.getPathInfoTokens();

            pathInfoTokens.addAll(Arrays.asList(request.getPathInfo().split("/")));

            // remove empty tokens...
            Iterator<String> tokensIterator = pathInfoTokens.iterator();
            while(tokensIterator.hasNext()) {
                if(tokensIterator.next().trim().length() == 0) {
                    tokensIterator.remove();
                }
            }
        }

        // Http Query params...
        Map paramMap = request.getParameterMap();
        if(paramMap != null) {
            requestInfo.getQueryParams().putAll(paramMap);
        }

        // Http headers...
        Enumeration enumeration = request.getHeaderNames();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			String value = request.getHeader(name);
			requestInfo.getHeaders().add(new HttpHeader(name, value));
		}

        return requestInfo;
	}
}
