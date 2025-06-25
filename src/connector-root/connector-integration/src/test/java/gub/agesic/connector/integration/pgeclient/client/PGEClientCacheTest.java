package gub.agesic.connector.integration.pgeclient.client;

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
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.client.PGEClient;
import uy.gub.agesic.pge.client.PGEClientCache;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfig;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PGEClientCacheTest {

    @Mock
    private PGEClient mockPGEClient;

    @Test
    public void cacheDisabled() throws RequestSecurityTokenException, ConfigurationException, uy.gub.agesic.pge.exceptions.ConfigurationException {
        final PGEClient client = new PGEClientCache(mockPGEClient);
        final PGEConfiguration configuration = loadConfiguration(false);

        final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
        when(mockPGEClient.requestSecurityToken(any())).thenReturn(new STSResponse(1, token));

        client.requestSecurityToken(configuration);
        final STSResponse response = client.requestSecurityToken(configuration);

        assertThat(token, equalTo(response.getAssertion()));
        assertThat(response.getResponseTime(), equalTo(1L));
        verify(mockPGEClient, times(2)).requestSecurityToken(any());
    }

    @Test
    public void noTokenOnCache() throws ConfigurationException, RequestSecurityTokenException, uy.gub.agesic.pge.exceptions.ConfigurationException {
        final PGEClient client = new PGEClientCache(mockPGEClient);
        final PGEConfiguration configuration = loadConfiguration(true);

        final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
        when(mockPGEClient.requestSecurityToken(any())).thenReturn(new STSResponse(1, token));

        final STSResponse response = client.requestSecurityToken(configuration);

        assertThat(token, equalTo(response.getAssertion()));
        assertThat(response.getResponseTime(), equalTo(1L));
    }

    @Test
    public void expiredTokenOnCache() throws ConfigurationException, RequestSecurityTokenException, uy.gub.agesic.pge.exceptions.ConfigurationException {
        final PGEClient client = new PGEClientCache(mockPGEClient);
        final PGEConfiguration configuration = loadConfiguration(true);

        final DateTime oldDate = new DateTime(2011, 12, 11, 11, 11);
        final SAMLAssertion oldToken = generateSAML(oldDate, oldDate);
        when(mockPGEClient.requestSecurityToken(any())).thenReturn(new STSResponse(2, oldToken));
        final SAMLAssertion newToken = generateSAML(new DateTime(), new DateTime());
        when(mockPGEClient.requestSecurityToken(any())).thenReturn(new STSResponse(1, newToken));

        client.requestSecurityToken(configuration);
        final STSResponse response = client.requestSecurityToken(configuration);

        assertThat(newToken, equalTo(response.getAssertion()));
    }

    @Test
    public void validTokenOnCache() throws ConfigurationException, RequestSecurityTokenException, uy.gub.agesic.pge.exceptions.ConfigurationException {
        final PGEClient client = new PGEClientCache(mockPGEClient);
        final PGEConfiguration configuration = loadConfiguration(true);

        final SAMLAssertion token = generateSAML(new DateTime(), new DateTime());
        when(mockPGEClient.requestSecurityToken(any())).thenReturn(new STSResponse(0, token));

        client.requestSecurityToken(configuration);
        final STSResponse response = client.requestSecurityToken(configuration);

        assertThat(token, equalTo(response.getAssertion()));
        assertThat(response.getResponseTime(), equalTo(0L));
        verify(mockPGEClient, times(1)).requestSecurityToken(any());
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
        samlAssertion.setAssertionSaml1(assertion);

        return samlAssertion;
    }

    private PGEConfiguration loadConfiguration(boolean isCacheEnabled) {
        final PGEConfig.STSConfig stsConfig = new PGEConfig.STSConfig();
        List<PGEConfig.STSConfig.Property> stsProperties = new ArrayList<>();
        PGEConfig.STSConfig.Property stsProperty;
        // WSA To
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.WSA_TO);
        stsProperty.setValue("some wsaTo");
        stsProperties.add(stsProperty);
        // SAML version
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.POLICY);
        stsProperty.setValue("");
        stsProperties.add(stsProperty);
        // Cache token enabled
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.CACHE_TOKEN_ENABLED);
        stsProperty.setValue(isCacheEnabled ? "1" : "0");
        stsProperties.add(stsProperty);

        stsConfig.setProperty(stsProperties);

        final PGEConfig pgeConfig = new PGEConfig();
        pgeConfig.setSTSConfig(stsConfig);

        return new PGEConfiguration(pgeConfig);
    }
}
