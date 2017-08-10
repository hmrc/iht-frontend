<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="debtsTotal"/>

    <xsl:template name="debts">
        <fo:block page-break-inside="avoid">
        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != '' or freeEstate/estateLiabilities != ''">
                <fo:block role="H2" xsl:use-attribute-sets="h2">
                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
                </fo:block>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != ''">
                <fo:block>
                        <xsl:for-each select="freeEstate/estateAssets">
                            <xsl:if test="assetCode='0016'">
                            <fo:table>
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body>
                                <xsl:choose>
                                    <xsl:when test="./liabilities != ''">
                                        <xsl:call-template name="table-row">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                            <xsl:with-param name="value" select="scala:getMessagesText($translator, 'iht.estateReport.debts.mortgages')"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="table-row--currency">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesText($translator, 'iht.value')"/>
                                            <xsl:with-param name="value" select='format-number(number(./liabilities/liabilityAmount), "##,###.00")'/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>

                                    </xsl:otherwise>
                                </xsl:choose>

                            </fo:table-body>
                            </fo:table>
                            </xsl:if>
                        </xsl:for-each>
                </fo:block>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="freeEstate/estateLiabilities != ''">
                <fo:block>
                    <fo:block>
                       <xsl:for-each select="freeEstate/estateLiabilities">
                            <xsl:if test="liabilityType='Funeral Expenses'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body>

                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                        <xsl:with-param name="value" select="scala:getMessagesText($translator, 'iht.estateReport.debts.funeralExpenses.title')"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                    </xsl:call-template>

                                </fo:table-body>
                            </fo:table>
                            </xsl:if>
                        </xsl:for-each>

                        <xsl:for-each select="freeEstate/estateLiabilities">
                            <xsl:if test="liabilityType! ='Funeral Expenses'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body>

                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                        <xsl:with-param name="value" select="scala:getMessagesText($translator, 'iht.common.other')"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                    </xsl:call-template>

                                </fo:table-body>
                            </fo:table>
                            </xsl:if>
                        </xsl:for-each>

                        <fo:block>
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body>
                                    <xsl:call-template name="table-row--currency-total">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'page.iht.application.debts.overview.total')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number($debtsTotal), "##,###.00")'/>
                                    </xsl:call-template>

                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </fo:block>
                </fo:block>
            </xsl:when>
        </xsl:choose>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
