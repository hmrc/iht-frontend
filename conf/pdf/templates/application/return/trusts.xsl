<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
>
    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="trusts">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="trusts/trustAssets != ''">
                <fo:block>
                    <xsl:for-each select="trusts/trustAssets">
                        <fo:table>
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row-top">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                    <xsl:with-param name="value" select="i18n:getMessagesText($translator, 'pdf.postSubmission.trusts.assetDescription')"/>
                                </xsl:call-template>
                                <xsl:call-template name="table-row-money">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'pdf.assetTotal.text')"/>
                                    <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                </xsl:call-template>
                                <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                <xsl:call-template name="table-row-application-bottom-blank"/>
                            </fo:table-body>
                        </fo:table>
                    </xsl:for-each>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>