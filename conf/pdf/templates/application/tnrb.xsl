<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-tnrb">
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" page-break-before="always">
            <xsl:value-of select="i18n:getMessagesTextWithParameter($translator, 'pdf.inheritance.tax.application.summary.tnrb.title', $preDeceasedName)"/>
        </fo:block>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm">
            <xsl:value-of select="i18n:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.partnerEstate.questions.heading',
            $preDeceasedName, formatter:getYearFromDate($pdfFormatter, widowCheck/dateOfPreDeceased))"/>
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body font-size="12pt">
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.permanentHome.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerLivingInUk='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.giftsMadeBeforeDeath.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isGiftMadeBeforeDeath='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.giftsWithReservation.question', ' ', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerGiftWithResToOther='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.stateClaim.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isStateClaimAnyBusiness='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.benefitFromTrust.question', $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isPartnerBenFromTrust='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.charity.question', ' ',  $deceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isEstateBelowIhtThresholdApplied='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label"
                                        select="i18n:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.jointlyOwned.question', ' ',  $deceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="increaseIhtThreshold/isJointAssetPassed='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block>
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" space-before="0.5cm">
            <xsl:value-of select="i18n:getMessagesTextWithParameter($translator, 'pdf.inheritance.tax.application.summary.tnrb.personalDetails.title', $preDeceasedName)"/>
        </fo:block>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm">
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" column-width="70%"/>
                <fo:table-column column-number="2" column-width="30%"/>
                <fo:table-body font-size="12pt">
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="i18n:getMessagesTextWithThreeParameters($translator, 'iht.estateReport.tnrb.partner.married', $deceasedName, $marriedOrCivilPartnershipLabel, $preDeceasedName)"/>
                        <xsl:with-param name="value">
                            <xsl:choose>
                                <xsl:when test="widowCheck/widowed='true'">
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="i18n:getMessagesTextWithParameter($translator, 'page.iht.application.tnrbEligibilty.overview.partner.dod.question', $preDeceasedName)"/>
                        <xsl:with-param name="value" select="formatter:getDateForDisplay($pdfFormatter, widowCheck/dateOfPreDeceased)"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="i18n:getMessagesText($translator, 'iht.name.upperCaseInitial')"/>
                        <xsl:with-param name="value" select="$preDeceasedName"/>
                    </xsl:call-template>
                    <xsl:call-template name="table-row">
                        <xsl:with-param name="label" select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.dateOfMarriage', $marriageLabel)"/>
                        <xsl:with-param name="value" select="formatter:getDateForDisplay($pdfFormatter, increaseIhtThreshold/dateOfMarriage)"/>
                    </xsl:call-template>
                </fo:table-body>
            </fo:table>
        </fo:block>

    </xsl:template>
</xsl:stylesheet>
