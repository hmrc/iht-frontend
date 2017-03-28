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
    <xsl:param name="debtsTotal"/>

    <xsl:template name="debts">
        <xsl:param name="value"/>

        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != '' or freeEstate/estateLiabilities != ''">
                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="1.5cm" space-after="0.5cm">
                    <xsl:value-of
                            select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
                </fo:block>
            </xsl:when>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != ''">
                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal">
                        <xsl:for-each select="freeEstate/estateAssets">
                            <xsl:if test="assetCode='0016'">
                            <fo:table>
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:choose>
                                    <xsl:when test="./liabilities != ''">
                                        <xsl:call-template name="table-row-tall">
                                            <xsl:with-param name="label"
                                                            select="i18n:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                            <xsl:with-param name="value" select="./liabilities/liabilityType"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="table-row-money-tall">
                                            <xsl:with-param name="label"
                                                            select="i18n:getMessagesText($translator, 'iht.value')"/>
                                            <xsl:with-param name="value" select='format-number(number(./liabilities/liabilityAmount), "##,###.00")'/>
                                        </xsl:call-template>
                                    </xsl:when>
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

                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal">
                    <fo:block>
                       <xsl:for-each select="freeEstate/estateLiabilities">
                            <xsl:if test="liabilityType='Funeral Expenses'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-tall-border-top-black-thin">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                        <xsl:with-param name="value" select="liabilityType"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                    </xsl:call-template>

                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                            </xsl:if>
                        </xsl:for-each>

                        <xsl:for-each select="freeEstate/estateLiabilities">
                            <xsl:if test="liabilityType! ='Funeral Expenses'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-tall-border-top-black-thin">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'pdf.liabilityType.text')"/>
                                        <xsl:with-param name="value" select="liabilityType"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="i18n:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(liabilityAmount), "##,###.00")'/>
                                    </xsl:call-template>

                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                            </xsl:if>
                        </xsl:for-each>

                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">

                                <xsl:call-template name="table-row-money-short-vpad-no-border">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'iht.valueOfDebts')"/>
                                    <xsl:with-param name="value" select='format-number(number($debtsTotal), "##,###.00")'/>
                                </xsl:call-template>

                                <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
