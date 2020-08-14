package uy.gub.agesic.connector.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorKeystoreType;
import uy.gub.agesic.connector.entity.ConnectorPaths;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;

public class ConnectorFileManager {
	private static Log log = LogFactory.getLog(ConnectorFileManager.class);

	public static void loadFilesData(Connector connector, ConnectorManager connectorManager) {
		String path = ConnectorFileManager.getFilePath(connector, connectorManager);
		File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
		File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
		File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
		File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
		File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
		File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		if (fileWsdl.exists()) {
			connector.setWsdl(ConnectorFileManager.getByteArrayFromFile(fileWsdl)); 
		}
		if (fileKeyOrg.exists() || fileKeyOrg2.exists()) {
			if (fileKeyOrg2.exists()) {
				connector.setKeystoreOrg(ConnectorFileManager.getByteArrayFromFile(fileKeyOrg2));
			} else {
				connector.setKeystoreOrg(ConnectorFileManager.getByteArrayFromFile(fileKeyOrg));
			}
		}
		if (fileKey.exists() || fileKey2.exists()) {
			if (fileKey2.exists()) {
				connector.setKeystore(ConnectorFileManager.getByteArrayFromFile(fileKey2));
			} else {
				connector.setKeystore(ConnectorFileManager.getByteArrayFromFile(fileKey));
			}
		}
		if (fileKeySsl.exists() || fileKeySsl2.exists()) {
			if (fileKeySsl2.exists()) {
				connector.setKeystoreSsl(ConnectorFileManager.getByteArrayFromFile(fileKeySsl2));
			} else {
				connector.setKeystoreSsl(ConnectorFileManager.getByteArrayFromFile(fileKeySsl));					
			}				
		}
	}

	public static void loadFilesInfo(Connector connector, ConnectorManager connectorManager) {		
		String path = getFilePath(connector, connectorManager);
		File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
		File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
		File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
		File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
		File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
		File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		if (fileWsdl.exists()) {
			connector.setExistWsdl(true);
		}
		if (fileKeyOrg.exists() || fileKeyOrg2.exists()) {
			connector.setExistKeystoreOrg(true);
			if (fileKeyOrg2.exists()) {
				connector.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG2);
			} else {
				connector.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG);
			}
		}
		if (fileKey.exists() || fileKey2.exists()) {
			connector.setExistKeystore(true);
			if (fileKey2.exists()) {
				connector.setKeystoreName(Connector.NAME_FILE_KEYSTORE2);
			} else {
				connector.setKeystoreName(Connector.NAME_FILE_KEYSTORE);
			}
		}
		if (fileKeySsl.exists() || fileKeySsl2.exists()) {
			connector.setExistKeystoreSsl(true);
			if (fileKeySsl2.exists()) {
				connector.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL2);
			} else {
				connector.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL);
			}
		}
	}
	
	public static void createfiles(Connector connector, ConnectorManager connectorManager, InputStream inputWsdl, 
			InputStream inputkeystoreOrg, InputStream inputKeyStore, InputStream inputKeyStoreSsl) throws Exception {
		String pathFiles = getFilePath(connector, connectorManager);
		File folder = new File(pathFiles);
		folder.mkdir();
		if (inputWsdl != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_WSDL, inputWsdl);
			connector.setExistWsdl(true);
		}

		if (inputkeystoreOrg != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE_ORG2, inputkeystoreOrg);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE_ORG);
			connector.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG2);
			connector.setExistKeystoreOrg(true);
		}

		if (inputKeyStore != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE2, inputKeyStore);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE);
			connector.setKeystoreName(Connector.NAME_FILE_KEYSTORE2);
			connector.setExistKeystore(true);
		}

		if (inputKeyStoreSsl != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE_SSL2, inputKeyStoreSsl);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE_SSL);
			connector.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL2);
			connector.setExistKeystoreSsl(true);
		}			
	}
	
	public static void loadConnectorPaths(ConnectorManager connectorManager, ConnectorPaths cp, Connector connector) throws ConnectorException{
		
		if (connector != null && cp != null) {
			
			String path = getFilePath(connector, connectorManager);
			
			File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
			File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
			File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
			File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
			File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
			File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
			File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
			if (fileWsdl.exists()) {
				cp.setWsdl(fileWsdl.getAbsolutePath()); 
			}
			if (fileKeyOrg.exists() || fileKeyOrg2.exists()) {
				if (fileKeyOrg2.exists()) {
					cp.setKeystoreOrg(fileKeyOrg2.getAbsolutePath());
				} else {
					cp.setKeystoreOrg(fileKeyOrg.getAbsolutePath());
				}
			}
			if (fileKey.exists() || fileKey2.exists()) {
				if (fileKey2.exists()) {
					cp.setKeystore(fileKey2.getAbsolutePath());
				} else {
					cp.setKeystore(fileKey.getAbsolutePath());
				}
			}
			if (fileKeySsl.exists() || fileKeySsl2.exists()) {
				if (fileKeySsl2.exists()) {
					cp.setKeystoreSsl(fileKeySsl2.getAbsolutePath());
				} else {
					cp.setKeystoreSsl(fileKeySsl.getAbsolutePath());					
				}				
			}
		}
	}
	
	public static void importConnectorFiles(ConnectorManager connectorManager, Connector connector) throws Exception {
		InputStream inputWsdl = null; 
		InputStream inputkeystoreOrg = null;
		InputStream inputKeyStore = null;
		InputStream inputKeyStoreSsl = null;
		
		if (connector.getWsdl() != null) {
			inputWsdl = new ByteArrayInputStream(connector.getWsdl());
		}

		if (connector.getKeystoreOrg() != null) {
			inputkeystoreOrg = new ByteArrayInputStream(connector.getKeystoreOrg());
		}

		if (connector.getKeystore() != null) {
			inputKeyStore = new ByteArrayInputStream(connector.getKeystore());
		}

		if (connector.getKeystoreSsl() != null) {
			inputKeyStoreSsl = new ByteArrayInputStream(connector.getKeystoreSsl());
		}
		createfiles(connector, connectorManager, inputWsdl, inputkeystoreOrg, inputKeyStore, inputKeyStoreSsl);
	}
	
	public static void deleteFile(ConnectorManager connectorManager, Connector connector, ConnectorKeystoreType type) {
		String path = getFilePath(connector, connectorManager);
		if (ConnectorKeystoreType.ORGANISMO.equals(type)) {
			deleteFile(path + Connector.NAME_FILE_KEYSTORE_ORG);
			deleteFile(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		}
		if (ConnectorKeystoreType.KEYSTORE_SSL.equals(type)) {
			deleteFile(path + Connector.NAME_FILE_KEYSTORE);
			deleteFile(path + Connector.NAME_FILE_KEYSTORE2);
		}
		if (ConnectorKeystoreType.TRUSTORE.equals(type)) {
			deleteFile(path + Connector.NAME_FILE_KEYSTORE_SSL);
			deleteFile(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		}
	}


	private static byte[] getByteArrayFromFile(File file) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
			buffer.flush();

			return buffer.toByteArray();
			
		} catch (FileNotFoundException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	private static void writeToFile(String pathFileName, InputStream inputStream) throws Exception {
		File file = new File(pathFileName);
		OutputStream output = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) > 0) {
			output.write(buf, 0, len);
		}
		output.close();
		inputStream.close();
	}

	private static void deleteFile(String pathFileName) {
		File file = new File(pathFileName);
		if (file.exists()) {
			file.delete();
		}
	}

	public static String getFilePath(Connector connector, ConnectorManager connectorManager) {
		String basic_path = connectorManager.getBasicPath();
		return basic_path + connector.getName() + "_" + connector.getType() + "/";
	}
}
