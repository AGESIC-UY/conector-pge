package gub.agesic.connector.integration.actions;

import org.apache.log4j.Logger;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import gub.agesic.connector.integration.controller.ConnectorWsdlService;

import java.net.MalformedURLException;
import java.net.URL;

public class WsdlFetcher {

    private static final String REGEX_PATH = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?";
    private final Logger logger = Logger.getLogger(WsdlFetcher.class);

    private ConnectorWsdlService connectorWsdlService;

    public WsdlFetcher(ConnectorWsdlService connectorWsdlService) {
        this.connectorWsdlService = connectorWsdlService;
    }

    public Message<byte[]> process(final Message<String> message) throws MessageProcessorException {
        try {
            final String path = getPathFromUrl(message);
            final String port = getPortFromUrl(message);

            byte[] wsdlInBytes = connectorWsdlService.getConnectorWSDL(path, port);

            return MessageBuilder
                    .withPayload(wsdlInBytes)
                    .setHeader("Content-Type", "application/xml")
                    .build();

        } catch (Exception e ) {
            logger.error(e);
            throw new MessageProcessorException(e.getMessage());
        }
    }

    private String getPathFromUrl(final Message<String> message) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        return url.replaceFirst(REGEX_PATH, "$5");
    }

    private String getUrlFromMessageHeader(final Message<String> message) {
        // Getting the path from message
        return (String) message.getHeaders().get(HttpHeaders.REQUEST_URL);
    }

    private String getPortFromUrl(final Message<String> message) throws MessageProcessorException {
        final String url = getUrlFromMessageHeader(message);

        try {
            // Getting the port from url string
            return String.valueOf(new URL(url).getPort());
        } catch (final MalformedURLException e) {
            // Throw business exception
            final String portError = "No se pudo obtener correctamente el puerto de la URL.";
            logger.error(portError);
            throw new MessageProcessorException(portError, e);
        }
    }
}
