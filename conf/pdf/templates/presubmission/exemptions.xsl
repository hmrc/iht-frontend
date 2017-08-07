<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-exemptions">
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" page-break-before="always">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
        </fo:block>
        <fo:block font-family="OpenSans" font-size="12pt" font-weight="regular" space-before="0.5cm">
            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.guidance1')"/>
        </fo:block>
        <xsl:comment>Exemptions Left to spouse or partner section starts only if if maried or civil partnership</xsl:comment>
        <xsl:if test="allExemptions/partner">
            <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm"
                      page-break-inside="avoid">
                <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.partner.assetsLeftToSpouse.title')"/>
            </fo:block>
            <fo:block font-family="OpenSans" font-size="12pt">
                <fo:table space-before="0.5cm">
                    <fo:table-column column-number="1" column-width="70%"/>
                    <fo:table-column column-number="2" column-width="30%"/>
                    <fo:table-body font-size="12pt">
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.exemptions.spouse.assetLeftToSpouse.question', $deceasedName)"/>
                            <xsl:with-param name="value">
                                <xsl:if test="allExemptions/partner/isAssetForDeceasedPartner='true'"><xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/></xsl:if>
                                <xsl:if test="allExemptions/partner/isAssetForDeceasedPartner='false'"><xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/></xsl:if>
                            </xsl:with-param>
                        </xsl:call-template>
                        <xsl:if test="allExemptions/partner/isAssetForDeceasedPartner='true'">
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.partner.homeInUK.question')"/>
                                <xsl:with-param name="value">
                                    <xsl:if test="allExemptions/partner/isPartnerHomeInUK">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                    </xsl:if>
                                    <xsl:if test="not(allExemptions/partner/isPartnerHomeInUK)">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                    </xsl:if>
                                </xsl:with-param>
                            </xsl:call-template>
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'page.iht.application.exemptions.partner.name.title')"/>
                                <xsl:with-param name="value"
                                                select="concat(allExemptions/partner/firstName,' ', allExemptions/partner/lastName)"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'page.iht.application.exemptions.partner.dateOfBirth.question.title')"/>
                                <xsl:with-param name="value"
                                                select="scala:getDateForDisplay($translator, allExemptions/partner/dateOfBirth)"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'page.iht.application.exemptions.partner.nino.sectionTitle')"/>
                                <xsl:with-param name="value"
                                                select="allExemptions/partner/nino"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row--currency">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'page.iht.application.exemptions.overview.partner.totalAssets.title')"/>
                                <xsl:with-param name="value">
                                    <xsl:if test="allExemptions/partner/totalAssets">
                                        <xsl:value-of select='allExemptions/partner/totalAssets'/>
                                    </xsl:if>
                                </xsl:with-param>

                            </xsl:call-template>
                        </xsl:if>

                    </fo:table-body>
                </fo:table>
            </fo:block>
        </xsl:if>
        <xsl:comment>Exemptions Charities Section</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.charities.assetsLeftToCharities.title')"/>
            <xsl:choose>
                <xsl:when test="allExemptions/charity">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.exemptions.charities.assetLeftToCharity.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allExemptions/charity/isSelected='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-body>
                        </fo:table>
                        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm">
                            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.charityOverviewTable.header')"/>
                            <xsl:if test="charities">
                                <fo:table space-before="0.5cm">
                                    <fo:table-column column-number="1" column-width="70%"/>
                                    <fo:table-column column-number="2" column-width="30%"/>
                                    <fo:table-body font-size="12pt">
                                        <xsl:for-each select="charities">
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'page.iht.application.exemptions.charityName.sectionTitle')"/>
                                                <xsl:with-param name="value" select="name"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.charities.charityNo.question')"/>
                                                <xsl:with-param name="value" select="number"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row--currency">
                                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.charities.totalValueOfAssetsCharityReceived')"/>
                                                <xsl:with-param name="value">
                                                    <xsl:if test="totalValue">
                                                        <xsl:value-of select='totalValue'/>
                                                    </xsl:if>
                                                </xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </fo:table-body>
                                </fo:table>
                            </xsl:if>
                            <xsl:if test="not(charities)">
                                <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.charityOverview.noCharities.text')"/>
                                </fo:block>
                            </xsl:if>
                        </fo:block>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                        <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.charityOverview.noCharities.text')"/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Exemptions Qualifying bodies Section</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title')"/>
            <xsl:choose>
                <xsl:when test="allExemptions/qualifyingBody">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.exemptions.qualifyingBodyOverview.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:choose>
                                            <xsl:when test="allExemptions/qualifyingBody/isSelected='false'">
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.no')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'iht.yes')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-body>
                        </fo:table>
                        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm">
                            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.qualifyingBodyOverviewTable.header')"/>
                            <xsl:if test="qualifyingBodies">
                                <fo:table space-before="0.5cm">
                                    <fo:table-column column-number="1" column-width="70%"/>
                                    <fo:table-column column-number="2" column-width="30%"/>
                                    <fo:table-body font-size="12pt">
                                        <xsl:for-each select="qualifyingBodies">
                                            <xsl:call-template name="table-row">
                                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.estateReport.qualifyingBodies.qualifyingBodyName')"/>
                                                <xsl:with-param name="value" select="name"/>
                                            </xsl:call-template>
                                            <xsl:call-template name="table-row--currency">
                                                <xsl:with-param name="label" select="scala:getMessagesText($translator, 'page.iht.application.exemptions.overview.qualifyingBody.detailsOverview.value.title')"/>
                                                <xsl:with-param name="value">
                                                    <xsl:if test="totalValue">
                                                        <xsl:value-of select='totalValue'/>
                                                    </xsl:if>
                                                </xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </fo:table-body>
                                </fo:table>
                            </xsl:if>
                            <xsl:if test="not(qualifyingBodies)">
                                <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.qualifyingBodyOverview.noQualifyingBodies.text')"/>
                                </fo:block>
                            </xsl:if>
                        </fo:block>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                        <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.application.exemptions.qualifyingBodyOverview.noQualifyingBodies.text')"/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block>
        <xsl:comment>Assets Total section starts</xsl:comment>
        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="0.5cm">
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body font-size="12pt">

                    <xsl:call-template name="table-row--currency-total">
                        <xsl:with-param name="label"
                                        select="scala:getMessagesText($translator, 'pdf.totalexemptions.text')"/>
                        <xsl:with-param name="value" select='$exemptionsTotal'/>
                    </xsl:call-template>

                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
