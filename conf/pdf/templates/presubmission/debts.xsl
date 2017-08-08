<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-debts">
        <fo:block page-break-before="always" xsl:use-attribute-sets="h2">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedFromEstate')"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="copy">
            <xsl:value-of select="scala:getMessagesTextWithParameters($translator, 'pdf.debts.summary.p1', $deceasedName, $deceasedName)"/>
        </fo:block>


        <xsl:comment>Debts Mortgages section starts</xsl:comment>
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.mortgages')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/mortgages/mortgageList/isOwned='true'">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
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
                                                            <xsl:value-of select="scala:getMessagesText($translator, 'site.noMortgage')"/>
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
                <xsl:otherwise>
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                            <xsl:call-template name="table-row--novalue">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'site.noneInEstate')"/>
                            </xsl:call-template>
                        </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block>


        <xsl:comment>Debts Funeral Expenses section starts</xsl:comment>
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.funeralExpenses.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/funeralExpenses != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'page.iht.application.debts.funeralExpenses.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/funeralExpenses/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/funeralExpenses/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.debts.valueOfFuneralCosts')" />
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
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.debtsTrust.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/trust != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.debts.debtsTrust.isOwned', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/trust/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/trust/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.debts.debtsTrust.value')" />
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
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedOutsideUK')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/debtsOutsideUk != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'page.iht.application.debts.debtsOutsideUk.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/debtsOutsideUk/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/debtsOutsideUk/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedOutsideUK.value')" />
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
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedOnJointAssets')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/jointlyOwned != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'page.iht.application.debts.jointlyOwned.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/jointlyOwned/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/jointlyOwned/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.debts.owedOnJointAssets.value')" />
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
        <fo:block page-break-inside="avoid">
            <fo:block xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.debts.other.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allLiabilities/other != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'page.iht.application.debts.other.isOwned')"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allLiabilities/other/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allLiabilities/other/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'page.iht.application.debts.other.inputLabel1')" />
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


        <xsl:comment>Debts Total section starts</xsl:comment>
        <fo:block page-break-inside="avoid">
            <fo:table>
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body>

                    <xsl:call-template name="table-row--currency-total">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'page.iht.application.debts.overview.total')"/>
                        <xsl:with-param name="value" select='$debtsTotal'/>
                    </xsl:call-template>

                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
