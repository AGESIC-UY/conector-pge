package uy.gub.agesic.connector.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;


public class WsdlManager {
	
	private static Log log = LogFactory.getLog(WsdlManager.class);
	
	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	
	public ArrayList<String> searchXsd(String nameWsdl, boolean isWsdl) throws SAXPathException, IOException, XPathException {
		
		try {
		
			if (isWsdl){
				return parseWsdl(nameWsdl, "//xsd:schema/xsd:import/@schemaLocation");
			} else {
				return parseWsdl(nameWsdl, "//xs:schema/xs:import/@schemaLocation");
			}
		
		} catch (FileNotFoundException e) {
			log.error("No se encontr\u00F3 un archivo xsd de importaci\u00F3n", e);
			throw new FileNotFoundException("No se encontr\u00F3 un archivo xsd de importaci\u00F3n");
		}
		
	}
	
	public ArrayList<String> searchWsdl(String path) throws SAXPathException, IOException, XPathException {
		try {	
			return parseWsdl(path, "//wsdl:import/@location");
			
		} catch (FileNotFoundException e) {
			log.error("No se encontr\u00F3 un archivo wsdl de importaci\u00F3n", e);
			throw new FileNotFoundException("No se encontr\u00F3 un archivo wsdl de importaci\u00F3n");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> parseWsdl(String path, String expression) throws SAXPathException, XPathException, IOException {
		
	  	
		File wsdl = new File(path);
	
		nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
		nsContext.declarePrefix("xs", "http://www.w3.org/2001/XMLSchema");
		nsContext.declarePrefix("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		
		dog = new XMLDog(nsContext, null, null);
		Expression exp = dog.addXPath(expression);
		
		
		ByteArrayOutputStream ous = null;
	    InputStream ios = null;
	    
        byte[] buffer = new byte[4096];
        ous = new ByteArrayOutputStream();
       
			ios = new FileInputStream(wsdl);
		
        int read = 0;
        while ( (read = ios.read(buffer)) != -1 ) {
            ous.write(buffer, 0, read);
        }    
				
		InputSource source = new InputSource(new ByteArrayInputStream(ous.toByteArray()));
		XPathResults results = null;
		
		results = dog.sniff(source);
		
		ArrayList<String> matchesList = new ArrayList<String>();
		
		Object result = results.getResult(exp);
		List<NodeItem> resultList = null;
		if (result != null) {
			resultList = (List<NodeItem>) result;
			for (NodeItem node: resultList) {
				matchesList.add(node.value);
			}
		}
		
		return matchesList;
	}
	
	
	
	
	public static ArrayList<String> searchWsdlLocation(String path) {
		
		ArrayList<String> result = new ArrayList<String>();

		File f = new File(path);
		
		if (f.exists()){
			File[] ficheros = f.listFiles();
			for (int x = 0; x < ficheros.length; x++) {
				String name = ficheros[x].getName();
				String extension = name.substring(name.lastIndexOf("."));
				if (extension.equals(".wsdl") || extension.equals(".xml")) {
					result.add(ficheros[x].getName());
			
				}
			}
		}
		
		return result;
		
	}
	
	public File searchMainWsdl (String path, ArrayList<String> locations) throws Exception {
		
		try {
			for (String location: locations) {
				File f = new File(path + location);
				
				nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
				nsContext.declarePrefix("wsdl", "http://schemas.xmlsoap.org/wsdl/");
				
				dog = new XMLDog(nsContext, null, null);
				Expression exp = dog.addXPath("count(/wsdl:definitions/wsdl:service) != 0");
				
				ByteArrayOutputStream ous = null;
			    InputStream ios = null;
			    
		        byte[] buffer = new byte[4096];
		        ous = new ByteArrayOutputStream();
		        ios = new FileInputStream(f);
		        int read = 0;
		        while ( (read = ios.read(buffer)) != -1 ) {
		            ous.write(buffer, 0, read);
		        }    
						
				InputSource source = new InputSource(new ByteArrayInputStream(ous.toByteArray()));
				XPathResults results = null;
				
				results = dog.sniff(source);
				Object result = results.getResult(exp);
				
				boolean mainWsdl = false;
				if (result instanceof Boolean) {
					mainWsdl = (Boolean) result;
				}
				
				if (mainWsdl) {
					return f;
				}
			}
			
			return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("No se encontro el wsdl principal");
		}

	}
	
	public ArrayList<File> getFileList(String path, File mainWsdl) throws Exception {
		
		ArrayList<File> result = new ArrayList<File>();
		ArrayList<String> nameList;
		
		nameList = getNameList(path, mainWsdl.getName());
		
		// Obtener los archivos a partir de los nombres

		for (String fileName : nameList) {
			result.add(new File(path + fileName));
		}
	
		return result;
		
	}
	
	private ArrayList<String> getNameList(String path, String nameWsdl) throws SAXPathException, IOException, XPathException {
		
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> wsdlList = searchWsdl(path + nameWsdl);
		ArrayList<String> xsdList = searchXsd(path + nameWsdl, true);
		
		// Recursividad sobre los wsdl importados
		if (wsdlList != null) {
			for (String nameImportWsdl : wsdlList) {
				ArrayList<String> list = getNameList(path, nameImportWsdl);
				for (String x : list) {
					if (!result.contains(x)) {
						result.add(x);
					}
				}
				if (!result.contains(nameImportWsdl)) {
					result.add(nameImportWsdl);
				}
			}
		}
		// Recursividad sobre los xsd importados
		if (xsdList != null) {
			for (String nameImportXsd : xsdList) {
				ArrayList<String> list = searchXsdRecursive(path, nameImportXsd);
				for (String x : list) {
					if (!result.contains(x)) {
						result.add(x);
					}
				}
			}
		}
		return result;
		
	}
	
	private ArrayList<String> searchXsdRecursive(String path, String nameXsd) throws SAXPathException, IOException, XPathException {
		
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> xsdList = searchXsd(path + nameXsd, false);
		
		result.add(nameXsd);
		for (String nameImportXsd : xsdList) {
			ArrayList<String> list = searchXsdRecursive(path, nameImportXsd);
			for (String x : list) {
				if (!result.contains(x)) {
					result.add(x);
				}
			}
		}
		return result;
	}
	
	public static String getElementNameForOperation(String pathWsdlDir, ArrayList<String> wsdlFileNames, String operationName, XPath xpath) throws Exception {
		
		String query = null;
		String elementName = null;
		
		for (String wsdlFilename: wsdlFileNames) {
			
			File wsdl = new File(pathWsdlDir + wsdlFilename);
			query = "/wsdl:definitions/wsdl:portType/wsdl:operation[@name = '" + operationName +"']/wsdl:input/@message";
			String messageName = xpath.evaluate(query , new InputSource(new FileInputStream(wsdl)));
			
			if (messageName != null && messageName.length() > 0) {
				
				String messageNameNoPrefix = messageName;
				int index = messageName.indexOf(":");
				if (index > -1) {
					messageNameNoPrefix = messageName.substring(index + 1);
				}
				
				query = "/wsdl:definitions/wsdl:message[@name = '" + messageName + "' or @name = '" + messageNameNoPrefix + "']/wsdl:part/@element";
				elementName = xpath.evaluate(query , new InputSource(new FileInputStream(wsdl)));
				index = elementName.indexOf(":");
				if (index > -1) {
					elementName = elementName.substring(index + 1);
				}
				
				break;
			}
		}
		
		return elementName;
	}
}
