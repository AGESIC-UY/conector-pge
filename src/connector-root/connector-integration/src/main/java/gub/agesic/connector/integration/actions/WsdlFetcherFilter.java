/**
 *
 */
package gub.agesic.connector.integration.actions;

import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;

public class WsdlFetcherFilter implements MessageSelector {

    @Override
    public boolean accept(Message<?> message) {
        String requestUrl = (String) message.getHeaders().get(org.springframework.integration.http.HttpHeaders.REQUEST_URL);
        return requestUrl.endsWith("?wsdl");
    }
}
