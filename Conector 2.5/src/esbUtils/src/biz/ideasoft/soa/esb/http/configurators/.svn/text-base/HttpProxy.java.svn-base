package biz.ideasoft.soa.esb.http.configurators;

import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.http.Configurator;

public class HttpProxy extends Configurator {

	public void configure(HttpClient httpClient, Properties properties)
			throws ConfigurationException {
		String host = properties.getProperty("proxyHost");
		String port = properties.getProperty("proxyPort");
		String user = properties.getProperty("proxyUser");
		String password = properties.getProperty("proxyPassword");

		if (host != null) { 
			int portNumber = port == null ? 3128 : Integer.parseInt(port);
			httpClient.getHostConfiguration().setProxy(host, portNumber);
			if (user != null) {
				httpClient.getState().setProxyCredentials(
						new AuthScope(host, portNumber, null),
						new UsernamePasswordCredentials(user, password));
			}
		}
	}
}
