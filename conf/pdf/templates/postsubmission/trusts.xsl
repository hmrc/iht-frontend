<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge">
    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="trusts">

        <xsl:choose>
            <xsl:when test="trusts/trustAssets != ''">

                    <xsl:for-each select="trusts/trustAssets">
                        <xsl:call-template name="table-row--currency-right">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.estateReport.assets.heldInATrust.title')"/>
                            <xsl:with-param name="value" select="assetTotalValue"/>
                        </xsl:call-template>
                    </xsl:for-each>

            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>