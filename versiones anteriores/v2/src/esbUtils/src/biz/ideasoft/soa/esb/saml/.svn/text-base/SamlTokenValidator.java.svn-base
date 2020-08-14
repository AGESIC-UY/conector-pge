package biz.ideasoft.soa.esb.saml;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

public class SamlTokenValidator {

	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	
	private static Logger log = Logger.getLogger(SamlTokenValidator.class);
	
	public SamlTokenValidator() {
		
		nsContext.declarePrefix("saml", "urn:oasis:names:tc:SAML:1.0:assertion");
		
	}
	
	public boolean checkTokenValidity(CachedSamlToken token) {
		
		Date actualTime = new Date();
		
		Date notBefore = token.getNotBefore();
		if (actualTime.before(notBefore)) {
			log.info("---------------CheckTokenValidity - NotBefore attribute violated---------------");
			return false; // token expired
		}
		
		Date notOnOrAfter = token.getNotOnOrAfter();
		if (actualTime.after(notOnOrAfter)) {
			log.info("---------------CheckTokenValidity - NotOnOrAfter attribute violated---------------");
			return false; // token expired
		}
		
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public CachedSamlToken createCachedToken(String tokenXml) {
		
		try {
			
			CachedSamlToken cachedToken = new CachedSamlToken();
			cachedToken.setToken(tokenXml);
			
			dog = new XMLDog(nsContext, null, null);
			
			Expression notBeforeExpr = dog.addXPath("saml:Assertion/saml:Conditions/@NotBefore");
			Expression NotOnOrAfterExpr = dog.addXPath("saml:Assertion/saml:Conditions/@NotOnOrAfter");
			
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			
			InputSource source = new InputSource(new ByteArrayInputStream(tokenXml.getBytes()));
			XPathResults results = dog.sniff(source);
			Object queryResult = results.getResult(notBeforeExpr);
			if (queryResult != null) {
				
				List<NodeItem> attrsList = (List<NodeItem>) queryResult;
				if (attrsList.size() > 0) {
					
					String stringDate = (attrsList.get(0).value).replaceAll("Z$", "+0000");
					Date notBefore = dateFormatGmt.parse(stringDate);
					cachedToken.setNotBefore(notBefore);
					
				} else {
					log.error("Could not validate SamlToken");
					return null;
				}
			}
			
			queryResult = results.getResult(NotOnOrAfterExpr);
			if (queryResult != null) {
				
				List<NodeItem> attrsList = (List<NodeItem>) queryResult;
				if (attrsList.size() > 0) {
					
					String stringDate = (attrsList.get(0).value).replaceAll("Z$", "+0000");
					Date notOnOrAfter = dateFormatGmt.parse(stringDate);
					cachedToken.setNotOnOrAfter(notOnOrAfter);
					
				} else {
					log.error("Could not validate SamlToken");
					return null;
				}
			}
			
			return cachedToken;
			
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			return null;
		}
		
	}

}
