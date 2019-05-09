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
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorKeystoreType;
import uy.gub.agesic.connector.entity.ConnectorPaths;
import uy.gub.agesic.connector.entity.GlobalConfiguration;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import uy.gub.agesic.connector.session.api.ConnectorManager;
import uy.gub.agesic.connector.session.api.GlobalConfigurationManager;

public class ConnectorFileManager {
	private static Log log = LogFactory.getLog(ConnectorFileManager.class);

	public static void loadFilesData(Connector connector, ConnectorManager connectorManager) {
		
		String path = ConnectorFileManager.getFilePath(connector, connectorManager);
		File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
		
		if (fileWsdl.exists()) {
			
			try {
				
				// genero el zip que contiene el wsdl y todos sus recursos
				FileOutputStream fos = new FileOutputStream(path + "connector.zip");
				ZipOutputStream zos = new ZipOutputStream(fos);
				
				File connectorDir = new File(path);
				
				for(File file: connectorDir.listFiles()) {
					String name = file.getName();
					String extension = name.substring(name.lastIndexOf("."));
					if (extension.equalsIgnoreCase(".xml") || extension.equalsIgnoreCase(".wsdl")) {
						ZipEntry ze= new ZipEntry(file.getName());
						zos.putNextEntry(ze);
						
						FileInputStream in = new FileInputStream(file.getAbsolutePath());
						byte[] buffer = new byte[1024];
						
			    		int len;
			    		while ((len = in.read(buffer)) > 0) {
			    			zos.write(buffer, 0, len);
			    		}
			    		
			    		in.close();
			    		zos.closeEntry();
					}
				}
				
				zos.close();
				
				connector.setWsdl(ConnectorFileManager.getByteArrayFromFile(new File(path + "connector.zip")));
				
			} catch(IOException e) {
				log.error("ERROR: No se pudo procesar los archivos del conector" , e);
				FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: No se pudo procesar los archivos del conector", null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
			}
		}
		
		if (connector.isEnableLocalConf()) {
			
			File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
			File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
			File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
			File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
			File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
			File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
			
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
	}

	public static FilesPathBean loadFilesInfo(Connector connector, ConnectorManager connectorManager) {		
		String path = getFilePath(connector, connectorManager);
		File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
		
		if (fileWsdl.exists()) {
			connector.setExistWsdl(true);
		}
		
		if (connector.isEnableLocalConf()) {

			File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
			File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
			File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
			File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
			File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
			File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
			
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
			
			FilesPathBean fpb = new FilesPathBean();
			fpb.setKeystoreOrgFilePath(path + Connector.NAME_FILE_KEYSTORE_ORG2);
			fpb.setKeystoreSSLFilePath(path + Connector.NAME_FILE_KEYSTORE2);
			fpb.setTruststoreSSLFilePath(path + Connector.NAME_FILE_KEYSTORE_SSL2);
			
			return fpb;
		}
		
		return new FilesPathBean();
		
	}
	
	public static FilesPathBean loadFilesInfoGlobalConf(GlobalConfiguration globalConf, GlobalConfigurationManager globalConfManager) {		
		String path = getFilePath(globalConfManager, globalConf.getType());
		File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
		File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
		File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
		File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
		File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		
		if (fileKeyOrg.exists() || fileKeyOrg2.exists()) {
			globalConf.setExistKeystoreOrg(true);
			if (fileKeyOrg2.exists()) {
				globalConf.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG2);
			} else {
				globalConf.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG);
			}
		}
		if (fileKey.exists() || fileKey2.exists()) {
			globalConf.setExistKeystore(true);
			if (fileKey2.exists()) {
				globalConf.setKeystoreName(Connector.NAME_FILE_KEYSTORE2);
			} else {
				globalConf.setKeystoreName(Connector.NAME_FILE_KEYSTORE);
			}
		}
		if (fileKeySsl.exists() || fileKeySsl2.exists()) {
			globalConf.setExistKeystoreSsl(true);
			if (fileKeySsl2.exists()) {
				globalConf.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL2);
			} else {
				globalConf.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL);
			}
		}
		
		FilesPathBean fpb = new FilesPathBean();
		fpb.setKeystoreOrgFilePath(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		fpb.setKeystoreSSLFilePath(path + Connector.NAME_FILE_KEYSTORE2);
		fpb.setTruststoreSSLFilePath(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		
		return fpb;
	}
	
	public static void createfiles(Connector connector, ConnectorManager connectorManager, InputStream inputWsdl, 
			InputStream inputkeystoreOrg, InputStream inputKeyStore, InputStream inputKeyStoreSsl, String nameWsdl, boolean editingToProduction, File wsdlInput) throws Exception {
		String pathFiles = getFilePath(connector, connectorManager);
		File folder = new File(pathFiles);
		folder.mkdir();
		
		if (inputWsdl != null) {
			if (nameWsdl != null && isZipFile(nameWsdl)) {
				try {
					descompressZip(pathFiles, inputWsdl);
				} catch (Exception e) {
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), null);
					FacesContext.getCurrentInstance().addMessage(null, fm);
				}
			} else {
				writeToFile(pathFiles + Connector.NAME_FILE_WSDL, inputWsdl);
				
				// Valido las dependencias que puede tener a otros xsd o wsdl
				WsdlManager w = new WsdlManager();
				
				try {
					w.getFileList(pathFiles, new File(pathFiles + Connector.NAME_FILE_WSDL));
				} catch (Exception e) {
					log.error("Error al escribir el wsdl",e);
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error al obtener importaciones", null);
					FacesContext.getCurrentInstance().addMessage(null, fm);
				}
			}
			connector.setExistWsdl(true);
		} else {
		
			if (wsdlInput != null && editingToProduction) {
				// Grabo el wsdl en el  nuevo conector
				writeToFile(pathFiles + Connector.NAME_FILE_WSDL, new FileInputStream(wsdlInput));
				
				// Obtengo el path del conector asociado para verificar y obtener los archivos importados
				String pathFilesOld = null;
				try {
					pathFilesOld = getFilePath(connector.getConnectorAssociated(), connectorManager);
					
					WsdlManager w = new WsdlManager();
					ArrayList<File> filesArray = w.getFileList(pathFilesOld, wsdlInput);
					for (File file: filesArray) {
						writeToFile(pathFiles + file.getName(), new FileInputStream(file));
					}
				
				} catch (Exception e) {
					log.error("Error al escribir los archivos",e);
					FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), null);
					FacesContext.getCurrentInstance().addMessage(null, fm);
				}
				connector.setExistWsdl(true);
			}
		}
	
		if (connector.isEnableLocalConf()) {
			
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
	}
	
	public static void createfilesGlobalConf (GlobalConfiguration globalConf, GlobalConfigurationManager globalConfManager,  
			InputStream inputkeystoreOrg, InputStream inputKeyStore, InputStream inputKeyStoreSsl) throws Exception {
		String pathFiles = getFilePath(globalConfManager, globalConf.getType());
		File folder = new File(pathFiles);
		folder.mkdir();
		

		if (inputkeystoreOrg != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE_ORG2, inputkeystoreOrg);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE_ORG);
			globalConf.setKeystoreOrgName(Connector.NAME_FILE_KEYSTORE_ORG2);
			globalConf.setExistKeystoreOrg(true);
		}

		if (inputKeyStore != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE2, inputKeyStore);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE);
			globalConf.setKeystoreName(Connector.NAME_FILE_KEYSTORE2);
			globalConf.setExistKeystore(true);
		}

		if (inputKeyStoreSsl != null) {
			writeToFile(pathFiles + Connector.NAME_FILE_KEYSTORE_SSL2, inputKeyStoreSsl);
			deleteFile(pathFiles + Connector.NAME_FILE_KEYSTORE_SSL);
			globalConf.setKeystoreSslName(Connector.NAME_FILE_KEYSTORE_SSL2);
			globalConf.setExistKeystoreSsl(true);
		}			
	}
	 
	public static void loadWsdlPath(ConnectorManager connectorManager, ConnectorPaths cp, Connector connector) {
		if (connector != null && cp != null) {
			String path = getFilePath(connector, connectorManager);
			cp.setConnectorDirPath(path);
			File fileWsdl = new File(path + Connector.NAME_FILE_WSDL);
			if (fileWsdl.exists()) {
				cp.setWsdl(fileWsdl.getAbsolutePath()); 
			}
		}
	}
	
	public static void loadConnectorPaths(ConnectorManager connectorManager, ConnectorPaths cp, Connector connector) throws ConnectorException {
		
		if (connector != null && cp != null) {
			String path = getFilePath(connector, connectorManager);
			loadPaths(path, cp);
		}
	}
	
	public static void loadConnectorPaths(GlobalConfigurationManager globalConfManager, ConnectorPaths cp, String type) throws ConnectorException {
		
		if (cp != null) {
			String path = getFilePath(globalConfManager, type);
			loadPaths(path, cp);
		}
	}
	
	private static void loadPaths(String path, ConnectorPaths cp) {
		
		File fileKeyOrg = new File(path + Connector.NAME_FILE_KEYSTORE_ORG);
		File fileKeyOrg2 = new File(path + Connector.NAME_FILE_KEYSTORE_ORG2);
		File fileKey = new File(path + Connector.NAME_FILE_KEYSTORE);
		File fileKey2 = new File(path + Connector.NAME_FILE_KEYSTORE2);
		File fileKeySsl = new File(path + Connector.NAME_FILE_KEYSTORE_SSL);
		File fileKeySsl2 = new File(path + Connector.NAME_FILE_KEYSTORE_SSL2);
		
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
		
		String fileName = null;
		
		// en caso que el conector sea de version 2 en adelante, indico que se debe descomprimir un zip
		if (!connector.getVersion().equals("1")) {
			String path = getFilePath(connector, connectorManager);
			fileName = path + "connector.zip";
		}
		
		createfiles(connector, connectorManager, inputWsdl, inputkeystoreOrg, inputKeyStore, inputKeyStoreSsl, fileName, false, null);
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

	public static void writeToFile(String pathFileName, InputStream inputStream) throws Exception {
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
	
	public static String getFilePath(GlobalConfigurationManager globalConfManager, String type) {
		String basicPath = globalConfManager.getBasicPath();
		return basicPath + "globalConfig_" + type + "/";
	}
	
	public static void descompressZip(String path, InputStream inputWsdl) throws Exception {
		
		ZipInputStream zis = new ZipInputStream(inputWsdl);
		
		byte[] buffer = new byte[4096];
        ZipEntry ze;
        try {
			while ((ze = zis.getNextEntry()) != null) {
			   FileOutputStream fos = new FileOutputStream(path + ze.getName());
			   int numBytes;
			   while ((numBytes = zis.read(buffer, 0, buffer.length)) != -1) {
				   fos.write(buffer, 0, numBytes);
			   }
			   zis.closeEntry();
			}
			
			WsdlManager w = new WsdlManager();
			File mainWsdl = null;
			File newWsdl = null;
			
			// Busco los xml que hay en el directorio
			ArrayList<String> locationsWsdl = WsdlManager.searchWsdlLocation(path);
			
			// Encuentro el wsdl principal
			mainWsdl = w.searchMainWsdl(path, locationsWsdl);
			
			
			if (mainWsdl != null) {  
			
				// Validar imports
				w.getFileList(path , mainWsdl);
				
				//// Renombrar el wsdl principal
			    newWsdl = new File(path + Connector.NAME_FILE_WSDL);
			    mainWsdl.renameTo(newWsdl);
			    
			} else {
				log.error("No se encontr\u00F3 el wsdl principal");
				throw new IOException("No se encontr\u00F3 el wsdl principal");
			}
			
		} catch (IOException e) {
			log.error("Error al descomprimir el zip", e);
			throw new IOException(e.getMessage());
		} catch (Exception e) {
			log.error("Error al descomprimir el zip", e);
			throw new Exception("Error al descomprimir el zip", e);
		}
	}
	
	public static boolean isZipFile(String name) {
		String extension = name.substring(name.lastIndexOf("."));
		return extension.equals(".zip");
	}
}
