package gub.agesic.connector.integration.controller;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class ConnectorWsdlService {

    public static final String DANGER = "danger";
    public static final String CSS = "css";
    public static final String MSG = "msg";

    public static final String ERROR_NO_EXISTE_CONNECTOR = "ERROR: No existe un Connector con ID ";
    public static final String ERROR_NO_EXISTE_WSDL_DEL_CONECTOR = "ERROR: No existe un WSDL para ese Conector";
    public static final String ERROR_AL_COPIAR_WSDL_AL_RESPONSE = "Error al acceder al archivo wsdl del conectorId :";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorWsdlService.class);
    @Autowired
    private FileManagerService fileManagerService;

    @Autowired
    private ConnectorService connectorService;

    public byte[] getConnectorWSDL(String path, String port) throws ConnectorException {

        final Optional<Connector> connector = getConnectorPathAndPort(path, port, null);
        if (connector.isPresent()) {
            try {
                Path pathWSDL = fileManagerService.getConnectorWSDL(connector.get().getId(), null);
                if (pathWSDL.toFile().exists()) {
                    return Files.readAllBytes(pathWSDL);
                }
                return null;
            } catch (Exception e) {
                final String errorMessage = ERROR_NO_EXISTE_WSDL_DEL_CONECTOR;
                LOGGER.error(errorMessage, e);
                throw new ConnectorException(errorMessage, e);
            }
        } else {
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR);
            throw new ConnectorException(ERROR_NO_EXISTE_CONNECTOR);
        }
    }

    public byte[] getConnectorFile(String path, String port, String fileName) throws ConnectorException {

        final Optional<Connector> connector = getConnectorPathAndPort(path, port, null);
        if (connector.isPresent()) {
            try {
                Path pathXSD = fileManagerService.getConnectorFile(connector.get().getId(), fileName);
                if (pathXSD.toFile().exists()) {
                    return Files.readAllBytes(pathXSD);
                }
                return null;
            } catch (Exception e) {
                LOGGER.error(ERROR_NO_EXISTE_WSDL_DEL_CONECTOR, e);
                throw new ConnectorException(ERROR_NO_EXISTE_WSDL_DEL_CONECTOR, e);
            }
        } else {
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR);
            throw new ConnectorException(ERROR_NO_EXISTE_CONNECTOR);
        }
    }

    public Optional<Connector> getConnectorPathAndPort(final String path, final String port, final RedirectAttributes redirectAttributes) {

        final ConnectorTypeHolder connectorTypeHolder;
        Optional<Connector> connector = Optional.empty();

        try {
            connectorTypeHolder = connectorService.getConnectorTypeByPort(port);
            connector = connectorService.getConnectorByPathAndPort(path, connectorTypeHolder);
        } catch (ConnectorException e) {
            LOGGER.error(e.getMessage());
        }

        if (!(connector != null && connector.isPresent())) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + path);
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR + path);
        }

        return connector;
    }

    private void saveFileToResponse(final long connectorId, final HttpServletResponse response,
                                    final Path file) throws ConnectorException {
        try {
            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (final IOException e) {
            final String errorMessage = ERROR_AL_COPIAR_WSDL_AL_RESPONSE + connectorId;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

    }
}
