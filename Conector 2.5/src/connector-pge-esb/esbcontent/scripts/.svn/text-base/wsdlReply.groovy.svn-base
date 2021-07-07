import org.jboss.soa.esb.message.*
import uy.gub.agesic.connector.*
import uy.gub.agesic.connector.session.api.*;
import uy.gub.agesic.connector.exceptions.ConnectorException;
import biz.ideasoft.soa.esb.util.SoapUtil;
import org.jboss.internal.soa.esb.util.StreamUtils;

def location = "http://" + message.getProperties().getProperty("host") + message.getProperties().getProperty("Path"); 
message.getProperties().setProperty("location", location);
message.getProperties().setProperty("Content-Type", new ResponseHeader("Content-Type", "application/xml;charset=UTF-8"));

def soapFault = false;
def fullConnector = message.getBody().remove("fullConnector");
if (fullConnector != null) {
	def connectorConfig = fullConnector.getConnector();
	if (connectorConfig != null) {
		def wsaTo = connectorConfig.getWsaTo();
		def connectorPaths = fullConnector.getConnectorPaths();
		def wsdlPath = connectorPaths.getWsdl();		
		if (wsdlPath != null) {		
			def wsdl = null;
			def wsdlByte = null;
			try {
				def is = new FileInputStream(new File(wsdlPath));
				//wsdl = StreamUtils.readStreamString(is, "UTF-8");
				wsdlByte = StreamUtils.readStream(is);
			} catch (Exception e) {
				throw new ConnectorException(e);	
			}
			
			message.getBody().add(wsdlByte);
		} else {
			soapFault = true;
		}
	} else {
		soapFault = true;
	}
}
message.getProperties().setProperty("soapFault", new Boolean(soapFault));

