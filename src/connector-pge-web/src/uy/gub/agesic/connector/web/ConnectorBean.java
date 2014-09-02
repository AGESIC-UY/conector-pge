package uy.gub.agesic.connector.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.xml.sax.InputSource;

import uy.gub.agesic.connector.PasswordManager;
import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorKeystoreType;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.entity.RoleOperation;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.session.api.GlobalConfigurationManager;
import uy.gub.agesic.connector.util.ConnectorFileManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.FilesPathBean;
import uy.gub.agesic.connector.util.Props;
import uy.gub.agesic.connector.util.WsdlManager;

public class ConnectorBean {
	private static Log log = LogFactory.getLog(ConnectorBean.class);
	private XPathFactory xpf = XPathFactory.newInstance();

	private Connector connector = new Connector();

	ConnectorManager connectorManager;
	String type = Connector.TYPE_PROD;
	String tag = null;

	// PRUEBA
	String pathConnector;

	private UploadedFile wsdlUploadFile;
	private UploadedFile keystoreOrgUploadFile;
	private UploadedFile keystoreUploadFile;
	private UploadedFile keystoreSslUploadFile;
	
	private File wsdlFile;
	private File keystoreOrgFile;
	private File keystoreFile;
	private File keystoreSslFile;

	private RoleOperation rlToDelete;
	private RoleOperation rlToEdit = new RoleOperation();

	private String editingRoleOper = "false";

	private UploadedFile importConnectorFile;
	
	private static final String DEFAULT_PASS = "DEFAULT_PASS";
	private static final int  MAX_LENGTH_WSA_ACTION = 256;
	
	private String keystoreOrganismoPass = DEFAULT_PASS;
	private String keystoreSSLPass = DEFAULT_PASS;
	private String trustorePass = DEFAULT_PASS;
	private String userNameTokenPass = DEFAULT_PASS;
	
	public static String version = "2.0";
	
	private boolean editingToProduction = false;
	
	private FilesPathBean filesPathBean;
	
	private int scrollerPage = 1;
	
	private GlobalConfigurationManager globalConfigService;
	
	private boolean fetchWsdlOperations = false;
	
	private String nameKeystoreOrgUploadFile;
	private String nameKeystoreUploadFile;
	private String nameKeystoreSslUploadFile;
	private String nameWsdlUploadFile;

	@PostConstruct
	private void init() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			connectorManager = (ConnectorManager) ctx
					.lookup(Constants.CONNECTOR_MANAGER_REMOTE); // "connector-pge-ear/ConnectorManagerSession/remote");
			globalConfigService = (GlobalConfigurationManager) ctx.lookup(Constants.GLOBAL_CONFIGURATION_MANAGER_REMOTE);
		} catch (NamingException e) {
			log.error("Initialization failed ", e);
			throw new RuntimeException(e);
		}
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public String edit() {
		try {
			assureConnectorManager();
			connector = connectorManager.getConnector(connector.getId());
			filesPathBean = ConnectorFileManager.loadFilesInfo(connector, connectorManager);
			
			resetConnectorBean();
			
			return "edit";
		} catch (ConnectorException e) {
			log.error("edit failed", e);
			throw new RuntimeException(e);
		}
	}
	
	public String viewAssociated() {
		try {
			assureConnectorManager();
			connector = connectorManager.getConnector(connector.getConnectorAssociated().getId());
			ConnectorFileManager.loadFilesInfo(connector, connectorManager);
			return "edit";
		} catch (ConnectorException e) {
			log.error("edit failed", e);
			throw new RuntimeException(e);
		}
	}
	
	public String toProduction() {
		
		assureConnectorManager();
		String path = ConnectorFileManager.getFilePath(connector, connectorManager);
		Connector connectorProd = new Connector(connector);
		connectorProd.setType(Connector.TYPE_PROD);
		connectorProd.setId(null);
		
		// Genero relaciones
		
		connectorProd.setConnectorAssociated(connector);
		
		connector=connectorProd;
		
		// Cargar archivos
		
		wsdlFile = new File(path + Connector.NAME_FILE_WSDL);
		keystoreOrgFile= new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		keystoreFile = new File(path + Connector.NAME_FILE_KEYSTORE2);
		keystoreSslFile = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		
		editingToProduction = true;
		return "edit";
	
	}
	
	

	public String view() {
		try {
			assureConnectorManager();
			connector = connectorManager.getConnector(connector.getId());
			ConnectorFileManager.loadFilesInfo(connector, connectorManager);
			return "view";
		} catch (ConnectorException e) {
			log.error("view failed", e);
			throw new RuntimeException(e);
		}
	}

	public String delete() {
		try {
			assureConnectorManager();
			connectorManager.deleteConnector(connector.getId());
			return "home";
		} catch (ConnectorException e) {
			log.error("delete failed", e);
			throw new RuntimeException(e);
		}
	}
	
	public String save() {
		try {
			
			if (validateFields(connector) == null) {
				return null;
			}
			
			assureConnectorManager();
			
			if (keystoreOrganismoPass == null || !keystoreOrganismoPass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystoreOrg(PasswordManager.encrypt(keystoreOrganismoPass));
			}
			if (keystoreSSLPass == null || !keystoreSSLPass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystore(PasswordManager.encrypt(keystoreSSLPass));
			}
			if (trustorePass == null || !trustorePass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystoreSsl(PasswordManager.encrypt(trustorePass));
			}
				
			if (userNameTokenPass == null || !userNameTokenPass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setUserNameTokenPassword(PasswordManager.encrypt(userNameTokenPass));
			}
			
			
			if (connector.getId() == null) {
				Long connectorId = connectorManager.createConnector(connector);
				connector.setId(connectorId);
			} else {
				connectorManager.editConnector(connector);
			}

			try {
				InputStream inputWsdl = null;
				InputStream inputkeystoreOrg = null;
				InputStream inputkeystore = null;
				InputStream inputkeystoreSsl = null;
				String nameWsdl = null;
				
				if (wsdlUploadFile != null) {
					inputWsdl = wsdlUploadFile.getInputStream();
					nameWsdl = wsdlUploadFile.getName();
				}

				if (keystoreOrgUploadFile != null) {
					inputkeystoreOrg = keystoreOrgUploadFile.getInputStream();
				} else { // en el caso que al editar a produccion se cambie este campo antes de guardar el conector de produccion
					if (keystoreOrgFile != null && connector.isEnableLocalConf() && editingToProduction) {
						inputkeystoreOrg = new FileInputStream(keystoreOrgFile);
					}
				}

				if (keystoreUploadFile != null) {
					inputkeystore = keystoreUploadFile.getInputStream();
				} else { // en el caso que al editar a produccion se cambie este campo antes de guardar el conector de produccion 
					if (keystoreFile != null && connector.isEnableLocalConf() && editingToProduction) {
						inputkeystore = new FileInputStream(keystoreFile);
					}
				}

				if (keystoreSslUploadFile != null) {
					inputkeystoreSsl = keystoreSslUploadFile.getInputStream();
				} else { // en el caso que al editar a produccion se cambie este campo antes de guardar el conector de produccion
					if (keystoreSslFile != null && connector.isEnableLocalConf() && editingToProduction) {
						inputkeystoreSsl = new FileInputStream(keystoreSslFile);
					}
				}
				
				if (editingToProduction) {
					if (connector.isEnableLocalConf()) { 
						ConnectorFileManager.createfiles(connector, connectorManager, inputWsdl, 
								inputkeystoreOrg, inputkeystore, inputkeystoreSsl, nameWsdl, editingToProduction, wsdlFile);
					} else {
						ConnectorFileManager.createfiles(connector, connectorManager, inputWsdl, 
							null, null, null, nameWsdl, editingToProduction, wsdlFile);
					}
				} else {
					ConnectorFileManager.createfiles(connector, connectorManager, inputWsdl, 
						inputkeystoreOrg, inputkeystore, inputkeystoreSsl, nameWsdl, editingToProduction, null);
				}
				
				/// Validaciones
				
				if (fetchWsdlOperations) {
					getWsdlOperations(ConnectorFileManager.getFilePath(connector, connectorManager));
					connectorManager.editConnector(connector);
				}
				
				fetchWsdlOperations = false;
				
				FilesPathBean fpb = ConnectorFileManager.loadFilesInfo(connector, connectorManager);
				
				if (connector.isEnableLocalConf()) {
					validateKeystore(fpb.getKeystoreOrgFilePath(), null, PasswordManager.decrypt(connector.getPasswordKeystoreOrg()), connector.getAliasKeystore(), false, "Keystore Organismo: ", false, false);
					validateKeystore(fpb.getKeystoreSSLFilePath(), null, PasswordManager.decrypt(connector.getPasswordKeystore()), connector.getAliasKeystore(), false, "Keystore SSL: ", true, false);
					validateKeystore(fpb.getTruststoreSSLFilePath(), null, PasswordManager.decrypt(connector.getPasswordKeystoreSsl()), connector.getAliasKeystore(), false, "Truststore: ", false, true);
				}
				
				//Si es un conector con configuracion global, verificar si ya se esta cargada, de lo contrario informar al usuario
				if (!connector.isEnableLocalConf()){
					GlobalConfiguration globalConfiguration = globalConfigService.getGlobalConfiguration(connector.getType());
					if (globalConfiguration == null) {
						FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Configuraci\u00F3n global no se encuentra cargada", null);
						FacesContext.getCurrentInstance().addMessage(null, fm);
					}
				}
				
				connector.setVersion(version);
				
				editingToProduction = false;
				
				
			} catch (Exception e) {
				log.error("save failed", e);

				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return null;
			}

			return "correct";

		} catch (ConnectorException e) {
			log.error("Exception on save", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);

			return null;
		}
	}
	
	public void deleteKeystoreOrganismo() {
		deleteKeystore(ConnectorKeystoreType.ORGANISMO);
	}

	public void deleteKeystoreSSL() {
		deleteKeystore(ConnectorKeystoreType.KEYSTORE_SSL);	
	}

	public void deleteTrustore() {
		deleteKeystore(ConnectorKeystoreType.TRUSTORE);		
	}

	private void deleteKeystore(ConnectorKeystoreType type) {
		ConnectorFileManager.deleteFile(connectorManager, connector, type);
		if (ConnectorKeystoreType.ORGANISMO.equals(type)) {
			connector.setExistKeystoreOrg(false);
		}
		if (ConnectorKeystoreType.KEYSTORE_SSL.equals(type)) {
			connector.setExistKeystore(false);
		}
		if (ConnectorKeystoreType.TRUSTORE.equals(type)) {
			connector.setExistKeystoreSsl(false);
		}
	}

	public String testUpload() {

		System.out.println("=================================");
		if (wsdlUploadFile != null)
			System.out.println(wsdlUploadFile.getName());
		System.out.println("=================================");

		return "edit";
	}

	public String vaciarBean() {
		connector = new Connector();

		connector.setType(type);
		connector.setEnableCacheTokens(false);
		editingToProduction=false;
		
		resetConnectorBean();

		return "edit";
	}

	public String printConnectors() {
		assureConnectorManager();
		try {
			System.out.println(connectorManager.getConnectors(type, tag));
			return "edit";
		} catch (ConnectorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Connector> getConnectors() {
		assureConnectorManager();
		try {
			return connectorManager.getConnectors(type, tag);
		} catch (ConnectorException e) {
			log.error("getConnectors failed", e);
			throw new RuntimeException(e);
		}
	}
	
	

	public String gohome() {
		return "home";
	}

	public boolean isEditing() {
		return connector.getId() != null;
	}

	public String getLoggedInUsername() {
		return FacesContext.getCurrentInstance().getExternalContext()
				.getRemoteUser();
	}

	public boolean isSignedIn() {
		return (getLoggedInUsername() != null);
	}

	public String getPathConnector() {
		return pathConnector;
	}

	public void setPathConnector(String pathConnector) {
		this.pathConnector = pathConnector;
	}

	public void pathConnectorTest() {
		assureConnectorManager();
		try {
			FullConnector fc = connectorManager.getConnectorByPath(
					pathConnector, (Connector.TYPE_PROD.equals(getType())?true : false));
			log.debug("conection by path= " + fc.getConnector());
			log.debug("conectionPaths= " + fc.getConnectorPaths());

		} catch (ConnectorException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public UploadedFile getWsdlUploadFile() {
		return wsdlUploadFile;
	}

	public void setWsdlUploadFile(UploadedFile wsdlUploadFile) {
		if (wsdlUploadFile == null) {
			return;
		}
		this.wsdlUploadFile = wsdlUploadFile;
		fetchWsdlOperations= true;
		nameWsdlUploadFile = this.wsdlUploadFile.getName();
	}
	
	public void getWsdlOperations(String pathWsdlDir) {
		
		XPath xpath = xpf.newXPath();
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		namespaces.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		namespaces.put("wsaw", "http://www.w3.org/2006/05/addressing/wsdl");
		
		uy.gub.agesic.connector.util.XPathNamespaceContext namespaceContext = new uy.gub.agesic.connector.util.XPathNamespaceContext();
		for (Entry<String, String> entry : namespaces.entrySet()) {
			namespaceContext.setMapping(entry.getKey(), entry.getValue());
		}

		xpath.setNamespaceContext(namespaceContext);
		
		File mainWsdl = new File(pathWsdlDir + Connector.NAME_FILE_WSDL);
		
		try {
			String queryXPath = xpath.evaluate("/wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location" , new InputSource(new FileInputStream(mainWsdl))); // getInputSource(wsdlUploadFile. getBytes()));
			connector.setUrl(queryXPath);
		} catch (Exception e) {
			log.error("No se pudo obtener la URL del wsdl", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "No se pudo obtener la URL del wsdl", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		
		try {
			List<RoleOperation> role_operation = new LinkedList<RoleOperation>();
			String operationsCount = xpath.evaluate("count(/wsdl:definitions/wsdl:binding/wsdl:operation)" , new InputSource(new FileInputStream(mainWsdl))); // getInputSource(wsdlUploadFile. getBytes()));
			int operations = Integer.parseInt(operationsCount);
			
			ArrayList<String> wsdlFileNames = null;
			
			for (;operations > 0; operations --) {
				
				// operationName
				String query = "/wsdl:definitions/wsdl:binding/wsdl:operation[" + operations +"]/@name";
				String operationName = xpath.evaluate(query , new InputSource(new FileInputStream(mainWsdl))); // getInputSource(wsdlUploadFile. getBytes()));
				
				// soapAction - wsaAction (ambos salen del soapAction del binding del wsdl)
				String actionName = null;
				query = "/wsdl:definitions/wsdl:binding/wsdl:operation[@name = '" + operationName +"']/soap:operation/@soapAction";
				actionName = xpath.evaluate(query , new InputSource(new FileInputStream(mainWsdl))); // getInputSource(wsdlUploadFile. getBytes()));
				
				// elementName
				wsdlFileNames = WsdlManager.searchWsdlLocation(pathWsdlDir);
				String elementName = WsdlManager.getElementNameForOperation(pathWsdlDir, wsdlFileNames, operationName, xpath);
				
				RoleOperation roleOperation = new RoleOperation();
				roleOperation.setOperation(elementName);
				roleOperation.setOperationFromWSDL(operationName);
				role_operation.add(roleOperation);
				
				if (actionName != null) {
					// el wsaAction se utiliza en el header de addressing del mensaje soap
					// el soapAction se utiliza como header http en el post
					roleOperation.setWsaAction(actionName);
					roleOperation.setSoapAction(actionName);
				}
			}
			
			connector.setRole_operation(role_operation);
			
		} catch (Exception e) {
			log.error("No se pudo procesar las operaciones del wsdl", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "No se pudo procesar las operaciones del wsdl", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
	}
	
	public static InputSource getInputSource(byte[] bytes) throws XPathExpressionException {
		Object payload;

		payload = bytes;

		if (payload instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) payload));
		} else if (payload instanceof String) {
			return new InputSource(new StringReader((String) payload));
		} else {
			String name = null;
			try {
				name = payload.getClass().getName();
			} catch (Exception e) {
				log.error("Class " + payload, e);
			}
			
			throw new XPathExpressionException("Unsupport expression input object type: " + name + " ,payload " + payload);
		}
	}

	public UploadedFile getKeystoreOrgUploadFile() {
		return keystoreOrgUploadFile;
	}

	public void setKeystoreOrgUploadFile(UploadedFile keystoreOrgUploadFile) {
		if (keystoreOrgUploadFile == null) {
			return;
		}
		this.keystoreOrgUploadFile = keystoreOrgUploadFile;
		nameKeystoreOrgUploadFile = this.keystoreOrgUploadFile.getName();
	}

	public UploadedFile getKeystoreUploadFile() {
		return keystoreUploadFile;
	}

	public void setKeystoreUploadFile(UploadedFile keystoreUploadFile) {
		if (keystoreUploadFile == null) {
			return;
		}
		this.keystoreUploadFile = keystoreUploadFile;
		nameKeystoreUploadFile = this.keystoreUploadFile.getName();
	}

	public UploadedFile getKeystoreSslUploadFile() {
		return keystoreSslUploadFile;
	}

	public void setKeystoreSslUploadFile(UploadedFile keystoreSslUploadFile) {
		if (keystoreSslUploadFile == null) {
			return;
		}
		this.keystoreSslUploadFile = keystoreSslUploadFile;
		nameKeystoreSslUploadFile = this.keystoreSslUploadFile.getName();
	}

	public RoleOperation getRlToDelete() {
		return rlToDelete;
	}

	public void setRlToDelete(RoleOperation rlToDelete) {
		this.rlToDelete = rlToDelete;
	}

	public RoleOperation getRlToEdit() {
		return rlToEdit;
	}

	public void setRlToEdit(RoleOperation rlToEdit) {
		this.rlToEdit = rlToEdit;
	}

	public String getEditingRoleOper() {
		return editingRoleOper;
	}

	public void setEditingRoleOper(String editingRoleOper) {
		this.editingRoleOper = editingRoleOper;
	}

	public String removeRoleOperation() {
		connector.getRole_operation().remove(rlToDelete);
		return null;
	}

	public String initEditRoleOperation() {
		rlToEdit = new RoleOperation();
		return null;
	}

	public String editRoleOperation() {
		if (editingRoleOper.equals("false")) {
			connector.getRole_operation().add(rlToEdit);
			rlToEdit = new RoleOperation();
		}
		return null;
	}

	public String createDataBaseInicial() {
		assureConnectorManager();
		for (int i = 0; i <= 4; i++) {
			Connector connect = new Connector();

			connect.setName("name" + i);
			if (i % 2 == 0) {
				connect.setType(Connector.TYPE_PROD);
			} else {
				connect.setType(Connector.TYPE_TEST);
			}
			connect.setPath("path" + i);
			connect.setDescription("desc" + i);
			connect.setUrl("url" + i);

			for (int j = 0; j <= 4; j++) {
				RoleOperation roleOper = new RoleOperation();
				roleOper.setRole("role" + j);
				roleOper.setOperation("oper" + j);
				connect.getRole_operation().add(roleOper);
			}

			try {
				connectorManager.createConnector(connect);
			} catch (ConnectorException e) {
				log.error("Create connector failed", e);
				throw new RuntimeException(e);
			}
		}

		return "home";
	}

	public String getPathWsdl() {
		return Connector.NAME_FILE_WSDL;
	}

	public String getPathKeystoreOrg() {
		return this.connector.getKeystoreOrgName();
	}

	public String getPathKeystore() {
		return this.connector.getKeystoreName();
	}

	public String getPathKeystoreSsl() {
		return this.connector.getKeystoreSslName();
	}

	public UploadedFile getImportConnectorFile() {
		return importConnectorFile;
	}

	public void setImportConnectorFile(UploadedFile importConnectorFile) {
		this.importConnectorFile = importConnectorFile;
	}
	
	public void refreshInfoFromWSDL() {
		if (wsdlUploadFile != null) {
			
			try {
				
				// se guarda el wsdl en un directorio temporal para procesarlo
				String tmp_path = Props.getInstance().getProp("TEMP_PATH", Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_TMPPATH);
				tmp_path += connector.getName() + "/";
				
				
				
				File tempPath = new File(tmp_path);
				if (tempPath.exists()) {
					tempPath.delete(); /// Se borra el directorio porque con nobres iguales de conectores se guardan ambos wsdl
				}
				
				tempPath.mkdir();
				
				if (ConnectorFileManager.isZipFile(wsdlUploadFile.getName())) {
					ConnectorFileManager.descompressZip(tmp_path, wsdlUploadFile.getInputStream());					
				} else {
					ConnectorFileManager.writeToFile(tmp_path + Connector.NAME_FILE_WSDL, wsdlUploadFile.getInputStream());
				}
				
				getWsdlOperations(tmp_path);
				
				// indico que ya se hizo el fetch de operaciones, para que el save no lo vuelva a hacer
				fetchWsdlOperations = false;
				
			} catch(Exception e) {
				log.error(e.getLocalizedMessage(), e);
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: No se pudo procesar el wsdl", "");
				FacesContext.getCurrentInstance().addMessage(null, fm);
			}
			
		} else {
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Se debe elegir un wsdl para actualizar", "");
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		
		
	}
	
	public String importToXml() {
		if (importConnectorFile == null) {
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ERROR: Se debe elegir un archivo a importar", "");
			FacesContext.getCurrentInstance().addMessage(null, fm);

			return null;
		}

		assureConnectorManager();
		try {
			InputStream inputStream = importConnectorFile.getInputStream();
			// Connector con = connectorManager.importConnector(inputStream);

			JAXBContext context = JAXBContext.newInstance(Connector.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Connector con = (Connector) unmarshaller.unmarshal(inputStream);
			
			
			
			List<RoleOperation> role_operation = con.getRole_operation();
			Iterator<RoleOperation> it= role_operation.iterator();
			
			while (it.hasNext()){
				RoleOperation roleOperation = it.next();
				roleOperation.setSoapAction(roleOperation.getWsaAction());
			}
			
			con.setRole_operation(role_operation);
			
			
			
			
			if (con.getVersion() == null) {
				con.setVersion("1");
				con.setEnableLocalConf(true);
				con.setEnableUserToken(true);
			}
			
			Long idConnector = connectorManager.createConnector(con);
			con.setId(idConnector);

			try {				
				ConnectorFileManager.importConnectorFiles(connectorManager, con);

				importConnectorFile = null;
				connector = con;
				return view();
			} catch (Exception e) {
				log.error("Failed to extract file contents", e);

				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return null;
			}

		} catch (Exception e) {
			log.error("El archivo a importar es incorrecto", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					//e.getMessage(), e.getMessage());
					"ERROR: El archivo a importar es incorrecto. " + e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);

			return null;
		}
	}

	public String chooseFiles() {
		assureConnectorManager();
		try {
			connectorManager.checkConnectorValidation(connector);
		} catch (ConnectorException e) {
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);

			return null;
		}

		return "chooseFile";
	}

	public String getTypeProduction() {
		return Connector.TYPE_PROD;
	}

	public String getTypeTesting() {
		return Connector.TYPE_TEST;
	}
	
	public String getTypeProductionLabel() {
		return Connector.TYPE_PROD_LABEL;
	}

	public String getTypeTestingLabel() {
		return Connector.TYPE_TEST_LABEL;
	}
	
	public boolean isTestType() {
		return connector.getType().equals(Connector.TYPE_TEST);
	}
	
	public boolean isAssociated() {
		
		return connector.getConnectorAssociated() != null;
	}
	
	public String getServerProductionPath() {
		String host = Props.getInstance().getProp("HOST",
				Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_HOST); // "connector-pge/connector-pge.properties");
		String port = Props.getInstance().getProp("PORT_PROD",
				Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_PORT_PROD); // "connector-pge/connector-pge.properties");
		return "http://" + host + ":" + port;
	}

	public String getServerTestingPath() {
		String host = Props.getInstance().getProp("HOST",
				Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_HOST); // "connector-pge/connector-pge.properties");
		String port = Props.getInstance().getProp("PORT_TEST",
				Constants.PATH_PROPERTIES_FILE, Constants.DEFAULT_PORT_TEST); // "connector-pge/connector-pge.properties");
		return "http://" + host + ":" + port;
	}

	public void validateName(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ValidatorException {
		String name = (String) arg2;
		Pattern p = Pattern.compile("[^A-Za-z0-9_-]");

		if (p.matcher(name).find()) {
			throw new ValidatorException(new FacesMessage("Tiene caracteres incorrectos."));
		}

	}

	public RoleOperation getRlToAdd() {
		return new RoleOperation();
	}
	
	private ConnectorManager assureConnectorManager() {
		if (connectorManager == null) {
			init();
		}		
		return connectorManager;
	}

	public String getKeystoreOrganismoPass() {
		return keystoreOrganismoPass;
	}

	public void setKeystoreOrganismoPass(String keystoreOrganismoPass) {
		this.keystoreOrganismoPass = keystoreOrganismoPass;
	}

	public String getKeystoreSSLPass() {
		return keystoreSSLPass;
	}

	public void setKeystoreSSLPass(String keystoreSSLPass) {
		this.keystoreSSLPass = keystoreSSLPass;
	}

	public String getUserNameTokenPass() {
		return userNameTokenPass;
	}

	public void setUserNameTokenPass(String userNameTokenPass) {
		this.userNameTokenPass = userNameTokenPass;
	}

	public String getTrustorePass() {
		return trustorePass;
	}

	public void setTrustorePass(String trustorePass) {
		this.trustorePass = trustorePass;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void validateKeystoreOrg() {
		validateKeystoreOrg(connector, filesPathBean);
	}
	
	public void validateKeystoreSSL() {
		validateKeystoreSSL(connector, filesPathBean);
	}
	
	public boolean validateKeystoreOrg(Connector con, FilesPathBean fpb) {
		try {
			
			if (!connector.isExistKeystoreOrg()) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Keystore a\u00FAn no definido", null);
				FacesContext.getCurrentInstance().addMessage("form1:keystoreOrgFile", fm);
				return false;
			}
			
			String pass;
			if (keystoreOrganismoPass.equals(DEFAULT_PASS)) {
				pass = PasswordManager.decrypt(connector.getPasswordKeystoreOrg());
				if (pass == null) {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Password incorrecto", null);
					FacesContext.getCurrentInstance().addMessage("form1:keystoreOrgFile", fm);
					return false;
				}
			} else {
				pass = keystoreOrganismoPass; 
			}
			
			return validateKeystore(fpb.getKeystoreOrgFilePath(), "form1:keystoreOrgFile", pass, con.getAliasKeystore(), true, "", false, false);
			
		} catch (ConnectorException e) {
			log.error("Error al desencriptar el password");
			return false;
		}
	}
	
	public boolean validateKeystoreSSL(Connector con, FilesPathBean fpb) {
		try {
			
			if (!connector.isExistKeystore()) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Keystore a\u00FAn no definido", null);
				FacesContext.getCurrentInstance().addMessage("form1:keystoreFile", fm);
				return false;
			}
			
			String pass;
			if (keystoreSSLPass.equals(DEFAULT_PASS)) {
				pass = PasswordManager.decrypt(con.getPasswordKeystore());
				if (pass == null) {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Password incorrecto", null);
					FacesContext.getCurrentInstance().addMessage("form1:keystoreFile", fm);
					return false;
				}
			} else {
				pass = keystoreSSLPass; 
			}
			return validateKeystore(fpb.getKeystoreSSLFilePath(), "form1:keystoreFile", pass, con.getAliasKeystore(), true, "", true, false);
			
		} catch (ConnectorException e) {
			log.error("Error al desencriptar el password");
			return false;
		}
	}
	
	public boolean validateKeystore(String path, String idField, String passwordKeystore, String aliasKeystore, boolean show, String stringError, boolean keystoreSsl, boolean truststore) {
		
		File keystoreOrg = new File(path);
		
		FileInputStream keyStoreFis;
		KeyStore keyStore;
		X509Certificate x509Certificate;
		try {
			keyStoreFis = new FileInputStream(keystoreOrg);
			
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(keyStoreFis, passwordKeystore.toCharArray());
			
			if (keystoreSsl || truststore) {
				if (keyStore.size() > 0) {
					if (keystoreSsl && show) {
						FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Keystore Validado", null);
						FacesContext.getCurrentInstance().addMessage(idField, fm);
					}
					return true;
				} else {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "No contiene ningun certificado", null);
					FacesContext.getCurrentInstance().addMessage(idField, fm);
					return false;
				}
			}
			
			x509Certificate = (java.security.cert.X509Certificate) keyStore.getCertificate(aliasKeystore);
			if (x509Certificate == null) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "El Keystore no contiene ning\u00FAn certificado con el alias especificado", null);
				FacesContext.getCurrentInstance().addMessage(idField, fm);
				return false;
			}
			
			x509Certificate.checkValidity();
			
					
			if (show) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Keystore Validado", null);
				FacesContext.getCurrentInstance().addMessage(idField, fm);
			}
			return true;
			
		} catch (CertificateExpiredException e) {
			log.error("Certificado vencido", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "Certificado vencido", null);
			FacesContext.getCurrentInstance().addMessage(idField, fm);
			return false;
		} catch (CertificateNotYetValidException e) {
			log.error("Certificado Fuera del rango de validez", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "Certificado fuera del rango de validez", null);
			FacesContext.getCurrentInstance().addMessage(idField, fm);
			return false;
		} catch (KeyStoreException e) {
			log.error("No se encontr\u00F3 certificado", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "No se encontr\u00F3 certificado", null);
			FacesContext.getCurrentInstance().addMessage(idField, fm);
			return false;
		} catch (IOException e) {
			log.error("Password incorrecto", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "Password incorrecto", null);
			FacesContext.getCurrentInstance().addMessage(idField, fm);
			return false;
		} catch (Exception e) {
			log.error("Keystore no validado", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "Keystore no validado", null);
			FacesContext.getCurrentInstance().addMessage(idField, fm);
			return false;
		}
		
	
	}
	
	/** Limpia el estado de ConnectorBean, de manera de editar o crear un nuevo conector **/
	private void resetConnectorBean() {
		keystoreOrganismoPass = DEFAULT_PASS;
		keystoreSSLPass = DEFAULT_PASS;
		trustorePass = DEFAULT_PASS;
		userNameTokenPass = DEFAULT_PASS;
		
		wsdlUploadFile = null;
		keystoreOrgUploadFile = null;
		keystoreUploadFile = null;
		keystoreSslUploadFile = null;
	}

	public String getTypeAssociated() {
		if (connector.getType().equals(Connector.TYPE_TEST)){
			return Connector.TYPE_PROD;
		}
		return Connector.TYPE_TEST;
	}
	
	public String validateFields(Connector con) {
		List<RoleOperation> roleOperationList = con.getRole_operation();
			for(RoleOperation rolOp : roleOperationList) {
				if (rolOp.getWsaAction().length() > MAX_LENGTH_WSA_ACTION) {
					log.error("save failed");
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "El campo wsa:Action tiene un m\u00E1ximo permitido de " + MAX_LENGTH_WSA_ACTION, null);
					FacesContext.getCurrentInstance().addMessage(null, fm);
					return null;
				}
					
			}
		return "ok";
	}
	
	public boolean getKeystoreOrgRequired() {
		return !(connector.isExistKeystoreOrg()) && !(this.keystoreOrgUploadFile != null);
	}
	
	public boolean getKeystoreRequired() {
		return !(connector.isExistKeystore()) && !(this.keystoreUploadFile != null);
	}
	
	public boolean getKeystoreSslRequired() {
		return !(connector.isExistKeystoreSsl()) && !(this.keystoreSslUploadFile != null);
	}
	
	public boolean getWsdlRequired() {
		return !(connector.isExistWsdl()) && !(this.wsdlUploadFile != null);
	}

	public FilesPathBean getFilesPathBean() {
		return filesPathBean;
	}

	public void setFilesPathBean(FilesPathBean filesPathBean) {
		this.filesPathBean = filesPathBean;
	}

	public int getScrollerPage() {
		return scrollerPage;
	}


	public void setScrollerPage(int scrollerPage) {
		this.scrollerPage = scrollerPage;
	}

	public boolean isEditingToProduction() {
		return editingToProduction;
	}

	public void setEditingToProduction(boolean editingToProduction) {
		this.editingToProduction = editingToProduction;
	}
	
	public void cleanKeystoreOrg() {
		this.keystoreOrgUploadFile = null;
	}
	
	public void cleanKeystore() {
		this.keystoreUploadFile = null;
	}
	
	public void cleanKeystoreSsl() {
		this.keystoreSslUploadFile = null;
	}
	
	public void cleanWsdl() {
		this.wsdlUploadFile = null;
	}

	public String getNameKeystoreOrgUploadFile() {
		return nameKeystoreOrgUploadFile;
	}

	public void setNameKeystoreOrgUploadFile(String nameKeystoreOrgUploadFile) {
		this.nameKeystoreOrgUploadFile = nameKeystoreOrgUploadFile;
	}

	public String getNameKeystoreUploadFile() {
		return nameKeystoreUploadFile;
	}

	public void setNameKeystoreUploadFile(String nameKeystoreUploadFile) {
		this.nameKeystoreUploadFile = nameKeystoreUploadFile;
	}

	public String getNameKeystoreSslUploadFile() {
		return nameKeystoreSslUploadFile;
	}

	public void setNameKeystoreSslUploadFile(String nameKeystoreSslUploadFile) {
		this.nameKeystoreSslUploadFile = nameKeystoreSslUploadFile;
	}

	public String getNameWsdlUploadFile() {
		return nameWsdlUploadFile;
	}

	public void setNameWsdlUploadFile(String nameWsdlUploadFile) {
		this.nameWsdlUploadFile = nameWsdlUploadFile;
	}
	
	
}
