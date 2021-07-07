package uy.gub.agesic.connector.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.PasswordManager;
import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorKeystoreType;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.util.FilesPathBean;

public class CertificateStoreViewerBean {

	private ConnectorKeystoreType storeType;
	private String storeTitle;
	private String alias;
	private String storedPasswordOrg;
	private String storedPasswordSsl;
	private String storedPasswordTruststore;
	private String keyStoreFilePath;
	private FilesPathBean filesPathBean;
	private Connector connector;
	private boolean storeValidated;
	private String errorMsg;
	private static final String DEFAULT_PASS = "DEFAULT_PASS";
	
	private String keyStorePwd = DEFAULT_PASS;
	private String keyStoreSSLPwd = DEFAULT_PASS;
	private String truststorePwd = DEFAULT_PASS;
	
	private static Log log = LogFactory.getLog(CertificateStoreViewerBean.class);
	
	private List<KeystoreInfo> keystoreInfoList;
	
	public String showCertificates() {
		
		try {
		
			storeValidated = true;
			errorMsg = null;
			
			switch (storeType) {
			case ORGANISMO:
				storeTitle = "Keystore Organismo";
				
					
					File keyStoreFile = new File(filesPathBean.getKeystoreOrgFilePath());
					FileInputStream keyStoreFis;
				
					keyStoreFis = new FileInputStream(keyStoreFile);
					
					KeyStore keyStore = KeyStore.getInstance("JKS");
					String pass;
					if (keyStorePwd.equals(DEFAULT_PASS)) {
						pass = PasswordManager.decrypt(storedPasswordOrg);
						if (pass == null) {
							storeValidated = false;
							errorMsg = "Error en validaci\u00F3n de Certificado";
							return null;
						}
					} else {
						pass = keyStorePwd; 
					}
					String decryptPasskeyStore = pass;
					keyStore.load(keyStoreFis, decryptPasskeyStore.toCharArray());
					
					X509Certificate x509Certificate = (java.security.cert.X509Certificate) keyStore.getCertificate(alias);
					
					keystoreInfoList = new ArrayList<KeystoreInfo>();
					
		            KeystoreInfo keystoreInfo = new KeystoreInfo();
		            keystoreInfo.setAlias(alias);
		            keystoreInfo.setType(getTypeKeystore(keyStore));
		            keystoreInfo.setProvider(keyStore.getProvider().getName());
		            keystoreInfo.setCreationDate(keyStore.getCreationDate(alias));
		            keystoreInfo.setExpirationDate(x509Certificate.getNotAfter());
		            
		            keystoreInfoList.add(keystoreInfo);
		            
				break;
			
			case KEYSTORE_SSL:
				storeTitle = "Keystore SSL";
				
					File keyStoreSSLFile = new File(filesPathBean.getKeystoreSSLFilePath());
					FileInputStream keyStoreSSLFis = new FileInputStream(keyStoreSSLFile);
					
					KeyStore keyStoreSSL = KeyStore.getInstance("JKS");
					String passSSL;
					if (keyStoreSSLPwd.equals(DEFAULT_PASS)) {
						passSSL = PasswordManager.decrypt(storedPasswordSsl);
						if (passSSL == null) {
							storeValidated = false;
							errorMsg = "Error en validaci\u00F3n de Certificado";
							return null;
						}
					} else {
						passSSL = keyStoreSSLPwd; 
					}
					String decryptPasskeyStoreSSL = passSSL;
					keyStoreSSL.load(keyStoreSSLFis, decryptPasskeyStoreSSL.toCharArray());
					
					
					keystoreInfoList = new ArrayList<KeystoreInfo>();
					Enumeration<String> enumeration = keyStoreSSL.aliases();
			        while(enumeration.hasMoreElements()) {
			            String alias = enumeration.nextElement();
			            X509Certificate sslCertificate = (java.security.cert.X509Certificate)  keyStoreSSL.getCertificate(alias);
					
			            KeystoreInfo keystoreInfoSsl = new KeystoreInfo();
			            keystoreInfoSsl.setAlias(alias);
			            keystoreInfoSsl.setType(getTypeKeystore(keyStoreSSL));
			            keystoreInfoSsl.setProvider(keyStoreSSL.getProvider().getName());
			            keystoreInfoSsl.setCreationDate(keyStoreSSL.getCreationDate(alias));
			            keystoreInfoSsl.setExpirationDate(sslCertificate.getNotAfter());
		            
			            keystoreInfoList.add(keystoreInfoSsl);
			        }
					
				break;
				
			case TRUSTORE:
				storeTitle = "Truststore SSL";
				File truststoreFile = new File(filesPathBean.getTruststoreSSLFilePath());
				FileInputStream truststoreFis = new FileInputStream(truststoreFile);
				
				KeyStore truststore = KeyStore.getInstance("JKS");
				String passTrustore;
				if (truststorePwd.equals(DEFAULT_PASS)) {
					passTrustore = PasswordManager.decrypt(storedPasswordTruststore);
					if (passTrustore == null) {
						storeValidated = false;
						errorMsg = "Error en validaci\u00F3n de Certificado";
						return null;
					}
				} else {
					passTrustore = truststorePwd; 
				}
				
				String decryptPasstruststoreFile = passTrustore;
				truststore.load(truststoreFis, decryptPasstruststoreFile.toCharArray());
				
				keystoreInfoList = new ArrayList<KeystoreInfo>();
				Enumeration<String> enumeration2 = truststore.aliases();
		        while(enumeration2.hasMoreElements()) {
		            String alias = enumeration2.nextElement();
		            X509Certificate truststoreCertificate = (java.security.cert.X509Certificate)  truststore.getCertificate(alias);
		            
		            KeystoreInfo truststoreInfo = new KeystoreInfo();
		            truststoreInfo.setAlias(alias);
		            truststoreInfo.setType(truststore.getType());
		            truststoreInfo.setProvider(truststore.getProvider().getName());
		            truststoreInfo.setCreationDate(truststore.getCreationDate(alias));
		            truststoreInfo.setExpirationDate(truststoreCertificate.getNotAfter());
		            
		            keystoreInfoList.add(truststoreInfo);
		        }
				
				break;
			}
		
		} catch (FileNotFoundException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		} catch (KeyStoreException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		} catch (NoSuchAlgorithmException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		} catch (CertificateException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		} catch (IOException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		} catch (ConnectorException e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		
		} catch (Exception e) {
			log.error("Error en validaci\u00F3n de Certificado", e);
			storeValidated = false;
			errorMsg = "Error en validaci\u00F3n. Ver log para detalles.";
		}	
		
		return null;
		
	}
	
	
	private String getTypeKeystore(KeyStore keyStore) {
		
		try {
			if (keyStore.isKeyEntry(alias)) {
				return "PrivateKeyEntry";
			}else{
				return "PublicEntry";
			}
		} catch (KeyStoreException e) {
			log.error(e.getLocalizedMessage(), e);
			return null;
		}
	}


	public String getStoreType() {
		return storeType.toString();
	}
	
	public void setStoreType(String storeType) {
		this.storeType = ConnectorKeystoreType.valueOf(storeType);
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getStoreTitle() {
		return storeTitle;
	}

	public void setStoreTitle(String storeTitle) {
		this.storeTitle = storeTitle;
	}

	public String getKeyStoreFilePath() {
		return keyStoreFilePath;
	}

	public void setKeyStoreFilePath(String keyStoreFilePath) {
		this.keyStoreFilePath = keyStoreFilePath;
	}

	public String getKeyStorePwd() {
		return keyStorePwd;
	}

	public void setKeyStorePwd(String keyStorePwd) {
		this.keyStorePwd = keyStorePwd;
	}

	public void setStoreType(ConnectorKeystoreType storeType) {
		this.storeType = storeType;
	}

	public FilesPathBean getFilesPathBean() {
		return filesPathBean;
	}

	public void setFilesPathBean(FilesPathBean filesPathBean) {
		this.filesPathBean = filesPathBean;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public boolean isStoreValidated() {
		return storeValidated;
	}

	public void setStoreValidated(boolean storeValidated) {
		this.storeValidated = storeValidated;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getKeyStoreSSLPwd() {
		return keyStoreSSLPwd;
	}


	public void setKeyStoreSSLPwd(String keyStoreSSLPwd) {
		this.keyStoreSSLPwd = keyStoreSSLPwd;
	}


	public List<KeystoreInfo> getKeystoreInfoList() {
		return keystoreInfoList;
	}


	public void setTruststoreInfoList(List<KeystoreInfo> keystoreInfoList) {
		this.keystoreInfoList = keystoreInfoList;
	}
	

	public String getStoredPasswordOrg() {
		return storedPasswordOrg;
	}


	public void setStoredPasswordOrg(String storedPasswordOrg) {
		this.storedPasswordOrg = storedPasswordOrg;
	}


	public String getStoredPasswordSsl() {
		return storedPasswordSsl;
	}


	public void setStoredPasswordSsl(String storedPasswordSsl) {
		this.storedPasswordSsl = storedPasswordSsl;
	}


	public String getStoredPasswordTruststore() {
		return storedPasswordTruststore;
	}


	public void setStoredPasswordTruststore(String storedPasswordTruststore) {
		this.storedPasswordTruststore = storedPasswordTruststore;
	}


	public String getTruststorePwd() {
		return truststorePwd;
	}


	public void setTruststorePwd(String truststorePwd) {
		this.truststorePwd = truststorePwd;
	}
	
}
