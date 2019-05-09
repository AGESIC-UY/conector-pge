package gub.agesic.connector.integration.pgeclient.client;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.integration.pgeclient.beans.SAMLAssertion;
import gub.agesic.connector.integration.pgeclient.beans.STSResponse;
import gub.agesic.connector.integration.pgeclient.exceptions.RequestSecurityTokenException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PGEClientCacheTest {

	@Mock
	private PGEClient mockPGEClient;

	@Test
	public void cacheDisabled() throws ConfigurationException, RequestSecurityTokenException {
		final PGEClient client = new PGEClientCache(mockPGEClient);
		final Configuration configuration = null;
		final Connector connector = new Connector();
		final String policyName = "";
		connector.setEnableCacheTokens(false);
		connector.setWsaTo("some wsaTo");

		final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
		when(mockPGEClient.requestSecurityToken(any(), any(), any())).thenReturn(new STSResponse(1, token));

		client.requestSecurityToken(configuration, connector, policyName);
		final STSResponse response = client.requestSecurityToken(configuration, connector, policyName);

		assertThat(token, equalTo(response.getAssertion()));
		assertThat(response.getResponseTime(), equalTo(1L));
		verify(mockPGEClient, times(2)).requestSecurityToken(any(), any(), any());

	}

	@Test
	public void noTokenOnCache() throws ConfigurationException, RequestSecurityTokenException {
		final PGEClient client = new PGEClientCache(mockPGEClient);
		final Configuration configuration = null;
		final Connector connector = new Connector();
		final String policyName = "";
		connector.setEnableCacheTokens(true);
		connector.setWsaTo("some wsaTo");

		final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
		when(mockPGEClient.requestSecurityToken(any(), any(), any())).thenReturn(new STSResponse(1, token));

		final STSResponse response = client.requestSecurityToken(configuration, connector, policyName);

		assertThat(token, equalTo(response.getAssertion()));
		assertThat(response.getResponseTime(), equalTo(1L));
	}

	@Test
	public void expiredTokenOnCache() throws ConfigurationException, RequestSecurityTokenException {
		final PGEClient client = new PGEClientCache(mockPGEClient);
		final Configuration configuration = null;
		final Connector connector = new Connector();
		final String policyName = "";
		connector.setEnableCacheTokens(true);
		connector.setWsaTo("some wsaTo");

		final DateTime oldDate = new DateTime(2011, 12, 11, 11, 11);
		final SAMLAssertion oldToken = generateSAML(oldDate, oldDate);
		when(mockPGEClient.requestSecurityToken(any(), any(), any())).thenReturn(new STSResponse(2, oldToken));
		final SAMLAssertion newToken = generateSAML(new DateTime(), new DateTime());
		when(mockPGEClient.requestSecurityToken(any(), any(), any())).thenReturn(new STSResponse(1, newToken));

		client.requestSecurityToken(configuration, connector, policyName);
		final STSResponse response = client.requestSecurityToken(configuration, connector, policyName);

		assertThat(newToken, equalTo(response.getAssertion()));
	}

	@Test
	public void validTokenOnCache() throws ConfigurationException, RequestSecurityTokenException {
		final PGEClient client = new PGEClientCache(mockPGEClient);
		final Configuration configuration = null;
		final Connector connector = new Connector();
		final String policyName = "";
		connector.setEnableCacheTokens(true);
		connector.setWsaTo("some wsaTo");

		final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
		when(mockPGEClient.requestSecurityToken(any(), any(), any())).thenReturn(new STSResponse(0, token));

		client.requestSecurityToken(configuration, connector, policyName);
		final STSResponse response = client.requestSecurityToken(configuration, connector, policyName);

		assertThat(token, equalTo(response.getAssertion()));
		assertThat(response.getResponseTime(), equalTo(0L));
		verify(mockPGEClient, times(1)).requestSecurityToken(any(), any(), policyName);
	}

	private SAMLAssertion generateSAML(final DateTime conditionTimeNotBefore, final DateTime conditionTimeNotAfter)
			throws ConfigurationException {
		DefaultBootstrap.bootstrap();
		final XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();

		final SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) builderFactory
				.getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
		final Conditions conditions = (Conditions) conditionsBuilder.buildObject();

		conditions.setNotBefore(conditionTimeNotBefore);
		conditions.setNotOnOrAfter(conditionTimeNotAfter);

		final SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		final Assertion assertion = (Assertion) assertionBuilder.buildObject();
		assertion.setConditions(conditions);
		final SAMLAssertion samlAssertion = new SAMLAssertion();
		samlAssertion.setAssertion(assertion);

		return samlAssertion;

	}

}
