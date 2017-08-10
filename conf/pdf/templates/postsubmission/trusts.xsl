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
                <fo:block page-break-inside="avoid">
                    <xsl:for-each select="trusts/trustAssets">
                        <fo:table>
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                    <xsl:with-param name="value" select="scala:getMessagesText($translator, 'iht.estateReport.assets.heldInATrust.title')"/>
                                </xsl:call-template>
                                <xsl:call-template name="table-row--currency">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'iht.value')"/>
                                    <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                </xsl:call-template>

                            </fo:table-body>
                        </fo:table>
                    </xsl:for-each>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>