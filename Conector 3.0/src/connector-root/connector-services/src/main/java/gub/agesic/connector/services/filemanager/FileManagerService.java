package gub.agesic.connector.services.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

import gub.agesic.connector.exceptions.ConnectorException;

/**
 * Interfaz que provee métodos para el manejo de archivos en el sistema.
 * Permite: subir archivos, obtener WSDL de un Conector, crear/borrar directorio
 * de un Conector
 */
public interface FileManagerService {

    void changeExtensionXMLToWSDL(long connectorId) throws ConnectorException;

    Path createConnectorDirectory(String connectorId) throws ConnectorException;

    void deleteConnectorDirectory(String connectorId) throws ConnectorException;

    void deleteConnectorDirectoryFiles(String connectorId, boolean deleteOnlyWsdls)
            throws ConnectorException;

    void deletePrefixFilesInTemp(String prefixName) throws ConnectorException;

    boolean existsFile(Path filePath);

    String getConnectorDirectory(String connectorId);

    Path getConnectorXML(String prefixNameConnector) throws ConnectorException;

    String getGlobalConfigurationDirectory(String type) throws IOException;

    Path getConnectorWSDL(long id, String prefixNameConnector) throws ConnectorException;

    String getFileExtension(String filename) throws ConnectorException;

    Path getFilePathInUploadTempFolder(String filename);

    boolean isConnectorDirectory(String connectorId);

    void moveTempFilesToConnectorDirectory(String prefixNameConnector, Path destinationFolderPath)
            throws ConnectorException;

    void unZip(String zipFileName, String outputFolder, String prefixName)
            throws ConnectorException;

    Path uploadFile(final MultipartFile file) throws ConnectorException;

    Path uploadFileToPath(MultipartFile file, Path newFilePath) throws ConnectorException;

    String uploadFileWithPrefix(MultipartFile file) throws ConnectorException;

    Path getConnectorXSD(long id, String xsdFileName) throws ConnectorException;

    MultipartFile getConnectorWSDLNewFile(long id, String prefixNameConnector)
            throws ConnectorException;

    File getConnectorWSDLAndSchemasOnZipFile(Path filePath) throws ConnectorException;
}
