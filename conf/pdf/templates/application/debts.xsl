<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-debts">
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" page-break-before="always">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
        </fo:block>
        <xsl:comment>Debts Mortgages section starts</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.mortgages')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/mortgages/mortgageList/isOwned='true'">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:for-each select="allLiabilities/mortgages/mortgageList">
                                    <xsl:variable name="property-id" select="id"/>
                                    <xsl:variable name="mortgage-value" select="value"/>
                                    <xsl:choose>
                                        <xsl:when test="isOwned='true'">
                                            <xsl:for-each select="../../../propertyList">
                                                <xsl:if test="id = $property-id">
                                                    <!--<xsl:value-of select="address" />-->
                                                    <xsl:call-template name="table-row-mortgage-money">
                                                        <xsl:with-param name="address" select="address"/>
                                                        <xsl:with-param name="value" select="$mortgage-value"/>
                                                    </xsl:call-template>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:for-each select="../../../propertyList">
                                                <xsl:if test="id = $property-id">
                                                    <xsl:call-template name="table-row-mortgage">
                                                        <xsl:with-param name="address" select="address"/>
                                                        <xsl:with-param name="label">
                                                            <xsl:value-of select="i18n:getMessagesText($translator, 'site.noMortgage')"/>
                                                        </xsl:with-param>
                                                    </xsl:call-template>
                                                </xsl:if>
                                            </xsl:for-each>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Debts Funeral Expenses section starts</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.funeralExpenses.title')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/funeralExpenses != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.debts.funeralExpenses.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/funeralExpenses/isOwned='false'">
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/funeralExpenses/isOwned='true'">
                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'iht.estateReport.debts.valueOfFuneralCosts')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allLiabilities/funeralExpenses/value">
                                                <xsl:value-of select='allLiabilities/funeralExpenses/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Debts owed from a trust</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.debtsTrust.title')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/trust != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.debts.debtsTrust.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/trust/isOwned='false'">
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/trust/isOwned='true'">
                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'iht.estateReport.debts.debtsTrust.value')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allLiabilities/trust/value">
                                                <xsl:value-of select='allLiabilities/trust/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Debts owed to anyone outside of the UK</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedOutsideUK')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/debtsOutsideUk != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.debts.debtsOutsideUk.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/debtsOutsideUk/isOwned='false'">
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/debtsOutsideUk/isOwned='true'">
                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedOutsideUK.value')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allLiabilities/debtsOutsideUk/value">
                                                <xsl:value-of select='allLiabilities/debtsOutsideUk/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Debts owed on any jointly owned assets</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedOnJointAssets')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/jointlyOwned != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.debts.jointlyOwned.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/jointlyOwned/isOwned='false'">
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/jointlyOwned/isOwned='true'">
                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'iht.estateReport.debts.owedOnJointAssets.value')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allLiabilities/jointlyOwned/value">
                                                <xsl:value-of select='allLiabilities/jointlyOwned/value'/></xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Any other debts not listed</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.debts.other.title')"/>
            <xsl:choose>
                <xsl:when test="allLiabilities/other != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.debts.other.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/other/isOwned='false'">
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/other/isOwned='true'">
                                    <xsl:call-template name="table-row-money">
                                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'page.iht.application.debts.other.inputLabel1')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allLiabilities/other/value">
                                                <xsl:value-of select='allLiabilities/other/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Assets Total section starts</xsl:comment>
        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="i18n:getMessagesText($translator, 'page.iht.application.debts.overview.total')"/>
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body font-size="12pt">

                    <xsl:call-template name="table-row-money-border-top-black">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesText($translator, 'pdf.total.text')"/>
                        <xsl:with-param name="value" select='$debtsTotal'/>
                    </xsl:call-template>

                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                    <xsl:call-template name="table-row-application-bottom-blank"/>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
