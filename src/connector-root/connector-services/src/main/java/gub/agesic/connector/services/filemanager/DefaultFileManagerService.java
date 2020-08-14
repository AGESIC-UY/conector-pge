package gub.agesic.connector.services.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.xpathparser.XPathParserService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

@Configuration
@PropertySource("classpath:connector-pge.properties")
@Service
public class DefaultFileManagerService implements FileManagerService {

    public static final String FILE_SEPARATOR = File.separator;
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String ZIP = ".zip";
    public static final String XSD = ".xsd";
    public static final String XML = ".xml";
    public static final String WSDL = ".wsdl";
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(ZIP, WSDL);
    private static final String TEMPDIR = "java.io.tmpdir";

    public static final String ERROR_WSDL_NO_ENCONTRADO_PARA_CONECTOR = "ERROR: No se encontró ningún WSDL para el conector con ID: ";
    public static final String ERROR_NO_SE_PUDO_CREAR_EL_DIRECTORIO = "ERROR: No se pudo crear el directorio ";
    public static final String ERROR_NO_SE_PUDO_BORRAR_EL_DIRECTORIO = "ERROR: No se pudo borrar el directorio ";
    public static final String ERROR_NO_SE_PUDO_OBTENER_EL_CONECTOR = "ERROR: No se pudo obtener el conector.";
    public static final String ERROR_NO_SE_PUDO_CAMBIAR_EXTENSION_XML_A_WSDL = "ERROR: No se pudo cambiar la extensión XML a WSDL.";
    public static final String ERROR_NO_SE_PUDIERON_BORRAR_LOS_ARCHIVOS_DEL_DIRECTORIO = "ERROR: No se pudieron borrar los archivos del directorio ";
    public static final String ERROR_NO_SE_BORRARON_LOS_ARCHIVOS_TEMPORALES_ASOCIADOS_AL_CONECTOR = "ERROR: No se borraron los archivos temporales asociados al Conector";
    public static final String ERROR_NO_SE_PUDO_MOVER_LOS_ARCHIVOS_AL_DIRECTORIO_DEL_CONECTOR = "ERROR: No se pudo mover los archivos al directorio del Conector";
    public static final String ERROR_SELECCIONE_UN_ARCHIVO_PARA_SUBIR = "ERROR: Seleccione un archivo para subir.";
    public static final String ERROR_INTERNO_AL_CERRAR_STREAM = "ERROR: Hubo un error interno al cerrar el stream.";
    public static final String ERROR_NO_ERA_UN_ARCHIVO_ZIP = "ERROR: No era un archivo ZIP";
    public static final String ERROR_NO_SE_PUDO_DESCOMPRIMIR_EL_ZIP = "ERROR: No se pudo descomprimir el ZIP";
    public static final String ERROR_NO_SE_PUDO_ELIMINAR_ZIP = "ERROR: No se pudo eliminar el ZIP subido";
    public static final String ERROR_NO_SE_PUDO_SUBIR_ARCHIVO = "ERROR: No se pudo subir el archivo.";
    public static final String ERROR_NO_SE_PUDO_AGREGAR_PREFIJO_A_ARCHIVOS = "ERROR: No se pudo agregar el prefijo a los archivos.";
    public static final String ERROR_NO_SE_PUDO_OBTENER_EL_XSD = "ERROR: No se pudo obtener el xsd asociado al WSDL.";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFileManagerService.class);
    public static final String ERROR_ARCHIVO_SIN_EXTENSION = "Error: archivo sin extensión: ";

    public final String globalConfigurationFolderPrefix = "globalConfiguration_";
    public final String uploadFolder;
    public final String uploadTempFolder;
    private final XPathParserService xPathParserService;

    @Autowired
    public DefaultFileManagerService(@Value("${uploadFolder}") final String uploadFolder,
            final XPathParserService xPathParserService) {
        super();
        this.xPathParserService = xPathParserService;
        if (uploadFolder.endsWith(FILE_SEPARATOR)) {
            this.uploadFolder = uploadFolder;
        } else {
            this.uploadFolder = uploadFolder + FILE_SEPARATOR;
        }
        uploadTempFolder = System.getProperty(TEMPDIR) + FILE_SEPARATOR;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void changeExtensionXMLToWSDL(final long connectorId) throws ConnectorException {
        DirectoryStream<Path> res = null;
        try {
            res = Files.newDirectoryStream(Paths.get(uploadFolder + connectorId),
                    path -> isXML(path));
            for (final Path xmlPath : res) {
                if (xPathParserService.isWSDLFile(xmlPath)) {
                    final File xmlFile = xmlPath.toFile();
                    final String newFileName = xmlFile.getAbsolutePath().substring(0,
                            xmlFile.getAbsolutePath().length() - 4) + WSDL;
                    Files.move(xmlPath, Paths.get(newFileName));
                    return;
                }
            }
            final String errorMessage = ERROR_WSDL_NO_ENCONTRADO_PARA_CONECTOR + connectorId;
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_CAMBIAR_EXTENSION_XML_A_WSDL;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (final IOException e) {
                final String errorMessage = ERROR_INTERNO_AL_CERRAR_STREAM;
                LOGGER.error(errorMessage, e);
            }
        }
    }

    @Override
    public Path createConnectorDirectory(final String connectorId) throws ConnectorException {
        final Path destinationFolderPath = Paths.get(getConnectorDirectory(connectorId));
        try {
            Files.createDirectory(destinationFolderPath);
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_CREAR_EL_DIRECTORIO
                    + destinationFolderPath;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
        return destinationFolderPath;
    }

    @Override
    public void deleteConnectorDirectory(final String connectorId) throws ConnectorException {
        final Path destinationFolderPath = Paths.get(uploadFolder + connectorId);
        deleteConnectorDirectoryFiles(connectorId, false);
        try {
            Files.deleteIfExists(destinationFolderPath);
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_BORRAR_EL_DIRECTORIO
                    + destinationFolderPath;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public void deleteConnectorDirectoryFiles(final String connectorId,
            final boolean deleteOnlyWsdls) throws ConnectorException {
        final Path destinationFolderPath = Paths.get(uploadFolder + connectorId);
        try {
            if (destinationFolderPath.toFile().isDirectory()) {
                if (deleteOnlyWsdls) {
                    Files.walk(destinationFolderPath, FileVisitOption.FOLLOW_LINKS)
                            .filter(path -> (isWSDL(path) || isXSD(path))).map(Path::toFile)
                            .forEach(File::delete);
                } else {
                    Files.walk(destinationFolderPath, FileVisitOption.FOLLOW_LINKS)
                            .filter(path -> path.toFile().isFile()).map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDIERON_BORRAR_LOS_ARCHIVOS_DEL_DIRECTORIO
                    + destinationFolderPath;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public void deletePrefixFilesInTemp(final String prefixName) throws ConnectorException {
        try {
            final List<Path> filesInDirectory = Files.list(Paths.get(uploadTempFolder))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().startsWith(prefixName))
                    .collect(Collectors.toList());

            for (final Path path : filesInDirectory) {
                LOGGER.debug("Archivo " + path + " eliminado !");
                Files.deleteIfExists(path);
            }
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_BORRARON_LOS_ARCHIVOS_TEMPORALES_ASOCIADOS_AL_CONECTOR;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public boolean existsFile(final Path filePath) {
        return filePath.toFile().exists();
    }

    @Override
    public String getConnectorDirectory(final String connectorId) {
        return uploadFolder + connectorId + FILE_SEPARATOR;
    }

    @Override
    public String getGlobalConfigurationDirectory(final String type) throws IOException {
        final String globalConfigurationTypePath = uploadFolder + globalConfigurationFolderPrefix
                + type + FILE_SEPARATOR;
        if (!Files.exists(Paths.get(globalConfigurationTypePath))) {
            Files.createDirectory(Paths.get(globalConfigurationTypePath));
        }
        return globalConfigurationTypePath;
    }

    @Override
    public Path getConnectorWSDL(final long id, final String prefixNameConnector)
            throws ConnectorException {
        DirectoryStream<Path> res = null;
        try {
            if (prefixNameConnector == null) {
                res = Files.newDirectoryStream(Paths.get(uploadFolder + id), path -> isWSDL(path));
            } else {
                res = Files.newDirectoryStream(Paths.get(uploadTempFolder),
                        path -> (path.getFileName().toString().startsWith(prefixNameConnector)
                                && isWSDL(path)));
            }
            final Iterator<Path> it = res.iterator();
            if (it.hasNext()) {
                return it.next();
            } else {
                // Se utiliza cuando se subió un WSDL con una extensión distinta
                // a .wsdl
                // Cuando se exportan connectores desde Connector-PGE v2.0,
                // algunos WSDLs tienen extensión .xml
                changeExtensionXMLToWSDL(id);
                return getConnectorWSDL(id, prefixNameConnector);
            }
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_OBTENER_EL_CONECTOR;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final ConnectorException e) {
            throw e;
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (final IOException e) {
                final String errorMessage = ERROR_INTERNO_AL_CERRAR_STREAM;
                LOGGER.error(errorMessage, e);
            }
        }
    }

    @Override
    public Path getConnectorXML(final String prefixNameConnector) throws ConnectorException {
        DirectoryStream<Path> res = null;
        try {
            res = Files.newDirectoryStream(Paths.get(uploadTempFolder),
                    path -> (path.getFileName().toString().startsWith(prefixNameConnector)
                            && isXML(path)));
            return res.iterator().next();
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_OBTENER_EL_CONECTOR;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (final IOException e) {
                final String errorMessage = ERROR_INTERNO_AL_CERRAR_STREAM;
                LOGGER.error(errorMessage, e);
            }
        }
    }

    public String getCurrentTime() {
        final LocalDateTime now = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return now.format(formatter);
    }

    @Override
    public String getFileExtension(final String filename) throws ConnectorException {
        if (filename.lastIndexOf('.') > 0) {
            return filename.substring(filename.lastIndexOf('.'));
        } else {
            throw new ConnectorException(ERROR_ARCHIVO_SIN_EXTENSION + filename);
        }

    }

    @Override
    public Path getFilePathInUploadTempFolder(final String filename) {
        return Paths.get(uploadTempFolder + filename);
    }

    private String getFilenameWithoutDate(final Path path) {
        return path.getFileName().toString().substring(DATE_FORMAT.length());
    }

    @Override
    public boolean isConnectorDirectory(final String connectorId) {
        final Path destinationFolderPath = Paths.get(uploadFolder + connectorId);
        return destinationFolderPath.toFile().isDirectory();
    }

    private boolean isWSDL(final Path path) {
        return path.toFile().isFile() && path.toString().endsWith(WSDL);
    }

    private boolean isXML(final Path path) {
        return path.toFile().isFile() && path.toString().endsWith(XML);
    }

    private boolean isXSD(final Path path) {
        return path.toFile().isFile() && path.toString().endsWith(XSD);
    }

    @Override
    public void moveTempFilesToConnectorDirectory(final String prefixNameConnector,
            final Path connectorDirectoryPath) throws ConnectorException {
        try {

            final List<Path> filesInDirectory = Files.list(Paths.get(uploadTempFolder))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().startsWith(prefixNameConnector))
                    .collect(Collectors.toList());

            Path destinationFilePath;
            for (final Path path : filesInDirectory) {
                destinationFilePath = Paths.get(
                        connectorDirectoryPath + FILE_SEPARATOR + getFilenameWithoutDate(path));
                Files.move(path, destinationFilePath);
            }
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_MOVER_LOS_ARCHIVOS_AL_DIRECTORIO_DEL_CONECTOR;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public void unZip(final String zipFileName, final String outputFolder, final String prefixName)
            throws ConnectorException {

        try {
            if (ZIP.equals(getFileExtension(zipFileName))) {

                Path outputPath = Paths.get(outputFolder);
                java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zipFileName);
                Enumeration<? extends ZipEntry> zipEntries = zf.entries();
                while (zipEntries.hasMoreElements()) {

                    ZipEntry entry = zipEntries.nextElement();
                    if (!entry.isDirectory()) {
                        String entryName = entry.getName();
                        int lastIndex = entryName.lastIndexOf('/');
                        String fileName = lastIndex >= 0 ? entry.getName().substring(lastIndex + 1) : entryName;
                        //Evitar ficheros ocultos
                        if (!fileName.startsWith(".")) {
                            Path fileToCreate = outputPath.resolve(prefixName + fileName);
                                Files.copy(zf.getInputStream(entry), fileToCreate);
                        }
                    }

                }

            } else {
                final String errorMessage = ERROR_NO_ERA_UN_ARCHIVO_ZIP;
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            }
        } catch (Exception e) {
            final String errorMessage = ERROR_NO_SE_PUDO_DESCOMPRIMIR_EL_ZIP;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } finally {
            try {
                Files.deleteIfExists(Paths.get(zipFileName));
            } catch (final Exception e) {
                LOGGER.error(ERROR_NO_SE_PUDO_ELIMINAR_ZIP, e);
            }
        }
    }

    @Override
    public Path uploadFile(final MultipartFile file) throws ConnectorException {
        LOGGER.debug("uploadFile()");
        try {
            if (file.isEmpty()) {
                final String errorMessage = ERROR_SELECCIONE_UN_ARCHIVO_PARA_SUBIR;
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            }

            final byte[] bytes = file.getBytes();
            final Path originalFilePath = Paths.get(uploadTempFolder + file.getOriginalFilename());
            Files.write(originalFilePath, bytes);
            return originalFilePath;
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_SUBIR_ARCHIVO;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public Path uploadFileToPath(final MultipartFile file, final Path newFilePath)
            throws ConnectorException {
        final Path originalFilePath = uploadFile(file);
        try {
            Files.move(originalFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            return newFilePath;
        } catch (final IOException e) {
            final String errorMessage = ERROR_NO_SE_PUDO_SUBIR_ARCHIVO;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public String uploadFileWithPrefix(final MultipartFile file) throws ConnectorException {
        final Path originalFilePath = uploadFile(file);
        try {
            final String prefixName = getCurrentTime();

            if (getFileExtension(file.getOriginalFilename()).equals(ZIP)) {
                unZip(uploadTempFolder + file.getOriginalFilename(), uploadTempFolder, prefixName);
            } else {
                final Path newPath = Paths
                        .get(uploadTempFolder + prefixName + file.getOriginalFilename());
                Files.move(originalFilePath, newPath);
            }
            return prefixName;
        } catch (final Exception e) {
            final String errorMessage = ERROR_NO_SE_PUDO_AGREGAR_PREFIJO_A_ARCHIVOS;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public Path getConnectorFile(final long id, final String fileName) throws ConnectorException {
        return FileUtils.getFile(uploadFolder + id, fileName).toPath();
    }

    @Override
    public MultipartFile getConnectorWSDLNewFile(final long id, final String prefixNameConnector)
            throws ConnectorException {
        FileItem fileItem = null;
        final Path filePath = getConnectorWSDL(id, prefixNameConnector);
        final File file = filePath.toFile();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            fileItem = new DiskFileItem(file.getName(), Files.probeContentType(file.toPath()),
                    false, file.getName(), (int) file.length(), file.getParentFile());
            inputStream = new FileInputStream(file);
            outputStream = fileItem.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            final MultipartFile multipartWsdl = new CommonsMultipartFile(fileItem);
            return multipartWsdl;
        } catch (final IOException ex) {
            final String errorMessage = "ERROR: No se pudo encontrar el wsld " + filePath;
            throw new ConnectorException(errorMessage, ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException exception) {
                    throw new ConnectorException("Ocurrió un error interno inesperado", exception);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (final IOException exception) {
                    throw new ConnectorException("Ocurrió un error interno inesperado", exception);
                }
            }
        }
    }

    @Override
    public File getConnectorWSDLAndSchemasOnZipFile(final Path filePath) throws ConnectorException {
        final File fileDirectory = filePath.toFile().getParentFile();
        if (!fileDirectory.isDirectory()) {
            throw new ConnectorException("Could not find directory of WSDL and Schemas");
        }

        final ArrayList<File> filesToZip = new ArrayList<File>();
        for (final File file : fileDirectory.listFiles()) {
            if (isWSDLorXSDFile(file)) {
                filesToZip.add(file);
            }
        }

        final ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

        try {
            final String zipFilePath = System.getProperty(TEMPDIR) + FILE_SEPARATOR
                    + UUID.randomUUID() + ".zip";
            final ZipFile zipFile = new ZipFile(zipFilePath);
            zipFile.addFiles(filesToZip, parameters);

            return new File(zipFilePath);
        } catch (final ZipException zipex) {
            throw new ConnectorException("Error trying to zip wsdl and xml schema files", zipex);
        }
    }

    private boolean isWSDLorXSDFile(final File file) throws ConnectorException {
        final String extension = this.getFileExtension(file.getPath());
        return WSDL.equals(extension) || XSD.equals(extension);
    }
}