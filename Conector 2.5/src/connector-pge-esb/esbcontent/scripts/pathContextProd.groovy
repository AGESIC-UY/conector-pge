import org.jboss.soa.esb.message.*

def path = message.getProperties().getProperty("Path");
message.getBody().add("Path", path);
message.getBody().add("connectorType", true);
def stsURL = message.getProperties().getProperty("stsURL.production");
if (stsURL != null) {
	message.getProperties().setProperty("stsURL", stsURL);
} else {
	message.getProperties().setProperty("stsURL", "https://10.255.10.64:10001/TrustServer/SecurityTokenServiceProtected");
	//tirar exception	
}
