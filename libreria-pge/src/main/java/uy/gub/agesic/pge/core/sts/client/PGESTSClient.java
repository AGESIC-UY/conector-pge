package uy.gub.agesic.pge.core.sts.client;

import uy.gub.agesic.pge.beans.ClientCredential;
import uy.gub.agesic.pge.beans.RSTBean;
import uy.gub.agesic.pge.beans.StoreBean;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.core.ssl.SSLContextInitializer;
import uy.gub.agesic.pge.core.xml.util.WSTrustUtil;
import uy.gub.agesic.pge.enums.SamlVersion;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

public class PGESTSClient {
    public Response<SOAPMessage> requestSecurityToken(String serviceName, ClientCredential credential, StoreBean keyStoreSSL, StoreBean trustStoreSSL, PGEConfiguration config, SSLContextInitializer sslContextInitializer) throws RequestSecurityTokenException {
        String stsUrl = config.getSTSURL();
        setHostnameVerifier();

        SOAPMessage requestSecurityTokenMessage = WSTrustUtil.createRequestSecurityTokenMessage(credential, config, serviceName);
        QName service = new QName("http://uy.gub.agesic.trust/sts/", "PGESTS");
        QName portName = new QName("http://uy.gub.agesic.trust/sts/", "PGESTSPort");

        Service jaxwsService = Service.create(service);
        jaxwsService.addPort(portName, "http://schemas.xmlsoap.org/wsdl/soap/http", stsUrl);

        Dispatch<SOAPMessage> dispatch = jaxwsService.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        dispatch.getRequestContext().put("org.jboss.ws.trustStore", trustStoreSSL.getStoreFilePath());
        dispatch.getRequestContext().put("org.jboss.ws.trustStorePassword", trustStoreSSL.getStorePwd());
        dispatch.getRequestContext().put("org.jboss.ws.keyStore", keyStoreSSL.getStoreFilePath());
        dispatch.getRequestContext().put("org.jboss.ws.keyStorePassword", keyStoreSSL.getStorePwd());

        if (sslContextInitializer != null) {
            sslContextInitializer.initSSLContext(dispatch, config);
        }

        return dispatch.invokeAsync(requestSecurityTokenMessage);
    }

    private void setHostnameVerifier() {
        HostnameVerifier hv = (urlHostName, session) -> urlHostName.equals(session.getPeerHost());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
}