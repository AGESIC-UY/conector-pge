package gub.agesic.connector.integration.actions;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class RejectedMessage {
    private final Logger logger = Logger.getLogger(RejectedMessage.class);

    public Message<String> process(final Message<String> message) {
        try {
            return MessageBuilder
                    .withPayload("Solicitud incorrecta")
                    .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .setHeader(HttpHeaders.STATUS_CODE, HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

}
