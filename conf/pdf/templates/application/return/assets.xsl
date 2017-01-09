<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="assetsTotal"/>
    <xsl:include href="pdf/templates/application/return/trusts.xsl"/>
    <xsl:template name="assets">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != ''">
                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" page-break-before="always">
                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.assets.inEstate')"/>
                </fo:block>
                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
                    <fo:block>
                        <xsl:for-each select="freeEstate/estateAssets">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-top">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'pdf.assetTotal.text')"/>
                                        <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>

                                    <xsl:choose>
                                        <xsl:when test="howheld != ''">
                                            <xsl:call-template name="table-row-application">
                                                <xsl:with-param name="label"
                                                                select="i18n:getMessagesText($translator, 'page.iht.application.assets.deceased-permanent-home.typeofownership.label')"/>
                                                <xsl:with-param name="value" select="howheld"/>
                                            </xsl:call-template>

                                        </xsl:when>
                                    </xsl:choose>

                                    <xsl:if test="assetCode='0016'">
                                        <xsl:choose>
                                            <xsl:when test="./liabilities != ''">
                                                <xsl:call-template name="table-row-application">
                                                    <xsl:with-param name="label"
                                                                    select="i18n:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                                    <xsl:with-param name="value" select="./liabilities/liabilityType"/>
                                                </xsl:call-template>

                                                <xsl:call-template name="table-row-money">
                                                    <xsl:with-param name="label"
                                                                    select="i18n:getMessagesText($translator, 'iht.estateReport.debts.mortgage.valueOfDeceasedsShare')"/>
                                                    <xsl:with-param name="value" select='format-number(number(./liabilities/liabilityAmount), "##,###.00")'/>
                                                </xsl:call-template>
                                            </xsl:when>
                                        </xsl:choose>

                                    </xsl:if>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-application-bottom-blank"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:for-each>

                        <xsl:comment>Trust section starts</xsl:comment>
                        <xsl:call-template name="trusts">
                            <xsl:with-param name="value" select="trusts"></xsl:with-param>
                        </xsl:call-template>
                        <xsl:comment>Trust section ends</xsl:comment>

                        <!-- Total Assets row-->
                        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="0.5cm">
                            <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.totalassets.text')"/>
                        </fo:block>

                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">

                                <xsl:call-template name="table-row-money-border-top-black">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'pdf.total.text')"/>
                                    <xsl:with-param name="value" select='format-number(number($assetsTotal), "##,###.00")'/>
                                </xsl:call-template>

                                <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                <xsl:call-template name="table-row-application-bottom-blank"/>
                            </fo:table-body>
                        </fo:table>

                    </fo:block>
                </fo:block>
            </xsl:when>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>