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

            <fo:table>
                <fo:table-column column-number="1" column-width="60%"/>
                <fo:table-column column-number="2" column-width="40%"/>
                <fo:table-body>
                    <xsl:choose>
                        <xsl:when test="freeEstate/estateAssets != ''">
                            <xsl:for-each select="freeEstate/estateAssets">
                                <xsl:if test="assetCode='0016'">
                                    <xsl:choose>
                                        <xsl:when test="./liabilities != ''">
                                            <xsl:call-template name="table-row--currency-right">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesText($translator, 'iht.estateReport.debts.mortgages')"/>
                                                <xsl:with-param name="value" select='format-number(number(./liabilities/liabilityAmount), "##,###.00")'/>
                                            </xsl:call-template>
                                        </xsl:when>

                                    </xsl:choose>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>

                        <xsl:when test="freeEstate/estateLiabilities != ''">
                           <xsl:for-each select="freeEstate/estateLiabilities">

                               <xsl:if test="liabilityType = 'Funeral Expenses'">
                                   <xsl:call-template name="table-row--currency-right">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesText($translator, 'iht.estateReport.debts.funeralExpenses.title')"/>
                                            <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                        </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>

                            <xsl:for-each select="freeEstate/estateLiabilities">
                                <xsl:if test="liabilityType !='Funeral Expenses'">
                                    <xsl:call-template name="table-row--currency-right">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.common.other')"/>
                                        <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>

                </xsl:choose>
                    <xsl:call-template name="table-row--currency-right-total">
                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'page.iht.application.debts.overview.total')"/>
                        <xsl:with-param name="value" select="format-number(number($debtsTotal), '##,###.00')"/>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
