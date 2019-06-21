<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
xmlns:a="http://www.w3.org/2005/08/addressing"
xmlns:e='http://schemas.xmlsoap.org/soap/envelope/'>

	<xsl:output omit-xml-declaration="yes" method="xml" encoding="UTF-8"/>
	
	<xsl:param name="messageID"/>
	<xsl:param name="action"/>	
	
	<xsl:preserve-space elements="*"/>

	<!-- Realiza la transformacion identidad -->
	<xsl:template match="/ | @* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="e:Header">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
			<a:MessageID>uuid:<xsl:value-of select="$messageID"/></a:MessageID>
			<a:Action><xsl:value-of select="$action"/></a:Action>
		</xsl:copy>
	</xsl:template>
	
		
</xsl:stylesheet>
    
