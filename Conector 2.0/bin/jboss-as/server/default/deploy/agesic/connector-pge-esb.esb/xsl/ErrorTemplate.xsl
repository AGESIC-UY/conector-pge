<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsa="http://www.w3.org/2005/08/addressing"
	version="1.0">

	<xsl:output omit-xml-declaration="yes" method="xml"
		encoding="UTF-8" indent="yes" />

	<xsl:param name="faultString" />

	<xsl:param name="intermediate" />

	<xsl:param name="relatesTo" />

	<xsl:param name="messageId" />

	<xsl:template match="/">
		<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
			xmlns:wsa="http://www.w3.org/2005/08/addressing">
			<env:Header>
				<wsa:Action />
				<wsa:MessageID>
					<xsl:value-of select="$messageId" />
				</wsa:MessageID>
				<xsl:if test="string-length($relatesTo) > 0">
					<xsl:call-template name="output-tokens">
						<xsl:with-param name="list">
							<xsl:value-of select="$relatesTo" />
						</xsl:with-param>
						<xsl:with-param name="delimiter">
							<xsl:value-of select="','" />
						</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
			</env:Header>
			<env:Body>
				<env:Fault>
					<faultcode>env:Server</faultcode>
					<faultstring><xsl:value-of select="$faultString" /></faultstring>
					<faultactor>
						<xsl:value-of select="$intermediate" />
					</faultactor>
				</env:Fault>
			</env:Body>
		</env:Envelope>
	</xsl:template>

	<!-- codigo que parsea la lista de relatesTo.. equivale a un split -->
	<xsl:template name="output-tokens">
		<xsl:param name="list" />
		<xsl:param name="delimiter" />
		<xsl:variable name="newlist">
			<xsl:choose>
				<xsl:when test="contains($list, $delimiter)">
					<xsl:value-of select="normalize-space($list)" />
				</xsl:when>

				<xsl:otherwise>
					<xsl:value-of select="concat(normalize-space($list), $delimiter)" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="first"
			select="substring-before($newlist, $delimiter)" />
		<xsl:variable name="remaining"
			select="substring-after($newlist, $delimiter)" />
		<wsa:RelatesTo>
			<xsl:value-of select="$first" />
		</wsa:RelatesTo>
		<xsl:if test="$remaining">
			<xsl:call-template name="output-tokens">
				<xsl:with-param name="list" select="$remaining" />
				<xsl:with-param name="delimiter">
					<xsl:value-of select="$delimiter" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>


</xsl:stylesheet>
