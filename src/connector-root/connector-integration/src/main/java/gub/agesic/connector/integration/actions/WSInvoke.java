package gub.agesic.connector.integration.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import gub.agesic.connector.dataaccess.entity.Connector;
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.opensaml.OpenSamlBootstrap;

@Service
public class WSInvoke {
    private final Logger logger = Logger.getLogger(WSInvoke.class);
    protected String actor = "actor";

    @PostConstruct
    public void init() throws Exception {
        OpenSamlBootstrap.bootstrap();
    }

    public String processToken(final SAMLAssertion token, final Message message,
                               final Connector connector) throws IOException, SOAPException {
        final String body = (String) message.getPayload();
        final InputStream is = new ByteArrayInputStream(body.getBytes());
        final SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);

        if (logger.isDebugEnabled()) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            request.writeTo(out);
            logger.debug("Starting to add soap headers: " + new String(out.toString()));
        }
        insertTokenOnMessage(token, request, connector);
        insertWsAddressingOnMessage(request, connector);
        if (logger.isDebugEnabled()) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            request.writeTo(out);
            logger.debug("Finished adding soap headers: " + new String(out.toString()));
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        request.writeTo(out);

        return new String(out.toString());
    }

    private void insertTokenOnMessage(final SAMLAssertion samlAssertion,
            final SOAPMessage soapMessage, final Connector connector) {

        final Element elemToken = samlAssertion.getDOM();

        try {
            SOAPHeader header = soapMessage.getSOAPHeader();
            if (header == null) {
                header = soapMessage.getSOAPPart().getEnvelope().addHeader();
            }

            final QName securityName = new QName(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "Security", "wsse");
            final Iterator iter = header.getChildElements(securityName);
            SOAPElement security = null;
            if (iter.hasNext()) {
                security = (SOAPElement) iter.next();
            } else {
                security = header.addChildElement(securityName);
            }

            security.addChildElement(SOAPFactory.newInstance().createElement(elemToken));

            final QName envelopeQName = soapMessage.getSOAPPart().getEnvelope().getElementQName();
            final QName actorQName = new QName(envelopeQName.getNamespaceURI(), "actor",
                    envelopeQName.getPrefix());
            security.addAttribute(actorQName, actor);

            if (connector.isEnableUserCredentials()) {
                final QName usernameTokenName = new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "UsernameToken", "wsse");
                final SOAPElement securityToken = header.addChildElement(securityName);
                final SOAPElement usernameToken = securityToken.addChildElement(usernameTokenName);

                final QName usernameName = new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "Username", "wsse");
                final QName passwordName = new QName(
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "Password", "wsse");

                usernameToken.addChildElement(usernameName)
                        .addTextNode(connector.getUserCredentials().getUserNameTokenName());
                usernameToken.addChildElement(passwordName)
                        .addTextNode(connector.getUserCredentials().getUserNameTokenPassword());
            }
        } catch (final SOAPException e) {
            logger.error(e);
        }
    }

    private void insertWsAddressingOnMessage(final SOAPMessage soapMessage,
            final Connector connector) throws SOAPException {

        SOAPHeader header = soapMessage.getSOAPHeader();
        if (header == null) {
            header = soapMessage.getSOAPPart().getEnvelope().addHeader();
        }

        final QName wsaTo = new QName("http://www.w3.org/2005/08/addressing", "To", "wsa");
        final QName wsaAction = new QName("http://www.w3.org/2005/08/addressing", "Action", "wsa");

        final SOAPElement wsaToElement = header.addChildElement(wsaTo);
        final SOAPElement wsaActionElement = header.addChildElement(wsaAction);

        wsaToElement.addTextNode(connector.getWsaTo());
        wsaActionElement.addTextNode(connector.getActualRoleOperation().getWsaAction());
    }

}