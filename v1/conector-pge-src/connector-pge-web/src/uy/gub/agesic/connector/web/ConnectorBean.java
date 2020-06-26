package uy.gub.agesic.connector.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
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
import uy.gub.agesic.connector.entity.RoleOperation;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.util.ConnectorFileManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.Props;

public class ConnectorBean {
	private static Log log = LogFactory.getLog(ConnectorBean.class);
	private XPathFactory xpf = XPathFactory.newInstance();

	private Connector connector = new Connector();

	ConnectorManager connectorManager;
	String type = Connector.TYPE_PROD;

	// PRUEBA
	String pathConnector;

	private UploadedFile wsdlUploadFile;
	private UploadedFile keystoreOrgUploadFile;
	private UploadedFile keystoreUploadFile;
	private UploadedFile keystoreSslUploadFile;

	private RoleOperation rlToDelete;
	private RoleOperation rlToEdit = new RoleOperation();

	private String editingRoleOper = "false";

	// private String importString;
	private UploadedFile importConnectorFile;
	
	private static final String DEFAULT_PASS = "DEFAULT_PASS";
	
	private String keystoreOrganismoPass = DEFAULT_PASS;
	private String keystoreSSLPass = DEFAULT_PASS;
	private String trustorePass = DEFAULT_PASS;
	private String userNameTokenPass = DEFAULT_PASS;

	@PostConstruct
	private void init() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			connectorManager = (ConnectorManager) ctx
					.lookup(Constants.CONNECTOR_MANAGER_REMOTE); // "connector-pge-ear/ConnectorManagerSession/remote");
		} catch (NamingException e) {
			log.error("Initialization failed ", e);
			throw new RuntimeException(e);
		}
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
			ConnectorFileManager.loadFilesInfo(connector, connectorManager);
			return "edit";
		} catch (ConnectorException e) {
			log.error("edit failed", e);
			throw new RuntimeException(e);
		}
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
			assureConnectorManager();
			
			if (keystoreOrganismoPass == null || !keystoreOrganismoPass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystoreOrg(keystoreOrganismoPass);
			}
			if (keystoreSSLPass == null || !keystoreSSLPass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystore(keystoreSSLPass);
			}
			if (trustorePass == null || !trustorePass.equals(ConnectorBean.DEFAULT_PASS)) {
				connector.setPasswordKeystoreSsl(trustorePass);
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
				if (wsdlUploadFile != null) {
					inputWsdl = wsdlUploadFile.getInputStream();
				}

				if (keystoreOrgUploadFile != null) {
					inputkeystoreOrg = keystoreOrgUploadFile.getInputStream();
				}

				if (keystoreUploadFile != null) {
					inputkeystore = keystoreUploadFile.getInputStream();
				}

				if (keystoreSslUploadFile != null) {
					inputkeystoreSsl = keystoreSslUploadFile.getInputStream();
				}
				
				ConnectorFileManager.createfiles(connector, connectorManager, inputWsdl, 
						inputkeystoreOrg, inputkeystore, inputkeystoreSsl);
				
				keystoreOrganismoPass = DEFAULT_PASS;
				keystoreSSLPass = DEFAULT_PASS;
				trustorePass = DEFAULT_PASS;
				userNameTokenPass = DEFAULT_PASS;
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

		return "edit";
	}

	public String printConnectors() {
		assureConnectorManager();
		try {
			System.out.println(connectorManager.getConnectors(type));
			return "edit";
		} catch (ConnectorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Connector> getConnectors() {
		assureConnectorManager();
		try {
			return connectorManager.getConnectors(type);
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public UploadedFile getWsdlUploadFile() {
		return wsdlUploadFile;
	}

	public void setWsdlUploadFile(UploadedFile wsdlUploadFile) {
		if (this.wsdlUploadFile != null && wsdlUploadFile == null) {
			return;
		}
		this.wsdlUploadFile = wsdlUploadFile;
		if (wsdlUploadFile == null) {
			return;
		}
		
		XPath xpath = xpf.newXPath();
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		namespaces.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		
		uy.gub.agesic.connector.util.XPathNamespaceContext namespaceContext = new uy.gub.agesic.connector.util.XPathNamespaceContext();
		for (Entry<String, String> entry : namespaces.entrySet()) {
			namespaceContext.setMapping(entry.getKey(), entry.getValue());
		}

		xpath.setNamespaceContext(namespaceContext);
		
		String queryXPath;
		try {
			queryXPath = xpath.evaluate("/wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location" , getInputSource(wsdlUploadFile.getBytes()));
			connector.setUrl(queryXPath);
		} catch (Exception e) {
			log.error("No se pudo obtener la URL del wsdl", e);
		}
		
		
		try {
			List<RoleOperation> role_operation = new LinkedList<RoleOperation>();
			String operationsCount = xpath.evaluate("count(/wsdl:definitions/wsdl:binding/wsdl:operation)" , getInputSource(wsdlUploadFile.getBytes()));
			int operations = Integer.parseInt(operationsCount);
			for (;operations > 0; operations --) {
				String query = "/wsdl:definitions/wsdl:binding/wsdl:operation[" + operations +"]/@name";
				String operationName = xpath.evaluate(query , getInputSource(wsdlUploadFile.getBytes()));
				
				query = "/wsdl:definitions/wsdl:portType/wsdl:operation[@name = '" + operationName +"']/wsdl:input/@message";
				String messageName = xpath.evaluate(query , getInputSource(wsdlUploadFile.getBytes()));
				
				String messageNameNoPrefix = messageName;
				int index = messageName.indexOf(":");
				if (index > -1) {
					messageNameNoPrefix = messageName.substring(index + 1);
				}
				
				query = "/wsdl:definitions/wsdl:message[@name = '" + messageName + "' or @name = '" + messageNameNoPrefix + "']/wsdl:part/@element";
				String elementName = xpath.evaluate(query , getInputSource(wsdlUploadFile.getBytes()));
				index = elementName.indexOf(":");
				if (index > -1) {
					elementName = elementName.substring(index + 1);
				}
				
				RoleOperation roleOperation = new RoleOperation();
				roleOperation.setOperation(elementName);
				roleOperation.setOperationFromWSDL(operationName);
				role_operation.add(roleOperation);
			}
			
			connector.setRole_operation(role_operation);
			connector.setExistWsdl(this.wsdlUploadFile != null);
		} catch (Exception e) {
			log.error("No se pudo procesar las operaciones del wsdl", e);
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
		connector.setExistKeystoreOrg(this.keystoreOrgUploadFile != null);
	}

	public UploadedFile getKeystoreUploadFile() {
		return keystoreUploadFile;
	}

	public void setKeystoreUploadFile(UploadedFile keystoreUploadFile) {
		if (keystoreUploadFile == null) {
			return;
		}
		this.keystoreUploadFile = keystoreUploadFile;
		connector.setExistKeystore(this.keystoreUploadFile != null);
	}

	public UploadedFile getKeystoreSslUploadFile() {
		return keystoreSslUploadFile;
	}

	public void setKeystoreSslUploadFile(UploadedFile keystoreSslUploadFile) {
		if (keystoreSslUploadFile == null) {
			return;
		}
		this.keystoreSslUploadFile = keystoreSslUploadFile;
		connector.setExistKeystoreSsl(this.keystoreSslUploadFile != null);
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
		//this.setWsdlUploadFile(wsdlUploadFile);		
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
}