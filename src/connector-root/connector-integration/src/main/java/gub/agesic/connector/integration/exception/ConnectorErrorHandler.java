package gub.agesic.connector.integration.exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import gub.agesic.connector.exceptions.ConnectorException;

@Service
public class ConnectorErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(ConnectorErrorHandler.class);

    private static final String SERVER_CODE = "Server";

    private final static String INTERNAL_SERVER_ERROR = "Error interno en el conector";

    private static final String CONNECTION_ERROR = "No se pudo establecer la conexion con el servicio de la PDI";

    private static final String TIMEOUT_ERROR = "Expiro el tiempo de espera de una respuesta con el servicio de la PDI";

    private static final String ACTOR = "http://servicios.pge.red.uy/conectorPGE";

    public Message<String> handleError(final Message<MessagingException> message) {
        final Throwable originalException = message.getPayload().getRootCause();
        String soapFault = "";
        /*
         * Handle Http 500 error codes. They may be soap faults or just http 500
         * with empty body
         */
        if (originalException instanceof HttpServerErrorException) {
            final HttpServerErrorException exception = (HttpServerErrorException) originalException;
            soapFault = exception.getResponseBodyAsString();
            if (soapFault.isEmpty()) {
                soapFault = buildSOAPFault(SERVER_CODE, INTERNAL_SERVER_ERROR);
            }
        }
        /*
         * Handle connection errors such as connection refused or unknown host
         */
        else if (originalException instanceof UnknownHostException
                || originalException instanceof ConnectException) {
            soapFault = buildSOAPFault(SERVER_CODE, CONNECTION_ERROR);
        }
        /*
         * Handle timeout errors. Service did not send a response in the
         * specified timeout
         */
        else if (originalException instanceof SocketTimeoutException) {
            soapFault = buildSOAPFault(SERVER_CODE, TIMEOUT_ERROR);
        }
        /*
         * Connector specific exceptions
         */
        else if (originalException instanceof ConnectorException) {
            final ConnectorException connectorException = (ConnectorException) originalException;
            soapFault = buildSOAPFault(SERVER_CODE, connectorException.getMessage());
        }
        /*
         * Unknown errors are handled here
         */
        else {
            LOGGER.error("Error desconocido procesando mensaje soap", originalException);
            soapFault = buildSOAPFault(SERVER_CODE, INTERNAL_SERVER_ERROR);
        }

        return MessageBuilder.withPayload(soapFault)
                .setHeader(MessageHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
                .setHeader(HttpHeaders.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private String buildSOAPFault(final String code, final String errorMessage) {
        final MessageFactory messageFactory;
        try {
            messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            final SOAPMessage message = messageFactory.createMessage();
            final SOAPFault soapFault = message.getSOAPBody().addFault();
            final QName faultName = new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, code);

            soapFault.setFaultCode(faultName);
            soapFault.setFaultActor(ACTOR);
            soapFault.setFaultString(errorMessage);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            message.writeTo(out);

            return new String(out.toString());

        } catch (final SOAPException | IOException e) {
            // Si entra aca es raro
            LOGGER.error("Error interno al construir soap fault.");
            return "";
        }
    }
}