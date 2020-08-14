package gub.agesic.connector.integration.actions;

import gub.agesic.connector.integration.controller.ConnectorWsdlService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class WsdlFilesFetcher {

    private static final String REGEX_PATH = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?";
    private final Logger LOGGER = Logger.getLogger(WsdlFilesFetcher.class);

    private ConnectorWsdlService connectorWsdlService;

    public WsdlFilesFetcher(ConnectorWsdlService connectorWsdlService) {
        this.connectorWsdlService = connectorWsdlService;
    }

    public Message<byte[]> process(final Message<String> message) throws MessageProcessorException {
        try {
            final String fileName = getFileName(message);
            final String path = getPathFromUrl(message, fileName);
            final String port = getPortFromUrl(message);

            byte[] xsdInBytes = connectorWsdlService.getConnectorFile(path, port, fileName);

            return MessageBuilder
                    .withPayload(xsdInBytes)
                    .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                    .build();

        } catch (Exception e) {
            LOGGER.error(e);
            throw new MessageProcessorException("El XSD solicitado no esta asociado al conector");
        }
    }

    private String getPathFromUrl(final Message<String> message, final String fileName) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        String path = url.replaceFirst(REGEX_PATH, "$5");
        return path.replace("/" + fileName, "");
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
            LOGGER.error(portError);
            throw new MessageProcessorException(portError, e);
        }
    }

    private String getFileName(Message<String> message) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        String path = url.replaceFirst(REGEX_PATH, "$5");
        int from = path.lastIndexOf("/") + 1;

        return path.substring(from);
    }
}
