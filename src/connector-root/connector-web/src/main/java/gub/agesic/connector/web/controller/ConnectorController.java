package gub.agesic.connector.web.controller;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.KeystoreModalData;
import gub.agesic.connector.dataaccess.repository.ConnectorType;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.connectorparser.ConnectorParserService;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;
import gub.agesic.connector.services.keystoremanager.KeystoreManagerService;
import gub.agesic.connector.services.wsdlparser.WSDLParserService;
import gub.agesic.connector.web.servlet3.MyWebInitializer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.*;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.*;

@Controller
@SessionAttributes(value = {"type", "tag"})
public class ConnectorController {

    public static final String CONNECTOR = "connector";
    public static final String VIEW_EDIT_1ST_STEP = "edit";
    public static final String VIEW_EDIT_2ND_STEP = "edit2";
    public static final String VIEW_CONNECTOR = "viewConnector";
    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";
    public static final String REDIRECT_TO_EDIT = "redirect:/connectors/add";
    public static final String DANGER = "danger";
    public static final String SUCCESS = "success";
    public static final String CSS = "css";
    public static final String MSG = "msg";

    public static final String PATH_UPLOAD_IMPORT = "/connectors/import";
    public static final String PATH_UPLOAD_ADD = "/connectors/add/uploadFile";

    public static final String CONECTOR_CREADO_EXITOSAMENTE = "Conector creado exitosamente !";
    public static final String CONECTOR_IMPORTADO_EXITOSAMENTE = "Conector importado exitosamente ! En caso de importar una configuración local, no olvide ingresar las contraseñas";
    public static final String CONECTOR_ACTUALIZADO_EXITOSAMENTE = "Conector actualizado exitosamente !";
    public static final String CONECTOR_ELIMINADO_EXITOSAMENTE = "Conector eliminado exitosamente !";
    public static final String CREACION_NUEVO_CONECTOR_CANCELADA = "Se canceló la creación del nuevo Conector !";
    public static final String ERROR_EXTENSION_DE_ARCHIVO_1 = "El archivo tiene extensión ";
    public static final String ERROR_EXTENSION_DE_ARCHIVO_2 = "debe subir un archivo con extensión: ";

    public static final String ERROR_NO_EXISTE_CONNECTOR = "ERROR: No existe un Connector con ID ";
    public static final String ERROR_ARCHIVO_INVALIDO = "ERROR: Debes subir un archivo con extensión: ";
    public static final String ERROR_CONECTOR_YA_EXISTENTE = "ERROR: Ese Conector ya existe !";
    public static final String ERROR_NO_SE_ENCONTRO_CONECTOR_MODIFICADO = "ERROR: No se encontró el conector modificado";
    public static final String ERROR_NO_SE_MOVIERON_ARCHIVOS_A_DIRECTORIO_DEL_CONECTOR = "ERROR: No se pudo mover los archivos al directorio del conector con ID ";
    public static final String ERROR_NO_SE_PUDO_ELIMINAR_CONECTOR = "ERROR: No se pudo eliminar el Conector ";
    public static final String ERROR_NO_SE_PUDO_ENCONTRAR_KEYSTORE = "ERROR: No se pudo encontrar ";
    public static final String ERROR_NO_EXISTE_WSDL_DEL_CONECTOR = "ERROR: No existe un WSDL para ese Conector";
    public static final String ERROR_NO_SE_PUDO_PROCESAR_CORRECTAMENTE_WSDL = "ERROR: No se pudo procesar correctamente el WSDL y XMLSchemas asociados. Revise las rutas de los archivos XSD que contiene el WSDL";

    public static final String NO_EXISTE_CONNECTOR_ID = "No existe un Connector con ID ";
    public static final String NO_EXISTE_CONNECTOR_ID_PARAM = "No existe un Connector con ID {}";
    public static final String KEYSTORESMODALDACOLL = "keystoreModalDataColl";
    public static final String KEYSTORE_SSL_MODALNAME = "Keystore SSL";
    public static final String KEYSTORE_ORG_MODALNAME = "Keystore Org";
    public static final String KEYSTORE_TRUSTSTORE_MODALNAME = "Truststore";
    public static final String ERROR_AL_COPIAR_WSDL_AL_RESPONSE = "Error al acceder al archivo wsdl del conectorId :";
    public static final String TYPE_CONNECTOR = "type";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorController.class);
    @Autowired
    private KeystoreManagerService keystoreManagerService;
    @Autowired
    private FileManagerService fileManagerService;
    @Autowired
    private WSDLParserService wsdlParserService;
    @Autowired
    private ConnectorParserService connectorParserService;
    @Autowired
    private ConnectorService connectorService;

    @GetMapping("/connectors/connector/{id}")
    public ModelAndView getConnector(@PathVariable("id") final long connectorId,
                                     final RedirectAttributes redirectAttributes) {

        return getConnectorView(connectorId, redirectAttributes, VIEW_CONNECTOR);
    }

    @GetMapping("/connectors/connector/{id}/wsdl")
    public void getConnectorWSDL(@PathVariable("id") final long connectorId,
                                 final HttpServletResponse response, final RedirectAttributes redirectAttributes,
                                 @RequestParam(value = "download", required = false, defaultValue = "false") final boolean isDownload)
            throws ConnectorException {

        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        final String errorMessage = ERROR_NO_EXISTE_WSDL_DEL_CONECTOR;
        if (connector != null) {
            try {
                Path path = fileManagerService.getConnectorWSDL(connectorId, null);
                if (path.toFile().exists()) {
                    response.setContentType("application/xml");
                    String contentDispositionValue = "inline";
                    if (isDownload) {
                        /*
                         * When download, allways download as zip file as
                         * connector may have only one wsdl or one wsdl with
                         * many xsds
                         */
                        final File zipFile = fileManagerService
                                .getConnectorWSDLAndSchemasOnZipFile(path);
                        path = zipFile.toPath();
                        contentDispositionValue = "attachment";
                    }
                    response.addHeader("Content-Disposition",
                            contentDispositionValue + "; filename=" + path.getFileName());
                    saveFileToResponse(connectorId, response, path);
                } else {
                    throw new ConnectorException(errorMessage);
                }
            } catch (final ConnectorException e) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
        }
    }

//    @GetMapping("/connectors/connector/{id}/{xsdFileName}.xsd")
//    public void getXsdWSDL(@PathVariable("id") final long connectorId,
//                           @PathVariable("xsdFileName") final String xsdFileName,
//                           final HttpServletResponse response, final RedirectAttributes redirectAttributes)
//            throws ConnectorException {
//
//        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
//        final String errorMessage = ERROR_NO_EXISTE_WSDL_DEL_CONECTOR;
//        if (connector != null) {
//            try {
//                final Path file = fileManagerService.getConnectorXSD(connectorId, xsdFileName);
//                if (file.toFile().exists()) {
//                    response.setContentType("application/xml");
//                    response.addHeader("Content-Disposition",
//                            "inline; filename=" + file.getFileName());
//                    saveFileToResponse(connectorId, response, file);
//                } else {
//                    throw new ConnectorException(errorMessage);
//                }
//            } catch (final ConnectorException e) {
//                redirectAttributes.addFlashAttribute(CSS, DANGER);
//                redirectAttributes.addFlashAttribute(MSG, e.getMessage());
//            }
//        } else {
//            redirectAttributes.addFlashAttribute(CSS, DANGER);
//            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
//        }
//    }

    @GetMapping("/connectors/connector/{id}/keystoreOrg")
    public void getConnectorKeystoreOrg(@PathVariable("id") final long connectorId,
                                        final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getConnectorKeystore(connectorId, response, redirectAttributes, KEYSTORE_ORG_FILENAME);
    }

    @GetMapping("/connectors/connector/{id}/keystoreSsl")
    public void getConnectorKeystoreSsl(@PathVariable("id") final long connectorId,
                                        final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getConnectorKeystore(connectorId, response, redirectAttributes, KEYSTORE_SSL_FILENAME);
    }

    @GetMapping("/connectors/connector/{id}/truststore")
    public void getConnectorTruststore(@PathVariable("id") final long connectorId,
                                       final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        getConnectorKeystore(connectorId, response, redirectAttributes,
                KEYSTORE_TRUSTSTORE_FILENAME);
    }

    @GetMapping("/connectors/add")
    public ModelAndView createConnectorStep1(Model model) {
        int maxUploadSize = MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE * 1024 * 1024;
        try {
            maxUploadSize = connectorService.getMaxUploadSize();
        } catch (ConnectorException e) {
            LOGGER.info(e.getMessage() + "Se utiliza el valor por defecto: " + MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE);
        }

        model.addAttribute("max_upload_size", maxUploadSize);

        return new ModelAndView(VIEW_EDIT_1ST_STEP);
    }

    @GetMapping("/connectors/connector/{id}/edit")
    public ModelAndView editConnector(@PathVariable("id") final long connectorId,
                                      final RedirectAttributes redirectAttributes) {

        return getConnectorView(connectorId, redirectAttributes, VIEW_EDIT_1ST_STEP);
    }

    @PostMapping("/connectors/connector/{id}/add/uploadFile")
    public ModelAndView uploadAndParseNewWSDL(
            @RequestParam("uploaded_file") final MultipartFile file, final Model model,
            @PathVariable("id") final long connectorId, final RedirectAttributes redirectAttributes, @SessionAttribute("type") String type)
            throws ConnectorException {
        final Connector connector = getConnectorByID(connectorId, redirectAttributes);

        // Si el conector no existía, redirijo al listado de conectores
        if (connector == null) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR + connectorId);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } else {

            model.addAttribute(CONNECTOR, connector);

            if (connector.isEnableLocalConfiguration()) {
                // Cargo los datos de los keystores para el modal.
                loadKeystoresModal(connector, model);
            }

            // Si no se eligió ningún archivo, envío mensaje de error
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG,
                        ERROR_ARCHIVO_INVALIDO + XML + " o " + ZIP);
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
            }
            // Si el conector existe y se eligió un archivo, continúo al upload
            // de ese archivo
            else {
                fileManagerService.deleteConnectorDirectoryFiles(Long.toString(connectorId), true);
                return uploadAndParseWSDL(file, model, connector, redirectAttributes, type);
            }
        }
    }

    @PostMapping(PATH_UPLOAD_ADD)
    public ModelAndView uploadAndParseWSDL(@RequestParam("uploaded_file") final MultipartFile file,
                                           final Model model, final Connector connector,
                                           final RedirectAttributes redirectAttributes, @SessionAttribute("type") String type) throws ConnectorException {

        try {
            // Si no se eligió ningún archivo, envío mensaje de error
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG,
                        ERROR_ARCHIVO_INVALIDO + ALLOWED_EXTENSIONS);
                return new ModelAndView(REDIRECT_TO_EDIT);
            }
            final String fileExtension = fileManagerService
                    .getFileExtension(file.getOriginalFilename());

            // Si el archivo elegido no tiene una extensión válida, envío
            // mensaje de error
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, ERROR_EXTENSION_DE_ARCHIVO_1
                        + fileExtension + ", " + ERROR_EXTENSION_DE_ARCHIVO_2 + ALLOWED_EXTENSIONS);
                return new ModelAndView(REDIRECT_TO_EDIT);
            }

            // Upload del archivo y descompresión en caso de que sea un .zip
            final String prefixNameConnector = fileManagerService.uploadFileWithPrefix(file);
            final Connector newConnector;

            // Parseo el WSDL y cargo el conector con los datos obtenidos.
            // Si el conector no existía, lo construyo
            if (connector == null) {
                Connector emptyConnector = new Connector();
                emptyConnector.setType(type);
                newConnector = wsdlParserService.getWSDLData(model, prefixNameConnector, emptyConnector);
            } else {
                connector.setType(type);
                newConnector = wsdlParserService.getWSDLData(model, prefixNameConnector, connector);
            }
            model.addAttribute(CONNECTOR, newConnector);
            model.addAttribute("prefixNameConnector", prefixNameConnector);
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, newConnector);
        } catch (final ConnectorException exception) {
            final String errorMessage = ERROR_NO_SE_PUDO_PROCESAR_CORRECTAMENTE_WSDL;
            LOGGER.error(errorMessage, exception);
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, errorMessage);
            return new ModelAndView(REDIRECT_TO_EDIT);
        }
    }

    @PostMapping("/connectors/add")
    public ModelAndView createConnectorStep2(final @ModelAttribute(CONNECTOR) Connector connector,
                                             @RequestParam("prefixNameConnector") final String prefixNameConnector,
                                             @RequestParam(value = "keystoreOrgFile", required = false) final MultipartFile keystoreOrgFile,
                                             @RequestParam(value = "keystoreSSLFile", required = false) final MultipartFile keystoreSSLFile,
                                             @RequestParam(value = "keystoreTruststoreFile", required = false) final MultipartFile keystoreTruststoreFile,
                                             final RedirectAttributes redirectAttributes, final Model model)
            throws ConnectorException {

        try {
            connectorService.checkConnectorPathAndTypeAvailabilityForType(connector.getName(),
                    connector.getPath(), connector.getType());
        } catch (final ConnectorException e) {
            final String errorMessage = e.getMessage();
            LOGGER.error(errorMessage, e);
            model.addAttribute("prefixNameConnector", prefixNameConnector);
            model.addAttribute("keystoreOrgFile", keystoreOrgFile);
            model.addAttribute("keystoreSSLFile", keystoreSSLFile);
            model.addAttribute("keystoreTruststoreFile", keystoreTruststoreFile);
            model.addAttribute(CSS, DANGER);
            model.addAttribute(MSG, errorMessage);
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
        }

        connectorService.updateConnectorPath(connector);

        //Patrón para validar ruta:
        //Que comience por '/'
        //Luego combinaciones de caracteres alfanuméricos, underscore (_) o guion (-)
        String pattern = "([/][\\w-]+)+";
        if (!connector.getPath().matches(pattern)) {
            final String msg = "Path incorrecto. Debe contener caracteres alfanuméricos, underscore (_) o guion (-) (Ejemplo: /1_texto/texto-2/otroTextoMas)";
            LOGGER.info(msg);
            model.addAttribute(CSS, DANGER);
            model.addAttribute(MSG, msg);
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
        }

        connectorService.saveConnector(connector);
        final String connectorId = connector.getId().toString();
        if (fileManagerService.isConnectorDirectory(connectorId)) {
            model.addAttribute(CSS, DANGER);
            model.addAttribute(MSG, ERROR_CONECTOR_YA_EXISTENTE);
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
        } else {
            try {
                final Path connectorDirectoryPath = fileManagerService
                        .createConnectorDirectory(connectorId);
                fileManagerService.moveTempFilesToConnectorDirectory(prefixNameConnector,
                        connectorDirectoryPath);

                if (connector.isEnableLocalConfiguration()) {
                    keystoreManagerService.setKeystoresFilePaths(connector);
                    try {
                        keystoreManagerService.uploadKeystoresConnector(connector, keystoreOrgFile,
                                keystoreSSLFile, keystoreTruststoreFile);
                    } catch (final ConnectorException e) {
                        connectorService.deleteConnector(Long.valueOf(connectorId));
                        connector.setId(null);
                        model.addAttribute(CSS, DANGER);
                        model.addAttribute(MSG, e.getMessage());
                        return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
                    }
                } else {
                    connector.setLocalConfiguration(null);
                }

                connectorService.saveConnector(connector);

                MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(connector.getId(), null);
                final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
                final String location = connectorService.getLocationBasedOnConnector(connector);
                wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, connector.getPath());

                redirectAttributes.addFlashAttribute(CSS, SUCCESS);
                redirectAttributes.addFlashAttribute(MSG, CONECTOR_CREADO_EXITOSAMENTE);
                return new ModelAndView(REDIRECT_TO_CONNECTORS + "connector/" + connector.getId());
            } catch (final ConnectorException e) {
                connectorService.deleteConnector(connector.getId());
                model.addAttribute(CSS, DANGER);
                model.addAttribute(MSG, e.getMessage());
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
            }
        }

    }

    @PostMapping("/connectors/connector/{id}/update")
    public ModelAndView updateConnector(@ModelAttribute(CONNECTOR) final Connector updatedConnector,
                                        @RequestParam("prefixNameConnector") final String prefixNameConnector,
                                        @PathVariable("id") final long connectorId,
                                        @RequestParam(value = "keystoreOrgFile", required = false) final MultipartFile keystoreOrgFile,
                                        @RequestParam(value = "keystoreSSLFile", required = false) final MultipartFile keystoreSSLFile,
                                        @RequestParam(value = "keystoreTruststoreFile", required = false) final MultipartFile keystoreTrustoreFile,
                                        final RedirectAttributes redirectAttributes, final Model model)
            throws ConnectorException {

        if (updatedConnector == null) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_SE_ENCONTRO_CONECTOR_MODIFICADO);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } else {
            // Si se subió un nuevo archivo WSDL o ZIP, se mueve el mismo desde
            // el directorio temporal al directorio del Conector
            if (!"".equals(prefixNameConnector)) {
                final Path connectorDirectoryPath = Paths
                        .get(fileManagerService.getConnectorDirectory(String.valueOf(connectorId)));
                try {
                    fileManagerService.moveTempFilesToConnectorDirectory(prefixNameConnector,
                            connectorDirectoryPath);
                } catch (final ConnectorException e) {
                    final String errorMessage = ERROR_NO_SE_MOVIERON_ARCHIVOS_A_DIRECTORIO_DEL_CONECTOR
                            + connectorId;
                    LOGGER.error(errorMessage, e);
                    throw new ConnectorException(errorMessage, e);
                }
            }
            final Connector connector = getConnectorByID(connectorId, redirectAttributes);
            try {
                updateLocalConfigurations(updatedConnector, keystoreOrgFile, keystoreSSLFile,
                        keystoreTrustoreFile, connector);
            } catch (final ConnectorException e) {
                model.addAttribute(CSS, DANGER);
                model.addAttribute(MSG, e.getMessage());
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, updatedConnector);
            }
            updateUserCredentials(updatedConnector, connector);

            connectorService.updateConnectorPath(updatedConnector);
            connectorService.saveConnector(updatedConnector);

            final MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(connector.getId(), null);
            final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
            final String location = connectorService.getLocationBasedOnConnector(updatedConnector);
            wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, updatedConnector.getPath());

            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG, CONECTOR_ACTUALIZADO_EXITOSAMENTE);
            return new ModelAndView(
                    REDIRECT_TO_CONNECTORS + "connector/" + updatedConnector.getId());
        }
    }

    @PostMapping("/connectors/connector/cancel")
    public ModelAndView cancelCreateConnector(
            @RequestParam(value = "prefixNameConnector", required = false) final String prefixNameConnector,
            final RedirectAttributes redirectAttributes) throws ConnectorException {
        try {
            fileManagerService.deletePrefixFilesInTemp(prefixNameConnector);
            if (!"".equals(prefixNameConnector)) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, CREACION_NUEVO_CONECTOR_CANCELADA);
            }

            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } catch (final ConnectorException e) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG,
                    ERROR_NO_SE_MOVIERON_ARCHIVOS_A_DIRECTORIO_DEL_CONECTOR);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
    }

    @GetMapping("/connectors/connector/{id}/delete")
    public ModelAndView deleteConnector(@PathVariable("id") final long connectorId,
                                        final RedirectAttributes redirectAttributes) throws ConnectorException {

        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        final String connectorName;
        if (connector != null) {
            connectorName = connector.getName();
            try {
                fileManagerService.deleteConnectorDirectory(Long.toString(connectorId));
                connectorService.deleteConnector(connectorId);
                redirectAttributes.addFlashAttribute(CSS, SUCCESS);
                redirectAttributes.addFlashAttribute(MSG, CONECTOR_ELIMINADO_EXITOSAMENTE);
            } catch (final ConnectorException e) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG,
                        ERROR_NO_SE_PUDO_ELIMINAR_CONECTOR + connectorName);
                return new ModelAndView(REDIRECT_TO_CONNECTORS);
            }
        }
        return new ModelAndView(REDIRECT_TO_CONNECTORS);
    }

    @GetMapping("/connectors/connector/{id}/export")
    public void exportConnector(@PathVariable("id") final long connectorId,
                                final HttpServletResponse response, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        if (connector != null) {

            final Path connectorFilePath = connectorParserService.exportConnectorData(connector);
            if (connectorFilePath.toFile().exists()) {
                response.setContentType("application/force-download");
                response.addHeader("Content-Disposition",
                        "attachment; filename=" + connectorFilePath.getFileName());
                saveFileToResponse(connectorId, response, connectorFilePath);
            }
        }
    }

    @PostMapping(PATH_UPLOAD_IMPORT)
    public ModelAndView importConnector(@RequestParam("uploaded_file") final MultipartFile file,
                                        final Model model, final RedirectAttributes redirectAttributes)
            throws ConnectorException {

        final Connector importedConnector = new Connector();
        connectorService.saveConnector(importedConnector);
        try {
            // Si no se eligió ningún archivo, envío mensaje de error
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, ERROR_ARCHIVO_INVALIDO + XML);
                return new ModelAndView(REDIRECT_TO_CONNECTORS);
            }
            final String fileExtension = fileManagerService
                    .getFileExtension(file.getOriginalFilename());

            // Si el archivo elegido no tiene una extensión válida, envío
            // mensaje de error
            if (!XML.equals(fileExtension)) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, ERROR_EXTENSION_DE_ARCHIVO_1
                        + fileExtension + ", " + ERROR_EXTENSION_DE_ARCHIVO_2 + XML);
                return new ModelAndView(REDIRECT_TO_CONNECTORS);
            }

            // Upload del archivo XML
            final String prefixNameConnector = fileManagerService.uploadFileWithPrefix(file);
            final Connector newConnector;

            newConnector = connectorParserService.importConnectorData(model, prefixNameConnector,
                    importedConnector);
            keystoreManagerService.setKeystoresFilePaths(newConnector);
            connectorService.saveConnector(newConnector);

            final MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(newConnector.getId(), null);
            final Path path = fileManagerService.getConnectorWSDL(newConnector.getId(), null);
            final String location = connectorService.getLocationBasedOnConnector(newConnector);
            wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, newConnector.getPath());

            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG, CONECTOR_IMPORTADO_EXITOSAMENTE);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } catch (final ConnectorException exception) {
            deleteConnector(importedConnector.getId(), redirectAttributes);
            final String errorMessage = exception.getMessage();
            LOGGER.error(errorMessage, exception);
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, errorMessage);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
    }

    public void getConnectorKeystore(final long connectorId, final HttpServletResponse response,
                                     final RedirectAttributes redirectAttributes, final String keystoreName)
            throws ConnectorException {
        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        final String errorMessage = ERROR_NO_SE_PUDO_ENCONTRAR_KEYSTORE + keystoreName;
        if (connector == null) {
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } else {
            final Path keystorePath = keystoreManagerService.getConnectorKeystore(connectorId,
                    keystoreName);
            try {
                if (keystorePath.toFile().exists()) {
                    response.setContentType("application/force-download");
                    response.addHeader("Content-Disposition",
                            "attachment; filename=" + keystorePath.getFileName());
                    Files.copy(keystorePath, response.getOutputStream());
                    response.getOutputStream().flush();
                } else {
                    LOGGER.error(errorMessage);
                    redirectAttributes.addFlashAttribute(CSS, DANGER);
                    redirectAttributes.addFlashAttribute(MSG, errorMessage);
                    throw new ConnectorException(errorMessage);
                }
            } catch (final IOException e) {
                LOGGER.error(errorMessage);
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, errorMessage);
                throw new ConnectorException(errorMessage);
            }
        }
    }

    public void updateUserCredentials(final @ModelAttribute(CONNECTOR) Connector updatedConnector,
                                      final Connector connector) {
        if (updatedConnector.isEnableUserCredentials()) {
            if (connector.getUserCredentials() != null) {
                final Long userCredentialsId = connector.getUserCredentials().getId();
                updatedConnector.getUserCredentials().setId(userCredentialsId);
            }
        } else {
            updatedConnector.setUserCredentials(null);
        }
    }

    public void updateLocalConfigurations(
            final @ModelAttribute(CONNECTOR) Connector updatedConnector,
            final @RequestParam(value = "keystoreOrgFile", required = false) MultipartFile keystoreOrgFile,
            final @RequestParam(value = "keystoreSSLFile", required = false) MultipartFile keystoreSSLFile,
            final @RequestParam(value = "keystoreTruststoreFile", required = false) MultipartFile keystoreTrustoreFile,
            final Connector connector) throws ConnectorException {
        if (updatedConnector.isEnableLocalConfiguration()) {
            // Sustituye el viejo Local Configuration, si existía, con los
            // valores del nuevo
            if (connector.getLocalConfiguration() != null) {
                final Long localConfigurationId = connector.getLocalConfiguration().getId();
                updatedConnector.getLocalConfiguration().setId(localConfigurationId);
            }
            // Sube los archivos Keystores y Truststore
            keystoreManagerService.setKeystoresFilePaths(updatedConnector);
            keystoreManagerService.uploadKeystoresConnector(updatedConnector, keystoreOrgFile,
                    keystoreSSLFile, keystoreTrustoreFile);
        } else {
            updatedConnector.setLocalConfiguration(connector.getLocalConfiguration());
        }
    }

    public Connector getConnectorByID(final long connectorId,
                                      final RedirectAttributes redirectAttributes) {
        final Connector connector = connectorService.getConnector(connectorId);
        if (connector == null) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR + connectorId);
        }

        return connector;
    }

    public ModelAndView getConnectorView(final @PathVariable("id") long connectorId,
                                         final RedirectAttributes redirectAttributes, final String targetViewName) {
        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        if (connector == null) {
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } else {
            return new ModelAndView(targetViewName, CONNECTOR, connector);
        }
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

    @GetMapping("/connectors/connector/{id}/testToProd")
    public ModelAndView testToProd(@PathVariable("id") final long connectorId,
                                   final RedirectAttributes redirectAttributes, final Model model, final String type)
            throws ConnectorException {
        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        // Busco el wsdl original.
        final Path filePath = fileManagerService.getConnectorWSDL(connectorId, null);

        // Levanto y parseo el archivo wsdl.
        final File file = fileManagerService.getConnectorWSDLAndSchemasOnZipFile(filePath);
        final MultipartFile multipartFile = convertFileToMultipartFile(connectorId, filePath, file);

        uploadAndParseWSDL(multipartFile, model, connector, redirectAttributes, type);

        // Seteo el tipo de servidor en produccion
        connector.setType(ConnectorType.PRODUCCION.getEnvironment());

        // Borro los datos que no se deben de pasar a producción-
        connector.setId(null);
        connector.setUrl(null);
        connector.setLocalConfiguration(null);
        connector.setUserCredentials(null);

        return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
    }

    private MultipartFile convertFileToMultipartFile(final long connectorId, final Path filePath,
                                                     final File file) throws ConnectorException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            final FileItem fileItem = new DiskFileItem(file.getName(),
                    Files.probeContentType(file.toPath()), false, file.getName(),
                    (int) file.length(), file.getParentFile());

            inputStream = new FileInputStream(file);
            outputStream = fileItem.getOutputStream();
            IOUtils.copy(inputStream, outputStream);

            return new CommonsMultipartFile(fileItem);
        } catch (final IOException ex) {
            final String errorMessage = "ERROR: No se pudo encontrar el wsld para el conector con ID "
                    + connectorId;
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

    private void loadKeystoresModal(final Connector connector, final Model model)
            throws ConnectorException {
        final ArrayList<KeystoreModalData> keystoreModalDataColl = new ArrayList<KeystoreModalData>();
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_SSL_FILENAME, KEYSTORE_SSL_MODALNAME));
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_ORG_FILENAME, KEYSTORE_ORG_MODALNAME));
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_TRUSTSTORE_FILENAME, KEYSTORE_TRUSTSTORE_MODALNAME));

        model.addAttribute(KEYSTORESMODALDACOLL, keystoreModalDataColl);
    }
}
