package gub.agesic.connector.integration;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * Created by adriancur on 31/10/17.
 */
public class ServiceTest {
	private final Logger logger = Logger.getLogger(ServiceTest.class);

	@Before
	public void setup() {
		System.setProperty("javax.net.ssl.trustStore",
				"/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "conector");
	}

	@Test
	public void testPOST() {
		final RestTemplate restTemplate = new RestTemplate();
		final String fullUrl = "http://testing.hg.red.uy:9700/connector-runtime/service/timeStamp";
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		final HttpEntity<Object> request = new HttpEntity<Object>(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\">\n"
						+ "   <soapenv:Header/>\n" + "   <soapenv:Body>\n" + "      <soa:GetTimestamp/>\n"
						+ "   </soapenv:Body>\n" + "</soapenv:Envelope>", headers);

		logger.info("LLAMADA DEL SERVICIO CON ESTE REQUEST: " + request);
		System.out.println("LLAMADA DEL SERVICIO CON ESTE REQUEST: " + request);

		final ResponseEntity<?> httpResponse = restTemplate.exchange(fullUrl, HttpMethod.POST, request, String.class, "Request");

		Assert.assertEquals(httpResponse.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void testPOSTDNIC() {
		final RestTemplate restTemplate = new RestTemplate();
		final String fullUrl = "http://testing.hg.red.uy:9700/connector-runtime/service/dnic";
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		final HttpEntity<Object> request = new HttpEntity<Object>(
				"<soapenv:Envelope xmlns:wsd=\"http://wsDNIC/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
						+ "  <soapenv:Header/>\n" + "  <soapenv:Body>\n" + "    <wsd:ProductDesc/>\n" + "  </soapenv:Body>\n"
						+ "</soapenv:Envelope>", headers);

		final ResponseEntity<?> httpResponse = restTemplate.exchange(fullUrl, HttpMethod.POST, request, String.class, "Request");

		Assert.assertEquals(httpResponse.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void testPOSTACCESSL() {
		final RestTemplate restTemplate = new RestTemplate();
		final String fullUrl = "https://testing.hg.red.uy:8443/connector-runtime/service/acce";
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		final HttpEntity<Object> request = new HttpEntity<Object>(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://www.comprasestatales.gub.uy/ws/sice/compras.wsdl\">  <soapenv:Body>\n"
						+ "    <com:consultar>\n" + "      <idCompra>23433</idCompra>\n" + "    </com:consultar>\n"
						+ "  </soapenv:Body>\n" + "</soapenv:Envelope>", headers);

		final ResponseEntity<?> httpResponse = restTemplate.exchange(fullUrl, HttpMethod.POST, request, String.class, "Request");

		Assert.assertEquals(httpResponse.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void testPOSTACCE() {
		final RestTemplate restTemplate = new RestTemplate();
		final String fullUrl = "http://testing.hg.red.uy:9700/connector-runtime/service/acce";
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		final HttpEntity<Object> request = new HttpEntity<Object>(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://www.comprasestatales.gub.uy/ws/sice/compras.wsdl\">  <soapenv:Body>\n"
						+ "    <com:consultar>\n" + "      <idCompra>23433</idCompra>\n" + "    </com:consultar>\n"
						+ "  </soapenv:Body>\n" + "</soapenv:Envelope>", headers);

		final ResponseEntity<?> httpResponse = restTemplate.exchange(fullUrl, HttpMethod.POST, request, String.class, "Request");

		Assert.assertEquals(httpResponse.getStatusCode(), HttpStatus.OK);
	}
}