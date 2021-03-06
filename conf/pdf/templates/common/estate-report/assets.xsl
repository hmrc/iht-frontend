<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="deceasedName"/>

    <xsl:template name="pre-assets">
        <fo:block  role="H2" xsl:use-attribute-sets="h2" page-break-before="always">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.inEstate')"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="copy copy--lede">
            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.subtitle', $deceasedName)"/>
        </fo:block>


        <xsl:comment>Assets Properties section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.propertiesBuildingsAndLand')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/properties != ''">
                    <xsl:choose>
                        <xsl:when test="allAssets/properties/isOwned='true'">
                            <fo:block>
                                <fo:table>
                                    <fo:table-column column-number="1" column-width="70%"/>
                                    <fo:table-column column-number="2" column-width="30%"/>
                                    <fo:table-body>
                                        <xsl:call-template name="table-row">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.properties.question.question', $deceasedName)"/>
                                            <xsl:with-param name="value" select="scala:getMessagesText($translator, 'iht.yes')"/>
                                        </xsl:call-template>
                                    </fo:table-body>
                                </fo:table>
                            </fo:block>
                            <fo:block>
                                <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.assets.deceased-permanent-home.table.header')"/>
                                <xsl:for-each select="./propertyList">
                                    <fo:table keep-together.within-column="1">
                                        <fo:table-column column-number="1" column-width="60%"/>
                                        <fo:table-column column-number="2" column-width="40%"/>
                                        <fo:table-body>
                                            <xsl:call-template name="table-row--address">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesText($translator, 'iht.address.upperCaseInitial')"/>
                                                <xsl:with-param name="value" select="address"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesText($translator, 'iht.estateReport.assets.properties.whatKind.question')"/>
                                                <xsl:with-param name="value" select="propertyType"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.howOwnedByDeceased', $deceasedName)"/>
                                                <xsl:with-param name="value" select="typeOfOwnership"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesText($translator, 'iht.estateReport.assets.properties.freeholdOrLeasehold')"/>
                                                <xsl:with-param name="value" select="tenure"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row--currency">
                                                <xsl:with-param name="label"
                                                                select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.properties.value.question', $deceasedName)"/>
                                                <xsl:with-param name="value">
                                                    <xsl:if test="value">
                                                        <xsl:value-of select='value'/>
                                                    </xsl:if>
                                                </xsl:with-param>
                                            </xsl:call-template>
                                        </fo:table-body>
                                    </fo:table>
                                </xsl:for-each>
                            </fo:block>
                        </xsl:when>
                        <xsl:otherwise>
                            <fo:block>
                                <xsl:value-of select="scala:getMessagesText($translator, 'site.noneInEstate')"/>
                            </fo:block>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
            </xsl:choose>
        </fo:block>

        <xsl:comment>Assets money section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.money.upperCaseInitial')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/money != ''">
                    <fo:block role="H4" xsl:use-attribute-sets="h4">
                        <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.moneyOwned', $deceasedName)"/>
                    </fo:block>
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.money.ownName.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/money/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/money/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'page.iht.application.assets.money.inputLabel1')"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/money/value">
                                                <xsl:value-of select='allAssets/money/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                    <fo:block role="H4" xsl:use-attribute-sets="h4">
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.money.jointlyOwned')"/>
                    </fo:block>
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.money.jointly.owned.question', $deceasedName)" />
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/money/isOwnedShare='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/money/isOwnedShare='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.money.jointly.owned.input.value.label', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/money/shareValue">
                                                <xsl:value-of select='allAssets/money/shareValue'/>
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

        <xsl:comment>Assets household section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.householdAndPersonalItems.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/household != ''">
                    <fo:block role="H4" xsl:use-attribute-sets="h4">
                        <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.householdAndPersonalItemsOwnedByDeceased.title', $deceasedName)"/>
                    </fo:block>
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.household.ownName.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/household/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/household/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.household.deceasedOwnedValue', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/household/value">
                                               <xsl:value-of select='allAssets/household/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                    <fo:block role="H4" xsl:use-attribute-sets="h4">
                        <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.householdAndPersonalItemsJointlyOwned.title', $deceasedName)"/>
                    </fo:block>
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.household.joint.question', $deceasedName)" />
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/household/isOwnedShare='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/household/isOwnedShare='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.household.valueOfJointlyOwned', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/household/shareValue">
                                                <xsl:value-of select='allAssets/household/shareValue'/>
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


        <xsl:comment>Assets vehicles section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <xsl:choose>
            <xsl:when test="allAssets/vehicles != ''">
                <fo:block  role="H3" xsl:use-attribute-sets="h3">
                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.vehicles')"/>
                </fo:block>
                <fo:block role="H4" xsl:use-attribute-sets="h4">
                    <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.vehiclesOwned', $deceasedName)"/>
                </fo:block>
                <fo:block>
                    <fo:table>
                        <fo:table-column column-number="1" column-width="70%"/>
                        <fo:table-column column-number="2" column-width="30%"/>
                        <fo:table-body>
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.vehicles.ownName.question', $deceasedName)"/>
                                <xsl:with-param name="value">
                                    <xsl:choose>
                                        <xsl:when test="allAssets/vehicles/isOwned='false'">
                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                            </xsl:call-template>
                            <xsl:if test="allAssets/vehicles/isOwned='true'">
                                <xsl:call-template name="table-row--currency">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.household.deceasedOwnedValue', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allAssets/vehicles/value">
                                            <xsl:value-of select='allAssets/vehicles/value'/>
                                        </xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
                <fo:block role="H4" xsl:use-attribute-sets="h4">
                    <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.vehicles.overview.joint.title',$deceasedName)"/>
                </fo:block>
                <fo:block>
                    <fo:table>
                        <fo:table-column column-number="1" column-width="70%"/>
                        <fo:table-column column-number="2" column-width="30%"/>
                        <fo:table-body>
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.vehicles.jointly.owned.question', $deceasedName)" />
                                <xsl:with-param name="value">
                                    <xsl:choose>
                                        <xsl:when test="allAssets/vehicles/isOwnedShare='false'">
                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                            </xsl:call-template>
                            <xsl:if test="allAssets/vehicles/isOwnedShare='true'">
                                <xsl:call-template name="table-row--currency">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.vehicles.valueOfJointlyOwned', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allAssets/vehicles/shareValue">
                                            <xsl:value-of select='allAssets/vehicles/shareValue'/>
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


        <xsl:comment>Assets private pensions section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.privatePensions')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/privatePension != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.pensions.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/privatePension/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/privatePension/isChanged">
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.pensions.changed.title', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:choose>
                                                <xsl:when test="allAssets/privatePension/isChanged='false'">
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of
                                                            select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="allAssets/privatePension/isChanged='false'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.estateReport.assets.pensions.valueOfRemainingPaymentsBeingPaid')"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/privatePension/value">
                                                <xsl:value-of select='allAssets/privatePension/value'/>
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


        <xsl:comment>Assets stocks and shares section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.stocksAndShares')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/stockAndShare != ''">
                    <fo:block>
                    <fo:block role="H4" xsl:use-attribute-sets="h4">
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.stocksAndSharesListed')"/>
                    </fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.stocksAndShares.listed.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/stockAndShare/isListed='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/stockAndShare/valueListed">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.assets.stocksAndShares.valueOfListed')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/stockAndShare/valueListed">
                                                <xsl:value-of select='allAssets/stockAndShare/valueListed'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <fo:block>
                        <fo:block role="H4" xsl:use-attribute-sets="h4">
                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.stocksAndSharesNotListed')"/>
                        </fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.stocksAndShares.notListed.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/stockAndShare/isNotListed='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/stockAndShare/valueNotListed">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.assets.stocksAndShares.valueOfNotListed')" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/stockAndShare/valueNotListed">
                                                <xsl:value-of select='allAssets/stockAndShare/valueNotListed'/>
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


        <xsl:comment>Assets insurance policies section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.insurancePolicies')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/insurancePolicy != ''">
                    <xsl:comment>Assets insurance policies paying out to the deceased</xsl:comment>
                    <fo:block>
                        <fo:block role="H4" xsl:use-attribute-sets="h4">
                            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.insurancePolicies.payingOutToDeceased', $deceasedName)"/>
                        </fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.insurancePolicies.ownName.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/insurancePolicy/policyInDeceasedName='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/insurancePolicy/value">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/insurancePolicy/value">
                                                <xsl:value-of select='allAssets/insurancePolicy/value'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:comment>Assets insurance policies that were jointly held with someone else</xsl:comment>
                    <fo:block>
                        <fo:block role="H4" xsl:use-attribute-sets="h4">
                            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.assets.insurance.policies.overview.joint.title')"/>
                        </fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.insurancePolicies.jointlyHeld.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/insurancePolicy/isJointlyOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/insurancePolicy/shareValue">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/insurancePolicy/shareValue">
                                                <xsl:value-of select='allAssets/insurancePolicy/shareValue'/>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:comment>Assets insurance premiums that wer paid by the deceased for someone</xsl:comment>
                    <fo:block>
                        <xsl:choose>
                        <xsl:when test="allAssets/insurancePolicy/isInsurancePremiumsPayedForSomeoneElse!=''">
                            <fo:block role="H4" xsl:use-attribute-sets="h4">
                                <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.insurancePolicies.premiumsPaidByOther', $deceasedName)"/>
                            </fo:block>
                            <fo:table>
                                <fo:table-column column-number="1" column-width="70%"/>
                                <fo:table-column column-number="2" column-width="30%"/>
                                <fo:table-body>
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.insurancePolicies.premiumsNotPayingOut.question', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:choose>
                                                <xsl:when test="allAssets/insurancePolicy/isInsurancePremiumsPayedForSomeoneElse='false'">
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                    <xsl:if test="allAssets/insurancePolicy/moreThanMaxValue">
                                        <xsl:call-template name="table-row">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question', $deceasedName)"/>
                                            <xsl:with-param name="value">
                                                <xsl:if test="allAssets/insurancePolicy/moreThanMaxValue">
                                                    <xsl:choose>
                                                        <xsl:when test="allAssets/insurancePolicy/moreThanMaxValue='false'">
                                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:if>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                        <xsl:call-template name="table-row">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.insurancePolicies.buyAnnuity.question', $deceasedName)"/>
                                            <xsl:with-param name="value">
                                                <xsl:choose>
                                                    <xsl:when test="allAssets/insurancePolicy/isAnnuitiesBought='false'">
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                        <xsl:call-template name="table-row">
                                            <xsl:with-param name="label"
                                                            select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.insurance.policies.overview.other.question4', $deceasedName)"/>
                                            <xsl:with-param name="value">
                                                <xsl:choose>
                                                    <xsl:when test="allAssets/insurancePolicy/isInTrust='false'">
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:if>
                                </fo:table-body>
                            </fo:table>
                            </xsl:when>
                        </xsl:choose>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>


        <xsl:comment>Assets business interests section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.businessInterests.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/businessInterest != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.businessInterest.isOwned', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/businessInterest/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:choose>
                                    <xsl:when test="allAssets/businessInterest/isOwned='true'">
                                        <xsl:call-template name="table-row--currency">
                                            <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.businessInterest.inputLabel1', $deceasedName)" />
                                            <xsl:with-param name="value">
                                                <xsl:if test="allAssets/businessInterest/value">
                                                    <xsl:value-of select='allAssets/businessInterest/value'/>
                                                </xsl:if>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                </xsl:choose>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>


        <xsl:comment>Assets nominated assets section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.nominated')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/nominated != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.nominated.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/nominated/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:choose>
                                    <xsl:when test="allAssets/nominated/isOwned='true'">
                                        <xsl:call-template name="table-row--currency">
                                            <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.nominated.inputLabel1', $deceasedName)" />
                                            <xsl:with-param name="value">
                                                <xsl:if test="allAssets/nominated/value">
                                                    <xsl:value-of select='allAssets/nominated/value'/>
                                                </xsl:if>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                </xsl:choose>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>


        <xsl:comment>Assets held in trust section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.heldInTrust.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/heldInTrust != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.trusts.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/heldInTrust/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/heldInTrust/isOwned='true'">
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.trusts.moreThanOne.question', $deceasedName)"/>
                                        <xsl:with-param name="value">
                                            <xsl:if test="not(allAssets/heldInTrust/isMoreThanOne)">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'site.noneInEstate')"/>
                                            </xsl:if>
                                            <xsl:if test="allAssets/heldInTrust/isMoreThanOne">
                                                <xsl:choose>
                                                    <xsl:when test="allAssets/heldInTrust/isMoreThanOne='false'">
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:if>
                                        </xsl:with-param>
                                    </xsl:call-template>
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.heldInTrust.valueOfTrust', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/heldInTrust/value">
                                                <xsl:value-of select='allAssets/heldInTrust/value'/>
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


        <xsl:comment>Assets foreign assets section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.foreign.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/foreign != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.foreign.deceasedOwned.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/foreign/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/foreign/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.foreign.inputLabel1', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/foreign/value">
                                                <xsl:value-of select='allAssets/foreign/value'/>
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


        <xsl:comment>Assets Money Owed section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.assets.moneyOwed', $deceasedName)"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/moneyOwed != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.moneyOwed.isOwned', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/moneyOwed/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/moneyOwed/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.moneyOwed.inputLabel1', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/moneyOwed/value">
                                                <xsl:value-of select='allAssets/moneyOwed/value'/>
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


        <xsl:comment>Assets Other assets section starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:block  role="H3" xsl:use-attribute-sets="h3">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.other.title')"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="allAssets/other != ''">
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.other.isOwned', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allAssets/other/isOwned='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:if test="allAssets/other/isOwned='true'">
                                    <xsl:call-template name="table-row--currency">
                                        <xsl:with-param name="label" select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.assets.moneyOwed.inputLabel1', $deceasedName)" />
                                        <xsl:with-param name="value">
                                            <xsl:if test="allAssets/other/value">
                                                <xsl:value-of select='allAssets/other/value'/>
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
        <fo:block xsl:use-attribute-sets="section" page-break-inside="avoid">
            <fo:table>
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body>
                    <xsl:call-template name="table-row--currency-total">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'page.iht.application.assets.overview.total')"/>
                        <xsl:with-param name="value" select='$assetsTotal'/>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
