<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>


    <xsl:template name="estate-summary">
        <xsl:param name="value"/>
        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" page-break-before="always">
            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.overview.title2', $deceasedName)"/>
        </fo:block>
        <fo:block>
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" />
                <fo:table-column column-number="2" />
                <fo:table-body font-size="12pt">
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'iht.estateReport.assets.inEstate')"/>
                        <xsl:with-param name="value" select="$assetsTotal"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'iht.estateReport.gifts.givenAway.title')"/>
                        <xsl:with-param name="value" select="$giftsTotal"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
                        <xsl:with-param name="value" select="$debtsTotal"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
                        <xsl:with-param name="value" select="$exemptionsTotal"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'page.iht.application.overview.value')"/>
                        <xsl:with-param name="value" select="$estateValue"/>
                    </xsl:call-template>

                </fo:table-body>
            </fo:table>
        </fo:block>
        <fo:block>
            <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                <xsl:value-of select="scala:getMessagesText($translator, 'pdf.iht.estateReport.ihtThreshold')"/>
                &#xA3;<xsl:value-of select='format-number(number($thresholdValue), "##,###.00")'/>
            </fo:block>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>
