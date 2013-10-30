import org.jboss.soa.esb.message.*

def path = message.getProperties().getProperty("Path");
message.getBody().add("Path", path);
message.getBody().add("connectorType", false);
def stsURL = message.getProperties().getProperty("stsURL.test");
if (stsURL != null) {
	message.getProperties().setProperty("stsURL", stsURL);
} else {
//	message.getProperties().setProperty("stsURL", "https://testservicios.pge.red.uy:6051/TrustServer/SecurityTokenServiceProtected");
	message.getProperties().setProperty("stsURL", "https://10.255.10.51:6051/TrustServer/SecurityTokenServiceProtected");
}

