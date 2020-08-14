package gub.agesic.connector.services.connectorparser;

import java.nio.file.Path;

import org.springframework.ui.Model;

/**
 * Interfaz que provee métodos para importar y exportar conectores.
 */

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.exceptions.ConnectorException;

public interface ConnectorParserService {

    Path exportConnectorData(Connector connector) throws ConnectorException;

    Connector importConnectorData(Model model, String prefixNameConnector, Connector connector)
            throws ConnectorException;

}
