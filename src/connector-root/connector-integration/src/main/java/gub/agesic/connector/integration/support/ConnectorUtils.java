package gub.agesic.connector.integration.support;

import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import gub.agesic.connector.integration.actions.MessageProcessorException;

/**
 * Created by adriancur on 30/11/17.
 */
public class ConnectorUtils {

    public static MediaType getContentType(final Message message) throws MessageProcessorException {
        // Content-type on tomcat, contentType on wildfly
        MediaType mediaType = (MediaType) message.getHeaders().get("content-type");
        if (mediaType == null) {
            mediaType = (MediaType) message.getHeaders().get(MessageHeaders.CONTENT_TYPE);
        }
        if (mediaType == null) {
            throw new MessageProcessorException("No se encuentra el cabezal http content-type");
        }
        return mediaType;
    }
}
