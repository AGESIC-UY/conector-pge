package biz.ideasoft.soa.esb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.log4j.Logger;
import org.jaxen.saxpath.SAXPathException;
import org.jboss.internal.soa.esb.message.format.xml.MessageImpl;
import org.jboss.soa.esb.message.Message;


public class XPathXmlDogUtil {

	private XMLDog dog;
	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	
	private static Logger log = Logger.getLogger(XPathXmlDogUtil.class);	
	
	public List<Object> executeMultipleXPath(Message message, List<String> xpaths, Map<String, String> namespaces) throws SAXPathException, XPathException{
		List<Object> returnList = new LinkedList<Object>();
		try {
			//Namepaces
			for (Map.Entry<String, String> entry : namespaces.entrySet()) {
				nsContext.declarePrefix(entry.getKey(), entry.getValue());
			}
			dog = new XMLDog(nsContext, null, null);

			//XPaths
			Map<String, Expression> exprsCache = new HashMap<String, Expression>();
			for (String xpath : xpaths) {
					
				try {
					Expression xpathExpr = dog.addXPath(xpath);
					exprsCache.put(xpath, xpathExpr);
				} catch (SAXPathException e) {
					e.printStackTrace();
					log.error("Error compiling xpath expression", e);
					throw e;
				}
				
			}
			
			//Execute XPaths
			XPathResults results = dog.sniff(XPathUtil.getInputSource(message));
			for (String xpath : xpaths) {
				Expression expr = exprsCache.get(xpath);
				Object result = results.getResult(expr);
				
				if (result instanceof java.util.Collection) {
					java.util.Collection<NodeItem> items = (java.util.Collection<NodeItem>) result;
					
					result = null;//Seteo null
					for (NodeItem nodeItem : items) {
						result = nodeItem.value;//Me quedo con el primer valor de la lista
						break;
					}
				}
				/*else if (result instanceof Boolean){
					Boolean bool = (Boolean)result;
				}
				else if (result instanceof String){
					String str = (String)result;
				}
				else if (result instanceof Double){
					Double dou = (Double)result;
				}
				System.out.println("---------");
				System.out.println("xPath = "+xpath);
				System.out.println("obj_result = "+result);
				System.out.println("---------");*/
				
				returnList.add(result);
			}
		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw e;
		} catch (XPathException e) {
			e.printStackTrace();
			throw e;
		}
		
		return returnList;
	}

	/** Idem a executeMultipleXPath pero los resultados pueden ser colecciones de objetos **/
	public List<Object> executeMultipleXPathWithCollections(Message message, List<String> xpaths, Map<String, String> namespaces) throws SAXPathException, XPathException{
		List<Object> returnList = new LinkedList<Object>();
		try {
			//Namepaces
			for (Map.Entry<String, String> entry : namespaces.entrySet()) {
				nsContext.declarePrefix(entry.getKey(), entry.getValue());
			}
			dog = new XMLDog(nsContext, null, null);

			//XPaths
			Map<String, Expression> exprsCache = new HashMap<String, Expression>();
			for (String xpath : xpaths) {
					
				try {
					Expression xpathExpr = dog.addXPath(xpath);
					exprsCache.put(xpath, xpathExpr);
				} catch (SAXPathException e) {
					e.printStackTrace();
					log.error("Error compiling xpath expression", e);
					throw e;
				}
				
			}
			
			//Execute XPaths
			XPathResults results = dog.sniff(XPathUtil.getInputSource(message));
			for (String xpath : xpaths) {
				Expression expr = exprsCache.get(xpath);
				Object result = results.getResult(expr);
				
				if (result instanceof java.util.Collection) {
					java.util.Collection<NodeItem> items = (java.util.Collection<NodeItem>) result;
					
					// me fijo si el resultado es una lista, o solo un elemento
					if (items.size() <= 1) {
						
						result = null;
						for (NodeItem nodeItem : items) {
							result = nodeItem.value;
						}
						
						// agrego el string al resultado
						returnList.add(result);
						
					} else {
						// creo la coleccion resultado para la query actual
						List<String> itemResult = new ArrayList<String>();
						
						for (NodeItem nodeItem : items) {
							result = itemResult.add(nodeItem.value);
						}
						// agrego la coleccion al resultado
						returnList.add(itemResult);
					}
					
				} else {
					returnList.add(result);
				}
			}
				
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw e;
		} catch (XPathException e) {
			e.printStackTrace();
			throw e;
		}
		
		return returnList;
	}

	public static void main(String[] args) {
		XPathXmlDogUtil xmlDogUtil = new XPathXmlDogUtil();
		List<String> xpaths = new LinkedList<String>();
		xpaths.add("/soapenv:Envelope/soapenv:Header/pge:MessageType/text() = 'TEST'");
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");  
		namespaces.put("pge", "http://pge.agesic.gub.uy/MessageType/");
//		namespaces.put("ps", "http://ps.agesic.gub.uy");
//		namespaces.put("p675", "http://tempuri.org/");
//		namespaces.put("soapenc", "http://schemas.xmlsoap.org/soap/encoding/");
		
		String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ps=\"http://ps.agesic.gub.uy\" xmlns:pge=\"http://pge.agesic.gub.uy/MessageType/\">"
				   + "<soapenv:Header>"
				   +   "<pge:MessageType>TEST</pge:MessageType>"
				   + "</soapenv:Header>"
				   + "<soapenv:Body>"
				   +    "<ps:NotificationRequest>"
				   +       "<subscriber>Testing</subscriber>"
				   +       "<topic>NovedadesPersonas</topic>"
				   +   "</ps:NotificationRequest>"
				   + "</soapenv:Body>"
				   + "</soapenv:Envelope>";
		String payload2 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ps=\"http://ps.agesic.gub.uy\" xmlns:pge=\"http://pge.agesic.gub.uy/MessageType/\">"
			   + "<soapenv:Header>"
			   + "</soapenv:Header>"
			   + "<soapenv:Body>"
			   +    "<ps:NotificationRequest>"
			   +       "<subscriber>Testing</subscriber>"
			   +       "<topic>NovedadesPersonas</topic>"
			   +   "</ps:NotificationRequest>"
			   + "</soapenv:Body>"
			   + "</soapenv:Envelope>";
		String payload3 = 
						"<soapenv:Envelope xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance/\" xmlns:pge=\"http://pge.agesic.gub.uy/MessageType/\">"
						+"  <soapenv:Header>"
						+"      <pge:MessageType>TEST</pge:MessageType>"
						+"   </soapenv:Header>"
						+"   <soapenv:Body soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
						+"      <p675:procesar xmlns:p675=\"http://tempuri.org/\">"
						+"         <insRuc xsi:type=\"java:InscripcionRuc\" xmlns:java=\"java:pe.gob.sunat.servicio.registro.inscripcion.extranet.bean\">"
						+"            <CUO xsi:type=\"xsd:string\">3001101402</CUO>"
						+"            <codigoNotaria xsi:type=\"xsd:string\">10001</codigoNotaria>"
						+"            <fechaConstitucion xsi:type=\"xsd:string\">05/05/2011</fechaConstitucion>"
						+"            <oficinaRegistral xsi:type=\"xsd:string\">01</oficinaRegistral>"
						+"            <partidaRegistral xsi:type=\"xsd:string\">2117030522</partidaRegistral>"
						+"            <razonSocial xsi:type=\"xsd:string\">LETRUFFE AGENCY 12 S.A</razonSocial>"
						+"            <regpre xsi:type=\"java:ArrayOfRepresentanteLegal\" soapenc:arrayType=\"java:RepresentanteLegal[1]\">"
                        +"               <multiRef soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"p973:RepresentanteLegal\" xmlns:p973=\"java:pe.gob.sunat.servicio.registro.inscripcion.extranet.bean\">"
                       	+"                  <p973:CUO xsi:type=\"xsd:string\">3001101402</p973:CUO>"
                       	+"                  <p973:codigoCargo xsi:type=\"xsd:string\">001</p973:codigoCargo>"
                       	+"                  <p973:direccion xsi:type=\"xsd:string\">Los gladiolos 400</p973:direccion>"
                       	+"                  <p973:fechaDesdeOcupacionCargo xsi:type=\"xsd:string\">05/05/2011</p973:fechaDesdeOcupacionCargo>"
                       	+"                  <p973:fechaNacimiento xsi:type=\"xsd:string\">25/01/1950</p973:fechaNacimiento>"
                       	+"                  <p973:indicadorRRLLParticipante xsi:type=\"xsd:string\">1</p973:indicadorRRLLParticipante>"
                       	+"                  <p973:nombreRepresentante xsi:type=\"xsd:string\">ARAUJO BABILONIA TERESA DE JESUS</p973:nombreRepresentante>"
                       	+"                  <p973:numeroDocumentoIdentidad xsi:type=\"xsd:string\">00000661</p973:numeroDocumentoIdentidad>"
                       	+"                  <p973:porcentaje xsi:type=\"xsd:string\">15.50</p973:porcentaje>"
                       	+"                  <p973:tipoDeParticipante xsi:type=\"xsd:string\">01</p973:tipoDeParticipante>"
                       	+"                  <p973:tipoDocumentoIdentidad xsi:type=\"xsd:string\">09</p973:tipoDocumentoIdentidad>"
                       	+"               </multiRef>"
                       	+"            </regpre>"
                       	+"            <sigla xsi:type=\"xsd:string\">SIGLAC</sigla>"
                       	+"            <tipoEmpresa xsi:type=\"xsd:string\">015</tipoEmpresa>"
                       	+"            <tipoSociedad xsi:type=\"xsd:string\"/>"
                       	+"            <zonaRegistral xsi:type=\"xsd:string\">01</zonaRegistral>"
                       	+"         </insRuc>"
                       	+"      </p675:procesar>"
                       	+"   </soapenv:Body>"
                       	+"</soapenv:Envelope>";
		Message message = new MessageImpl();
		message.getBody().add(payload3);
		try {
			List<Object> elements = xmlDogUtil.executeMultipleXPath(message, xpaths, namespaces);
			if (elements != null && elements.size() > 0) {
				System.out.println("**************************");
				System.out.println("YES");
				System.out.println("**************************");
				return;
			}
			System.out.println("No");
		} catch (SAXPathException e) {
			e.printStackTrace();
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
