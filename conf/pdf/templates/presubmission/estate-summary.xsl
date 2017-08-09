<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="estateOverviewDisplayMode"/>

    <xsl:template name="estate-summary">
        <fo:block  role="H2" xsl:use-attribute-sets="h2" page-break-before="always">
            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.overview.title2', $deceasedName)"/>
        </fo:block>
        <fo:block>
            <fo:table>
                <fo:table-column column-number="1" />
                <fo:table-column column-number="2" />
                <fo:table-body>
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
                    <xsl:if test="$estateOverviewDisplayMode=0">
                        <xsl:call-template name="table-row--currency-right">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
                            <xsl:with-param name="value" select="$debtsTotal"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="$estateOverviewDisplayMode=0">
                        <xsl:call-template name="table-row--currency-right">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
                            <xsl:with-param name="value" select="$exemptionsTotal"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="$estateOverviewDisplayMode=2">
                        <xsl:call-template name="table-row--currency-right-total-shaded">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'page.iht.application.estateOverview.valueOfAssetsAndGifts')"/>
                            <xsl:with-param name="value" select="$estateValue"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row--novalue">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'page.iht.application.tnrbEligibilty.increasedTnrbThreshold.title')"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row--currency-right">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.estateReport.ihtThreshold')"/>
                            <xsl:with-param name="value" select="$thresholdValue"/>
                        </xsl:call-template>
                    </xsl:if>
                </fo:table-body>
            </fo:table>
        </fo:block>
        <fo:block  role="H2" xsl:use-attribute-sets="h3">
            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.estateOverview.otherDetailsNeeded')"/>
        </fo:block>
        <fo:block>
            <fo:table>
            <fo:table-column column-number="1" />
            <fo:table-column column-number="2" />
            <fo:table-body>
                <xsl:if test="$estateOverviewDisplayMode=2">
                    <xsl:call-template name="table-row--currency-right">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
                        <xsl:with-param name="value" select="$debtsTotal"/>
                    </xsl:call-template>
                </xsl:if>
            </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>
