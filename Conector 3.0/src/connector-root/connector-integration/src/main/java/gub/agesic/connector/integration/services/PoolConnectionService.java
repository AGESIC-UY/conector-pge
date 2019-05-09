package gub.agesic.connector.integration.services;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import gub.agesic.connector.exceptions.ConnectorException;

public interface PoolConnectionService {

    PoolingHttpClientConnectionManager getPoolConnectionManagerByUrl(final String url,
            final Registry<ConnectionSocketFactory> registry) throws ConnectorException;
}
