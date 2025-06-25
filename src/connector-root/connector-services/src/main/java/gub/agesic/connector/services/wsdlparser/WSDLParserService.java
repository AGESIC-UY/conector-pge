package gub.agesic.connector.services.wsdlparser;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.exceptions.ConnectorException;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Interfaz que provee métodos para el parseo de WSDLs.
 */
public interface WSDLParserService {

    Connector getWSDLData(Model model, String prefixNameConnector, Connector connector)
            throws ConnectorException;

    void modifyLocationAndSave(final MultipartFile file, final String location, final Path filePath, String connectorPath)
            throws ConnectorException;

    void commentXSDImportTags(Path wsdlPath) throws ConnectorException;
}
