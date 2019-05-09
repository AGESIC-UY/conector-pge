/**
 *
 */
package gub.agesic.connector.integration.actions;

import org.springframework.messaging.Message;

/**
 * @author guzman.llambias
 *
 */
public class TimestampEnricher {

    public long enrich(final Message<?> message) {
        return System.currentTimeMillis();
    }
}
