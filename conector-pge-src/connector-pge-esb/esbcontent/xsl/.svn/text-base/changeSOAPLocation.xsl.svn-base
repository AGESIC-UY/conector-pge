<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/">

	<xsl:output omit-xml-declaration="yes" method="xml" encoding="UTF-8"/>
	
	<xsl:param name="location"/>
		
	<!-- Realiza la transformacion identidad -->
	<xsl:template match="/ | @* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Setea el valor del atributo mustUnderstand a 0 al elemento de security del Header -->
	<xsl:template match="soap:address/@location">
		<xsl:attribute name="location"><xsl:value-of select="$location"/></xsl:attribute>
	</xsl:template>

</xsl:stylesheet>
