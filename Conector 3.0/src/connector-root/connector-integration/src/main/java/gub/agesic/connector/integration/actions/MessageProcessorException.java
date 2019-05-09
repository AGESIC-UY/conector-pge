/**
 *
 */
package gub.agesic.connector.integration.actions;

/**
 * Exception to be used only for {@link MessageProcessor}
 *
 * @author guzman.llambias
 *
 */
@SuppressWarnings("serial")
public class MessageProcessorException extends Exception {

    public MessageProcessorException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public MessageProcessorException(final String message) {
        super(message);
    }
}
