package uy.gub.agesic.connector.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import uy.gub.agesic.connector.PasswordManager;
import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.GlobalConfigurationManager;
import uy.gub.agesic.connector.util.ConnectorFileManager;
import uy.gub.agesic.connector.util.Constants;
import uy.gub.agesic.connector.util.FilesPathBean;

public class GlobalConfigurationBean {
	private static Log log = LogFactory.getLog(GlobalConfigurationBean.class);
	
	private GlobalConfiguration globalConf;

	GlobalConfigurationManager globalConfManager;
	private String type = Connector.TYPE_PROD;

	private static final String DEFAULT_PASS = "DEFAULT_PASS";
	private String keystoreOrganismoPass = DEFAULT_PASS;
	private String keystoreSSLPass = DEFAULT_PASS;
	private String trustorePass = DEFAULT_PASS;
	private UploadedFile keystoreUploadFile;
	private UploadedFile keystoreSslUploadFile;
	private UploadedFile keystoreOrgUploadFile;
	private String condicionesUsoFileName = Constants.CONDICIONES_USO_FILENAME;
	private String nameKeystoreOrgUploadFile;
	private String nameKeystoreUploadFile;
	private String nameKeystoreSslUploadFile;
	
	private FilesPathBean filePathBean;
	
	public String getCondicionesUsoFileName() {
		return condicionesUsoFileName;
	}

	public void setCondicionesUsoFileName(String condicionesUsoFileName) {
		this.condicionesUsoFileName = condicionesUsoFileName;
	}

	public String getPathKeystoreOrg() {
		return this.globalConf.getKeystoreOrgName();
	}
	
	public String getPathKeystore() {
		return this.globalConf.getKeystoreName();
	}

	
	public String getPathKeystoreSsl() {
		return this.globalConf.getKeystoreSslName();
	}
	
	@PostConstruct
	private void init() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			globalConfManager = (GlobalConfigurationManager) ctx
					.lookup(Constants.GLOBAL_CONFIGURATION_MANAGER_REMOTE); 
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
	
	public GlobalConfiguration getGlobalConfiguration(String type) {
		assureConnectorManager();
		GlobalConfiguration globalConfig = globalConfManager.getGlobalConfiguration(type);
		if (globalConfig == null){
			globalConfig = new GlobalConfiguration(type);
		}
		return globalConfig;
	}

	public String edit() {
		assureConnectorManager();
		initForm(type);
			
		return "configurationEdit";
	}
	
	private void initForm (String type) {
		
		globalConf = getGlobalConfiguration(type);
		filePathBean = ConnectorFileManager.loadFilesInfoGlobalConf(globalConf, globalConfManager);
		
		keystoreOrganismoPass = DEFAULT_PASS;
		keystoreSSLPass = DEFAULT_PASS;
		trustorePass = DEFAULT_PASS;
		
		keystoreOrgUploadFile = null;
		keystoreUploadFile = null;
		keystoreSslUploadFile = null;
		
	}
	
	public String save() {
		try {
			assureConnectorManager();
			
			if (keystoreOrganismoPass == null || !keystoreOrganismoPass.equals(GlobalConfigurationBean.DEFAULT_PASS)) {
				globalConf.setPasswordKeystoreOrg(PasswordManager.encrypt(keystoreOrganismoPass));
			}
			if (keystoreSSLPass == null || !keystoreSSLPass.equals(GlobalConfigurationBean.DEFAULT_PASS)) {
				globalConf.setPasswordKeystore(PasswordManager.encrypt(keystoreSSLPass));
			}
			if (trustorePass == null || !trustorePass.equals(GlobalConfigurationBean.DEFAULT_PASS)) {
				globalConf.setPasswordKeystoreSsl(PasswordManager.encrypt(trustorePass));
			}

			globalConfManager.editGlobalConfiguration(globalConf);
			
			try {
				InputStream inputkeystoreOrg = null;
				InputStream inputkeystore = null;
				InputStream inputkeystoreSsl = null;
				
				if (keystoreOrgUploadFile != null) {
					inputkeystoreOrg = keystoreOrgUploadFile.getInputStream();
				}

				if (keystoreUploadFile != null) {
					inputkeystore = keystoreUploadFile.getInputStream();
				}

				if (keystoreSslUploadFile != null) {
					inputkeystoreSsl = keystoreSslUploadFile.getInputStream();
				}
				
				ConnectorFileManager.createfilesGlobalConf(globalConf, globalConfManager, 
						inputkeystoreOrg, inputkeystore, inputkeystoreSsl);
				
				FilesPathBean fpb = ConnectorFileManager.loadFilesInfoGlobalConf(globalConf, globalConfManager);
				
				validateKeystore(fpb.getKeystoreOrgFilePath(), null, PasswordManager.decrypt(globalConf.getPasswordKeystoreOrg()), globalConf.getAliasKeystore(), false, "Configuraci\u00F3n Global - Keystore Organismo: ", false, false);
				validateKeystore(fpb.getKeystoreSSLFilePath(), null, PasswordManager.decrypt(globalConf.getPasswordKeystore()), globalConf.getAliasKeystore(), false, "Configuraci\u00F3n Global - Keystore SSL: ", true, false);
				validateKeystore(fpb.getTruststoreSSLFilePath(), null, PasswordManager.decrypt(globalConf.getPasswordKeystoreSsl()), globalConf.getAliasKeystore(), false, "Configuraci\u00F3n Global - Truststore: ", false, true);
				
				
			} catch (Exception e) {
				log.error("save failed", e);

				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return null;
			}

			return "correct";

		} catch (Exception e) {
			log.error("Exception on save", e);
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);

			return null;
		}
	}
	
	private GlobalConfigurationManager assureConnectorManager() {
		if (globalConfManager == null) {
			init();
		}		
		return globalConfManager;
	}
	
	public void refreshConfigData(ValueChangeEvent e) {
		String newType = (String)e.getNewValue();
		initForm (newType);
	}

	public GlobalConfiguration getGlobalConf() {
		return globalConf;
	}

	public void setGlobalConf(GlobalConfiguration globalConf) {
		this.globalConf = globalConf;
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

	public String getTrustorePass() {
		return trustorePass;
	}

	public void setTrustorePass(String trustorePass) {
		this.trustorePass = trustorePass;
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

	public GlobalConfigurationManager getGlobalConfManager() {
		return globalConfManager;
	}

	public void setGlobalConfManager(GlobalConfigurationManager globalConfManager) {
		this.globalConfManager = globalConfManager;
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
	
	public FilesPathBean getFilePathBean() {
		return filePathBean;
	}

	public void setFilePathBean(FilesPathBean filePathBean) {
		this.filePathBean = filePathBean;
	}
	
	public boolean validateKeystoreOrg() {
		try {
			
			if (!globalConf.isExistKeystoreOrg()) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Keystore a\u00FAn no definido", null);
				FacesContext.getCurrentInstance().addMessage("form1:keystoreOrgFile", fm);
				return false;
			}
			
			String pass;
			if (keystoreOrganismoPass.equals(DEFAULT_PASS)) {
				pass = PasswordManager.decrypt(globalConf.getPasswordKeystoreOrg());
				if (pass == null) {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Password incorrecto", null);
					FacesContext.getCurrentInstance().addMessage("form1:keystoreOrgFile", fm);
					return false;
				}
			} else {
				pass = keystoreOrganismoPass; 
			}
			
			return validateKeystore(filePathBean.getKeystoreOrgFilePath(), "form1:keystoreOrgFile", pass, globalConf.getAliasKeystore(), true, "", false, false);
			
		} catch (ConnectorException e) {
			log.error("Error al desencriptar el password");
			return false;
		}
	}
	
	public boolean validateKeystoreSSL() {
		try {
			
			if (!globalConf.isExistKeystore()) {
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Keystore a\u00FAn no definido", null);
				FacesContext.getCurrentInstance().addMessage("form1:keystoreFile", fm);
				return false;
			}
			
			String pass;
			if (keystoreSSLPass.equals(DEFAULT_PASS)) {
				pass = PasswordManager.decrypt(globalConf.getPasswordKeystore());
				if (pass == null) {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Password incorrecto", null);
					FacesContext.getCurrentInstance().addMessage("form1:keystoreFile", fm);
					return false;
				}
			} else {
				pass = keystoreSSLPass; 
			}
			return validateKeystore(filePathBean.getKeystoreSSLFilePath(), "form1:keystoreFile", pass, globalConf.getAliasKeystore(), true, "", true, false);
			
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
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, stringError + "Trustore sin certificados", null);
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
			log.error("No se encontro certificado", e);
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
	
	
	public boolean getKeystoreOrgRequired() {
		return !(globalConf.isExistKeystoreOrg()) && !(this.keystoreOrgUploadFile != null);
	}
	
	public boolean getKeystoreRequired() {
		return !(globalConf.isExistKeystore()) && !(this.keystoreUploadFile != null);
	}
	
	public boolean getKeystoreSslRequired() {
		return !(globalConf.isExistKeystoreSsl()) && !(this.keystoreSslUploadFile != null);
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
	
	public void cleanKeystoreOrg() {
		this.keystoreOrgUploadFile = null;
	}
	
	public void cleanKeystore() {
		this.keystoreUploadFile = null;
	}
	
	public void cleanKeystoreSsl() {
		this.keystoreSslUploadFile = null;
	}


}
