/**
 *
 */
package gub.agesic.connector.integration.support;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * @author guzman.llambias
 *
 */
public class ConectorHttpMessageConverter extends AbstractHttpMessageConverter<InputStream> {

    public ConectorHttpMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return byte[].class == clazz || String.class == clazz
                || InputStream.class.isAssignableFrom(clazz);
    }

    @Override
    protected InputStream readInternal(final Class<? extends InputStream> clazz,
            final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return inputMessage.getBody();
    }

    @Override
    protected void writeInternal(final InputStream inputStream,
            final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        IOUtils.copy(inputStream, outputMessage.getBody());
    }
}
