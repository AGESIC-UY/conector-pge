package gub.agesic.connector.web.controller;

import gub.agesic.connector.dataaccess.entity.*;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.enums.SamlVersion;
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
import org.springframework.core.env.Environment;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.*;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.*;
import static gub.agesic.connector.web.controller.GlobalConfigurationController.GLOBAL_CERTIFICATE_STATUS;

@Controller
@SessionAttributes(value = {"type", "tag"})
public class ConnectorController {

    public static final String CONNECTOR = "connector";
    public static final String PREFIX_NAME_CONNECTOR = "prefixNameConnector";
    public static final String IS_GLOBAL_CONFIGURATION_ENABLED = "isGlobalConfigurationEnabled";

    public static final String VIEW_EDIT_1ST_STEP = "edit";
    public static final String VIEW_EDIT_2ND_STEP = "edit2";
    public static final String VIEW_ERROR_PAGE = "errorPage";
    public static final String VIEW_CONNECTOR = "viewConnector";
    public static final String REDIRECT_TO_CONNECTORS = "redirect:/connectors/";
    public static final String REDIRECT_TO_EDIT = "redirect:/connectors/add";
    public static final String DANGER = "danger";
    public static final String SUCCESS = "success";
    public static final String CSS = "css";
    public static final String MSG = "msg";

    public static final String PATH_UPLOAD_IMPORT = "/connectors/import";
    public static final String PATH_UPLOAD_ADD = "/connectors/add/uploadFile";

    public static final String CONECTOR_CREADO_EXITOSAMENTE = "Servicio creado exitosamente!";
    public static final String CONECTOR_IMPORTADO_EXITOSAMENTE = "Servicio importado exitosamente! En caso de importar una configuración local, no olvide ingresar las contraseñas";
    public static final String CONECTOR_ACTUALIZADO_EXITOSAMENTE = "Servicio actualizado exitosamente!";
    public static final String CONECTOR_ELIMINADO_EXITOSAMENTE = "Servicio eliminado exitosamente!";
    public static final String CREACION_NUEVO_CONECTOR_CANCELADA = "Se canceló la creación del nuevo Servicio!";
    public static final String ERROR_EXTENSION_DE_ARCHIVO_1 = "El archivo tiene extensión ";
    public static final String ERROR_EXTENSION_DE_ARCHIVO_2 = "debe subir un archivo con extensión: ";

    public static final String ERROR_NO_EXISTE_CONNECTOR = "ERROR: No existe un Connector con ID ";
    public static final String ERROR_ARCHIVO_INVALIDO = "ERROR: Debes subir un archivo con extensión: ";
    public static final String ERROR_CONECTOR_YA_EXISTENTE = "ERROR: Ese Servicio ya existe!";
    public static final String ERROR_NO_SE_ENCONTRO_CONECTOR_MODIFICADO = "ERROR: No se encontró el servicio modificado";
    public static final String ERROR_NO_SE_MOVIERON_ARCHIVOS_A_DIRECTORIO_DEL_CONECTOR = "ERROR: No se pudo mover los archivos al directorio del servicio con ID ";
    public static final String ERROR_NO_SE_PUDO_ELIMINAR_CONECTOR = "ERROR: No se pudo eliminar el Servicio ";
    public static final String ERROR_NO_SE_PUDO_ENCONTRAR_KEYSTORE = "ERROR: No se pudo encontrar ";
    public static final String ERROR_NO_EXISTE_WSDL_DEL_CONECTOR = "ERROR: No existe un WSDL para ese Servicio";
    public static final String ERROR_NO_SE_PUDO_PROCESAR_CORRECTAMENTE_WSDL = "ERROR: No se pudo procesar correctamente el WSDL y XMLSchemas asociados. Revise las rutas de los archivos XSD que contiene el WSDL";

    public static final String KEYSTORESMODALDACOLL = "keystoreModalDataColl";
    public static final String KEYSTORE_SSL_MODALNAME = "Keystore SSL";
    public static final String KEYSTORE_ORG_MODALNAME = "Keystore Org";
    public static final String KEYSTORE_TRUSTSTORE_MODALNAME = "Truststore";
    public static final String ERROR_AL_COPIAR_WSDL_AL_RESPONSE = "Error al acceder al archivo wsdl del Servicio con Id:";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorController.class);

    @Autowired
    private Environment environment;
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
        try {
            return getConnectorView(connectorId, redirectAttributes, VIEW_CONNECTOR);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            LOGGER.error(e.getMessage(), e);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
    }

    @GetMapping("/connectors/connector/{id}/wsdl")
    public void getConnectorWSDL(@PathVariable("id") final long connectorId,
                                 final HttpServletResponse response, final RedirectAttributes redirectAttributes,
                                 @RequestParam(value = "download", required = false, defaultValue = "false") final boolean isDownload) {

        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
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
                    throw new ConnectorException(ERROR_NO_EXISTE_WSDL_DEL_CONECTOR);
                }
            } catch (final ConnectorException e) {
                LOGGER.error(e.getMessage(), e);
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
        }
    }

    @GetMapping("/connectors/connector/{id}/keystoreOrg")
    public void getConnectorKeystoreOrg(@PathVariable("id") final long connectorId,
                                        final HttpServletResponse response, final RedirectAttributes redirectAttributes){
        try{
            getConnectorKeystore(connectorId, response, redirectAttributes, KEYSTORE_ORG_FILENAME);
        }catch(ConnectorException ce){
            LOGGER.error(ce.getMessage(), ce);
        }
    }

    @GetMapping("/connectors/connector/{id}/keystoreSsl")
    public void getConnectorKeystoreSsl(@PathVariable("id") final long connectorId,
                                        final HttpServletResponse response, final RedirectAttributes redirectAttributes){
        try{
            getConnectorKeystore(connectorId, response, redirectAttributes, KEYSTORE_SSL_FILENAME);
        }catch(ConnectorException ce){
            LOGGER.error(ce.getMessage(), ce);
        }
    }

    @GetMapping("/connectors/connector/{id}/truststore")
    public void getConnectorTruststore(@PathVariable("id") final long connectorId,
                                       final HttpServletResponse response, final RedirectAttributes redirectAttributes){
        try{
            getConnectorKeystore(connectorId, response, redirectAttributes, KEYSTORE_TRUSTSTORE_FILENAME);
        }catch(ConnectorException ce){
            LOGGER.error(ce.getMessage(), ce);
        }
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

        String status = connectorService.getGlobalCertificateStatus().name();
        model.addAttribute(GLOBAL_CERTIFICATE_STATUS, status);

        return new ModelAndView(VIEW_EDIT_1ST_STEP);
    }

    @GetMapping("/connectors/connector/{id}/edit")
    public ModelAndView editConnector(Model model, @PathVariable("id") final long connectorId,
                                      final RedirectAttributes redirectAttributes) {
        try {

            int maxUploadSize = MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE * 1024 * 1024;
            try {
                maxUploadSize = connectorService.getMaxUploadSize();
            } catch (ConnectorException e) {
                LOGGER.info(e.getMessage() + "Se utiliza el valor por defecto: " + MyWebInitializer.DEFAULT_MAX_UPLOAD_SIZE);
            }

            model.addAttribute("max_upload_size", maxUploadSize);
            return getConnectorView(connectorId, redirectAttributes, VIEW_EDIT_1ST_STEP);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            LOGGER.error(e.getMessage(), e);
            return new ModelAndView(VIEW_EDIT_1ST_STEP);
        }
    }

    @PostMapping("/connectors/connector/{id}/add/uploadFile")
    public ModelAndView uploadAndParseNewWSDL(
            @RequestParam("uploaded_file") final MultipartFile file, final Model model,
            @PathVariable("id") final long connectorId, final RedirectAttributes redirectAttributes, @SessionAttribute("type") String type) {
        try {
            final Connector connector = getConnectorByID(connectorId, redirectAttributes);

            // Si el servicio no existía, redirijo al listado de servicios
            if (connector == null) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
                LOGGER.error(ERROR_NO_EXISTE_CONNECTOR + connectorId);
                return new ModelAndView(REDIRECT_TO_CONNECTORS);
            } else {

                if (connector.isEnableLocalConfiguration()) {
                    // Cargo los datos de los keystores para el modal.
                    loadKeystoresModal(connector, model);
                }

                connectorService.setAllCertsExpirationStatus(connector);

                model.addAttribute(CONNECTOR, connector);

                String status = connectorService.getGlobalCertificateStatus().name();
                model.addAttribute(GLOBAL_CERTIFICATE_STATUS, status);
                model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));

                // Si no se eligió ningún archivo, envío mensaje de error
                if (file.isEmpty()) {
                    redirectAttributes.addFlashAttribute(CSS, DANGER);
                    redirectAttributes.addFlashAttribute(MSG,
                            ERROR_ARCHIVO_INVALIDO + XML + " o " + ZIP);
                    return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
                }
                // Si el servicio existe y se eligió un archivo, continúo al upload
                // de ese archivo
                else {
                    fileManagerService.deleteConnectorDirectoryFiles(Long.toString(connectorId), true);
                    return uploadAndParseWSDL(file, model, connector, redirectAttributes, type);
                }
            }
        } catch (ConnectorException e) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, e.getMessage());
            LOGGER.error(e.getMessage(), e);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
    }

    @PostMapping(PATH_UPLOAD_ADD)
    public ModelAndView uploadAndParseWSDL(@RequestParam("uploaded_file") final MultipartFile file,
                                           final Model model, final Connector connector,
                                           final RedirectAttributes redirectAttributes, @SessionAttribute("type") String type) {

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

            // Parseo el WSDL y cargo el servicio con los datos obtenidos.
            // Si el servicio no existía, lo construyo
            if (connector == null) {
                Connector emptyConnector = new Connector();
                emptyConnector.setType(type);
                newConnector = wsdlParserService.getWSDLData(model, prefixNameConnector, emptyConnector);
            } else {
                connector.setType(type);
                newConnector = wsdlParserService.getWSDLData(model, prefixNameConnector, connector);
            }
            model.addAttribute(CONNECTOR, newConnector);
            model.addAttribute(PREFIX_NAME_CONNECTOR, prefixNameConnector);

            String status = connectorService.getGlobalCertificateStatus().name();
            model.addAttribute(GLOBAL_CERTIFICATE_STATUS, status);
            model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));

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
                                             @RequestParam(PREFIX_NAME_CONNECTOR) final String prefixNameConnector,
                                             @RequestParam(value = "keystoreOrgFile", required = false) final MultipartFile keystoreOrgFile,
                                             @RequestParam(value = "keystoreSSLFile", required = false) final MultipartFile keystoreSSLFile,
                                             @RequestParam(value = "keystoreTruststoreFile", required = false) final MultipartFile keystoreTruststoreFile,
                                             final RedirectAttributes redirectAttributes,
                                             final Model model,
                                             @SessionAttribute("type") String type) {
        try {
            connectorService.checkConnectorPathAndTypeAvailabilityForType(connector.getName(), connector.getPath(), connector.getType());
            connectorService.updateConnectorPath(connector);

            // Patrón para validar ruta:
            // Que comience por '/'
            // Luego combinaciones de caracteres alfanuméricos, underscore (_) o guion (-)
            String pattern = "([/][\\w-]+)+";
            if (!connector.getPath().matches(pattern)) {
                final String msg = "Path incorrecto. Debe contener caracteres alfanuméricos, underscore (_) o guion (-) (Ejemplo: /1_texto/texto-2/otroTextoMas)";
                LOGGER.info(msg);
                model.addAttribute(PREFIX_NAME_CONNECTOR, prefixNameConnector);
                model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));
                model.addAttribute(CSS, DANGER);
                model.addAttribute(MSG, msg);
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
            }

            if (connector.isEnableLocalConfiguration()) {
                final ConnectorLocalConfiguration localConfig = connector.getLocalConfiguration();

                // Verificar la extension
                keystoreManagerService.checkFileExtension(keystoreOrgFile); // Org
                keystoreManagerService.checkFileExtension(keystoreSSLFile); // SSL
                keystoreManagerService.checkFileExtension(keystoreTruststoreFile); // Truststore

                // Verificar el password y copiar a temp/
                keystoreManagerService.uploadFileAndLoadKeystore(keystoreOrgFile,
                        prefixNameConnector + KEYSTORE_ORG_FILENAME,
                        localConfig.getPasswordKeystoreOrg());
                keystoreManagerService.uploadFileAndLoadKeystore(keystoreSSLFile,
                        prefixNameConnector + KEYSTORE_SSL_FILENAME,
                        localConfig.getPasswordKeystoreSsl());
                keystoreManagerService.uploadFileAndLoadKeystore(keystoreTruststoreFile,
                        prefixNameConnector + KEYSTORE_TRUSTSTORE_FILENAME,
                        localConfig.getPasswordKeystore());
            }

            // Se guarda el conector para obtener el ID
            connectorService.saveConnector(connector);

            // Verificar si ya existe una carpeta para este conector
            // se asume que ya existe el conector y se lanza error
            final String connectorId = connector.getId().toString();
            if (fileManagerService.isConnectorDirectory(connectorId)) {
                model.addAttribute(PREFIX_NAME_CONNECTOR, prefixNameConnector);
                model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));
                model.addAttribute(CSS, DANGER);
                model.addAttribute(MSG, ERROR_CONECTOR_YA_EXISTENTE);
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
            }

            // Crear carpeta connector/[connectorId]
            final Path connectorDirectoryPath = fileManagerService.createConnectorDirectory(connectorId);

            // Muevo los ficheros que coincidan con el prefijo prefixNameConnector[Nombre]
            // de la carpeta temp/ a la carpeta connector/[connectorId]
            fileManagerService.moveTempFilesToConnectorDirectory(prefixNameConnector, connectorDirectoryPath);

            if (connector.isEnableLocalConfiguration()) {
                // Actualizar el path de los ficheros para que apunten a la carpeta connector/[connectorId]
                keystoreManagerService.setKeystoresFilePaths(connector, keystoreOrgFile, keystoreSSLFile, keystoreTruststoreFile);
            } else {
                connector.setLocalConfiguration(null);
            }

            connectorService.saveConnector(connector);

            MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(connector.getId(), null);
            final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
            final String location = connectorService.getLocationBasedOnConnector(connector);

            //Actualizar el wsdl
            wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, connector.getPath());

            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG, CONECTOR_CREADO_EXITOSAMENTE);
            return new ModelAndView(REDIRECT_TO_CONNECTORS + "connector/" + connector.getId());

        } catch (final ConnectorException e) {
            LOGGER.error(e.getMessage(), e);
            model.addAttribute(PREFIX_NAME_CONNECTOR, prefixNameConnector);
            model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));
            model.addAttribute(CSS, DANGER);
            model.addAttribute(MSG, e.getMessage());
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
        }
    }

    // CHECK: Hay logica que se comparte con la logica al crear un servicio. Valorar unificarla.
    @PostMapping("/connectors/connector/{id}/update")
    public ModelAndView updateConnector(@ModelAttribute(CONNECTOR) final Connector updatedConnector,
                                        @RequestParam(PREFIX_NAME_CONNECTOR) final String prefixNameConnector,
                                        @PathVariable("id") final long connectorId,
                                        @RequestParam(value = "keystoreOrgFile", required = false) final MultipartFile keystoreOrgFile,
                                        @RequestParam(value = "keystoreSSLFile", required = false) final MultipartFile keystoreSSLFile,
                                        @RequestParam(value = "keystoreTruststoreFile", required = false) final MultipartFile keystoreTruststoreFile,
                                        final RedirectAttributes redirectAttributes,
                                        final Model model,
                                        @SessionAttribute("type") String type) {
        if (updatedConnector == null) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_SE_ENCONTRO_CONECTOR_MODIFICADO);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }

        String prefixName = prefixNameConnector;

        try {
            connectorService.checkConnectorPathAndTypeAvailabilityForType(updatedConnector.getName(), updatedConnector.getPath(), updatedConnector.getType(), connectorId);
            connectorService.updateConnectorPath(updatedConnector);

            final Connector connector = getConnectorByID(connectorId, redirectAttributes);
            prefixName = updateLocalConfigurations(updatedConnector, keystoreOrgFile, keystoreSSLFile, keystoreTruststoreFile, connector, prefixName);

            // Si se subió un nuevo archivo WSDL o ZIP, se mueve el mismo desde
            // el directorio temporal al directorio del Servicio
            // Se movera tambien cualquier nuevo keystore subido
            if (!prefixName.isEmpty()) {
                final Path connectorDirectoryPath = Paths.get(fileManagerService.getConnectorDirectory(String.valueOf(connectorId)));
                fileManagerService.moveTempFilesToConnectorDirectory(prefixName, connectorDirectoryPath);
            }

            if (updatedConnector.isEnableLocalConfiguration()) {
                // Actualizar el path de los ficheros para que apunten a la carpeta connector/[connectorId]
                keystoreManagerService.setKeystoresFilePaths(updatedConnector, keystoreOrgFile, keystoreSSLFile, keystoreTruststoreFile);
            } else {
                updatedConnector.setLocalConfiguration(null);
            }

            // Patrón para validar ruta:
            // Que comience por '/'
            // Luego combinaciones de caracteres alfanuméricos, underscore (_) o guion (-)
            String pattern = "([/][\\w-]+)+";
            if (!updatedConnector.getPath().matches(pattern)) {
                final String errorMessage = "Path incorrecto. Debe contener caracteres alfanuméricos, underscore (_) o guion (-) (Ejemplo: /1_texto/texto-2/otroTextoMas)";
                LOGGER.info(errorMessage);
                model.addAttribute(PREFIX_NAME_CONNECTOR, prefixNameConnector);
                model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));
                model.addAttribute(CSS, DANGER);
                model.addAttribute(MSG, errorMessage);
                return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, updatedConnector);
            }

            updateUserCredentials(updatedConnector, connector);

            connectorService.saveConnector(updatedConnector);

            final MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(connector.getId(), null);
            final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
            final String location = connectorService.getLocationBasedOnConnector(updatedConnector);
            wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, updatedConnector.getPath());

            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG, CONECTOR_ACTUALIZADO_EXITOSAMENTE);
            return new ModelAndView(REDIRECT_TO_CONNECTORS + "connector/" + updatedConnector.getId());

        } catch (final ConnectorException e) {
            // CHECK: Remove files from /temp
            final String errorMessage = e.getMessage();
            LOGGER.error(errorMessage, e);
            model.addAttribute(PREFIX_NAME_CONNECTOR, prefixName);
            model.addAttribute(IS_GLOBAL_CONFIGURATION_ENABLED, isGlobalConfigEnabled(type));
            model.addAttribute(CSS, DANGER);
            model.addAttribute(MSG, errorMessage);
            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, updatedConnector);
        }
    }

    @PostMapping("/connectors/connector/cancel")
    public ModelAndView cancelCreateConnector(
            @RequestParam(value = PREFIX_NAME_CONNECTOR, required = false) final String prefixNameConnector,
            final RedirectAttributes redirectAttributes) {
        try {
            fileManagerService.deletePrefixFilesInTemp(prefixNameConnector);
            if (!"".equals(prefixNameConnector)) {
                redirectAttributes.addFlashAttribute(CSS, DANGER);
                redirectAttributes.addFlashAttribute(MSG, CREACION_NUEVO_CONECTOR_CANCELADA);
            }

            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } catch (final ConnectorException e) {
            LOGGER.error(e.getMessage(), e);
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG,
                    ERROR_NO_SE_MOVIERON_ARCHIVOS_A_DIRECTORIO_DEL_CONECTOR);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }
    }

    @GetMapping("/connectors/connector/{id}/delete")
    public ModelAndView deleteConnector(@PathVariable("id") final long connectorId,
                                        final RedirectAttributes redirectAttributes) {

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
                LOGGER.error(e.getMessage(), e);
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
                                final HttpServletResponse response, final RedirectAttributes redirectAttributes){

        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        if (connector != null) {
            try{
                final Path connectorFilePath = connectorParserService.exportConnectorData(connector);
                if (connectorFilePath.toFile().exists()) {
                    response.setContentType("application/force-download");
                    response.addHeader("Content-Disposition",
                            "attachment; filename=" + connectorFilePath.getFileName());
                    saveFileToResponse(connectorId, response, connectorFilePath);
                }
            }catch(ConnectorException ce){
                LOGGER.error(ce.getMessage(), ce);
            }
        }
    }

    @PostMapping(PATH_UPLOAD_IMPORT)
    public ModelAndView importConnector(@RequestParam("uploaded_file") final MultipartFile file,
                                        final Model model, final RedirectAttributes redirectAttributes) {

        // Si no se eligió ningún archivo, envío mensaje de error
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_ARCHIVO_INVALIDO + XML);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        }

        Connector importedConnector = new Connector();

        // Se define la version de SAML, para que no se lance unn error al intentar guardar el conector,
        // porque este campo esta marcado como NOT NULL en la BD.
        // FIXME: Definir un valor por defecto para la columna Saml Version
        importedConnector.setSamlVersion(SamlVersion.V1_1.getName());

        try {
            // Se guarda el conector porque se esta utilizando luego el id ('populateConnector -> createConnectorDirectory'),
            // para crear un fichero en el sistema de archivos del sistema.
            // En caso de error (catch (final Exception exception) ) se borra el conector creado.
            // FIXME: No guardar si el proceso no termina por completo.
            connectorService.saveConnector(importedConnector);

            final String fileExtension = fileManagerService.getFileExtension(file.getOriginalFilename());

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

            importedConnector = connectorParserService.importConnectorData(model, prefixNameConnector, importedConnector);
            keystoreManagerService.setKeystoresFilePaths(importedConnector, null, null, null);
            connectorService.saveConnector(importedConnector);

            final MultipartFile toSaveFile = fileManagerService.getConnectorWSDLNewFile(importedConnector.getId(), null);
            final Path path = fileManagerService.getConnectorWSDL(importedConnector.getId(), null);
            final String location = connectorService.getLocationBasedOnConnector(importedConnector);
            wsdlParserService.modifyLocationAndSave(toSaveFile, location, path, importedConnector.getPath());

            redirectAttributes.addFlashAttribute(CSS, SUCCESS);
            redirectAttributes.addFlashAttribute(MSG, CONECTOR_IMPORTADO_EXITOSAMENTE);
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } catch (final Exception exception) {
            if (importedConnector.getId() != null) {
                deleteConnector(importedConnector.getId(), redirectAttributes);
            }
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
                LOGGER.error(errorMessage, e);
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

    public String updateLocalConfigurations(Connector updatedConnector, MultipartFile keystoreOrgFile,
                                            MultipartFile keystoreSSLFile, MultipartFile keystoreTruststoreFile,
                                            Connector connector, String prefixNameConnector) throws ConnectorException {

        String prefixName = prefixNameConnector;

        Path filePathOrg = null, filePathSSL = null, filePathTS = null;
        Boolean hasPasswordChangeOrg = false, hasPasswordChangeSSL = false, hasPasswordChangeTS = false;

        if (updatedConnector.isEnableLocalConfiguration()) {

            // Si no hay una configuracion local anterior, no van a existir keystores
            // por tanto debe subir los keystores
            if (connector.getLocalConfiguration() == null) {
                if (keystoreOrgFile.getOriginalFilename().isEmpty()) {
                    throw new ConnectorException("Debe subir el Keystore Organismo");
                } else if (keystoreSSLFile.getOriginalFilename().isEmpty()) {
                    throw new ConnectorException("Debe subir el Keystore SSL");
                } else if (keystoreTruststoreFile.getOriginalFilename().isEmpty()) {
                    throw new ConnectorException("Debe subir el Truststore SSL");
                }

                // En este caso el conector debe haber estado utilizando la config global
                // por tanto se utilizaran los valores por defecto para filePath (null) y hasPasswordChange (false)
            }
            // Sino, se copia la misma configuracion local
            else {
                ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
                ConnectorLocalConfiguration updatedLocalConfiguration = updatedConnector.getLocalConfiguration();

                updatedLocalConfiguration.setId(localConfiguration.getId());
                updatedLocalConfiguration.setDirKeystore(localConfiguration.getDirKeystore());
                updatedLocalConfiguration.setDirKeystoreOrg(localConfiguration.getDirKeystoreOrg());
                updatedLocalConfiguration.setDirKeystoreSsl(localConfiguration.getDirKeystoreSsl());

                updatedConnector.setLocalConfiguration(updatedLocalConfiguration);

                // file path
                filePathOrg = Paths.get(localConfiguration.getDirKeystoreOrg());
                filePathSSL = Paths.get(localConfiguration.getDirKeystoreSsl());
                filePathTS = Paths.get(localConfiguration.getDirKeystore());

                // has password change
                hasPasswordChangeOrg = hasPasswordChange(localConfiguration, updatedConnector.getLocalConfiguration(), KEYSTORE_ORG_FILENAME);
                hasPasswordChangeSSL = hasPasswordChange(localConfiguration, updatedConnector.getLocalConfiguration(), KEYSTORE_SSL_FILENAME);
                hasPasswordChangeTS = hasPasswordChange(localConfiguration, updatedConnector.getLocalConfiguration(), KEYSTORE_TRUSTSTORE_FILENAME);;
            }

            // Org
            String newPassword = updatedConnector.getLocalConfiguration().getPasswordKeystoreOrg();
            prefixName = checkUpdatedFile(keystoreOrgFile, filePathOrg, KEYSTORE_ORG_FILENAME, newPassword, prefixNameConnector, hasPasswordChangeOrg);

            // SSL
            newPassword = updatedConnector.getLocalConfiguration().getPasswordKeystoreSsl();
            prefixName = checkUpdatedFile(keystoreSSLFile, filePathSSL, KEYSTORE_SSL_FILENAME, newPassword, prefixName, hasPasswordChangeSSL);

            // Truststore
            newPassword = updatedConnector.getLocalConfiguration().getPasswordKeystore();
            prefixName = checkUpdatedFile(keystoreTruststoreFile, filePathTS, KEYSTORE_TRUSTSTORE_FILENAME, newPassword, prefixName, hasPasswordChangeTS);
        }

        return prefixName;
    }

    // CHECK: Move this logic to Service
    // Verifica si se subio un nuevo fichero, la extension y la contrasena
    // O si solo se actualizo la contrasena
    String checkUpdatedFile(MultipartFile file, Path filePath, String fileName, String password, String prefixName, Boolean hasPasswordChange) throws ConnectorException {
        // Si, se cargo algun keystore nuevo, se debe verificar la extension y la contrasena
        // contra el nuevo keystore
        if (!file.getOriginalFilename().isEmpty()) {
            // Verifico extension
            keystoreManagerService.checkFileExtension(file);
            // Si prefixName es vacio, creo uno nuevo
            if (prefixName.isEmpty()) {
                prefixName = fileManagerService.getCurrentTime();
            }
            // Verificar el password y copiar a temp/
            keystoreManagerService.uploadFileAndLoadKeystore(file, prefixName + fileName, password);
        }
        // Sino, Si se cambio la contrasena se debe verificar la nueva contrasena
        // contra el keystore ya existente
        else if (hasPasswordChange) {
            // Cargar fichero al KeyStore. Se chequea la contrasena
            keystoreManagerService.loadKeystore(filePath, password);
        }

        // Se debe utilizar el mismo prefijo para todos los ficheros asociados al mismo connector
        return prefixName;
    }

    // Verifica si se cambio alguna contrasena en un servicio que se esta actualizando
    Boolean hasPasswordChange(ConnectorLocalConfiguration current, ConnectorLocalConfiguration updated, String keystoreName) {
        if (current == null || updated == null) {
            return false;
        }
        switch (keystoreName) {
            case KEYSTORE_ORG_FILENAME:
                return !current.getPasswordKeystoreOrg().equals(updated.getPasswordKeystoreOrg());
            case KEYSTORE_SSL_FILENAME:
                return !current.getPasswordKeystoreSsl().equals(updated.getPasswordKeystoreSsl());
            case KEYSTORE_TRUSTSTORE_FILENAME:
                return !current.getPasswordKeystore().equals(updated.getPasswordKeystore());
            default:
                return false;
        }
    }

    public Connector getConnectorByID(final long connectorId,
                                      final RedirectAttributes redirectAttributes) {
        final Connector connector = connectorService.getConnector(connectorId);
        if (connector == null) {
            redirectAttributes.addFlashAttribute(CSS, DANGER);
            redirectAttributes.addFlashAttribute(MSG, ERROR_NO_EXISTE_CONNECTOR + connectorId);
            LOGGER.error(ERROR_NO_EXISTE_CONNECTOR + connectorId);
        } else if (connector.getSamlVersion() == null || connector.getSamlVersion().isEmpty()) {
            connector.setSamlVersion(SamlVersion.V1_1.getName());
        }

        return connector;
    }

    public ModelAndView getConnectorView(final @PathVariable("id") long connectorId,
                                         final RedirectAttributes redirectAttributes, final String targetViewName) {
        final Connector connector = getConnectorByID(connectorId, redirectAttributes);
        if (connector == null) {
            return new ModelAndView(REDIRECT_TO_CONNECTORS);
        } else {

            connectorService.setAllCertsExpirationStatus(connector);

            if (connector.isEnableLocalConfiguration()) {
                ConnectorLocalConfiguration localConfig = connector.getLocalConfiguration();
                String EXP_DATE_FORMAT = "dd-MM-yyyy";
                String aliasKeystore = localConfig.getAliasKeystore();
                String aliasKeystoreSSL = localConfig.getAliasKeystoreSSL();
                String aliasTruststore;
                if (connector.getType().equals(EnvironmentType.TESTING.getName())) {
                    aliasTruststore = environment.getProperty("connector.truststore.alias.test");
                } else {
                    aliasTruststore = environment.getProperty("connector.truststore.alias.prod");
                }
                Path keystorePath;
                Date expDate;
                String expDateString;

                try {
                    keystorePath = keystoreManagerService.getConnectorKeystore(connectorId, KEYSTORE_ORG_FILENAME);
                    expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasKeystore, localConfig.getPasswordKeystoreOrg());
                    expDateString = new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate);
                } catch (ConnectorException e) {
                    expDateString = e.getMessage();
                    LOGGER.error(e.getMessage(), e);
                }
                connector.setExpDateKeystoreOrg(expDateString);

                try {
                    keystorePath = keystoreManagerService.getConnectorKeystore(connectorId, KEYSTORE_SSL_FILENAME);
                    expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasKeystoreSSL, localConfig.getPasswordKeystoreSsl());
                    expDateString = new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate);
                } catch (ConnectorException e) {
                    expDateString = e.getMessage();
                    LOGGER.error(e.getMessage(), e);
                }
                connector.setExpDateKeystoreSSL(expDateString);

                try {
                    keystorePath = keystoreManagerService.getConnectorKeystore(connectorId, KEYSTORE_TRUSTSTORE_FILENAME);
                    expDate = keystoreManagerService.getCertificateExpirationDate(keystorePath, aliasTruststore, localConfig.getPasswordKeystore());
                    expDateString = new SimpleDateFormat(EXP_DATE_FORMAT).format(expDate);
                    connector.setExpDateKeystoreTruststore(expDateString + " (" + aliasTruststore + ")");
                } catch (ConnectorException e) {
                    connector.setExpDateKeystoreTruststore(e.getMessage());
                    LOGGER.info(e.getMessage(), e);
                }
            }

            ModelAndView modelAndView = new ModelAndView(targetViewName, CONNECTOR, connector);

            String status = connectorService.getGlobalCertificateStatus().name();
            modelAndView.addObject(GLOBAL_CERTIFICATE_STATUS, status);

            return modelAndView;
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
                                   final RedirectAttributes redirectAttributes, final Model model, final String type){
        try{
            final Connector connector = getConnectorByID(connectorId, redirectAttributes);
            // Busco el wsdl original.
            final Path filePath = fileManagerService.getConnectorWSDL(connectorId, null);

            // Levanto y parseo el archivo wsdl.
            final File file = fileManagerService.getConnectorWSDLAndSchemasOnZipFile(filePath);
            final MultipartFile multipartFile = convertFileToMultipartFile(connectorId, file);

            uploadAndParseWSDL(multipartFile, model, connector, redirectAttributes, type);

            // Seteo el tipo de servidor en produccion
            connector.setType(EnvironmentType.PRODUCTION.getName());

            // Borro los datos que no se deben de pasar a producción-
            connector.setId(null);
            connector.setUrl(null);
            connector.setLocalConfiguration(null);
            connector.setUserCredentials(null);

            return new ModelAndView(VIEW_EDIT_2ND_STEP, CONNECTOR, connector);
        }catch(ConnectorException ce){
            LOGGER.error(ce.getMessage(), ce);
            return new ModelAndView(VIEW_ERROR_PAGE);
        }
    }

    private MultipartFile convertFileToMultipartFile(final long connectorId, final File file) throws ConnectorException {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            final FileItem fileItem = new DiskFileItem(file.getName(),
                    Files.probeContentType(file.toPath()), false, file.getName(),
                    (int) file.length(), file.getParentFile());

            inputStream = new FileInputStream(file);
            outputStream = fileItem.getOutputStream();
            IOUtils.copy(inputStream, outputStream);

            CommonsMultipartFile commonsMultipartFile = new CommonsMultipartFile(fileItem);
            inputStream.close();
            outputStream.close();
            return commonsMultipartFile;
        } catch (final IOException ex) {
            final String errorMessage = "ERROR: No se pudo encontrar el wsld para el servicio con ID " + connectorId;
            throw new ConnectorException(errorMessage, ex);
        }
    }

    private void loadKeystoresModal(final Connector connector, final Model model)
            throws ConnectorException {
        final ArrayList<KeystoreModalData> keystoreModalDataColl = new ArrayList<>();
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_SSL_FILENAME, KEYSTORE_SSL_MODALNAME));
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_ORG_FILENAME, KEYSTORE_ORG_MODALNAME));
        keystoreModalDataColl.add(keystoreManagerService.getConnectorKeystoreData(connector,
                KEYSTORE_TRUSTSTORE_FILENAME, KEYSTORE_TRUSTSTORE_MODALNAME));

        model.addAttribute(KEYSTORESMODALDACOLL, keystoreModalDataColl);
    }

    private boolean isGlobalConfigEnabled(String type) {
        try {
            return connectorService.getGlobalConfigurationByType(type) != null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
