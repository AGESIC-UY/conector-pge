package gub.agesic.connector.services.connectorparser;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.exceptions.ConnectorException;
import org.springframework.ui.Model;

import java.nio.file.Path;

/**
 * Interfaz que provee métodos para importar y exportar servicios.
 */
public interface ConnectorParserService {

    Path exportConnectorData(Connector connector) throws ConnectorException;

    Connector importConnectorData(Model model, String prefixNameConnector, Connector connector)
            throws ConnectorException;

}
