<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="exemptionsTotal"/>
    <xsl:param name="exemptionTotalsSpouse"/>
    <xsl:param name="exemptionTotalsCharity"/>
    <xsl:param name="exemptionTotalsGNCP"/>

    <xsl:template name="exemptions">
        <fo:block>
        <xsl:choose>
            <xsl:when test="freeEstate/estateExemptions != ''">

                <fo:block page-break-inside="avoid">
                    <fo:block role="H2" xsl:use-attribute-sets="h2">
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
                    </fo:block>
                    <fo:table>
                        <fo:table-column column-number="1" column-width="60%"/>
                        <fo:table-column column-number="2" column-width="40%"/>
                        <fo:table-body>
                            <xsl:call-template name="table-row--currency-right">
                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.partner.assetsLeftToSpouse.title')"/>
                                <xsl:with-param name="value" select="$exemptionTotalsSpouse"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row--currency-right">
                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.charities.assetsLeftToCharities.title')"/>
                                <xsl:with-param name="value" select="$exemptionTotalsCharity"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row--currency-right">
                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title')"/>
                                <xsl:with-param name="value" select="$exemptionTotalsGNCP"/>
                            </xsl:call-template>

                            <xsl:call-template name="table-row--currency-right-total">
                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'pdf.totalexemptions.text')"/>
                                <xsl:with-param name="value" select="$exemptionsTotal"/>
                            </xsl:call-template>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
            </xsl:when>
        </xsl:choose>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
