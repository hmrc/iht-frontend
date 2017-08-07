<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="deceasedName"/>
    <xsl:param name="preDeceasedName"/>
    <xsl:param name="marriageLabel"/>

    <xsl:template name="tnrb">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="deceased/transferOfNilRateBand != ''">

                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="1.5cm">
                    <xsl:value-of
                            select="scala:getMessagesTextWithParameter($translator, 'pdf.inheritance.tax.application.summary.tnrb.title', $preDeceasedName )"/>
                </fo:block>

                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
                    <fo:block>
                        <fo:table space-before="0.5cm" space-after="1.0cm">
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">

                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'iht.firstName')"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spouse/firstName"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'iht.lastName')"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spouse/lastName"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.dateOfMarriage', $marriageLabel)"/>
                                    <xsl:with-param name="value"
                                                    select="scala:getDateForDisplay($translator,deceased/transferOfNilRateBand/deceasedSpouses/spouse/dateOfMarriage)"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'page.iht.application.tnrbEligibilty.overview.partner.dod.question', $preDeceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="scala:getDateForDisplay($translator,deceased/transferOfNilRateBand/deceasedSpouses/spouse/dateOfDeath)"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.permanentHome.question', $deceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/domiciledInUk"/>
                                </xsl:call-template>


                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.giftsMadeBeforeDeath.question', $preDeceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/otherGifts"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.giftsWithReservation.question', $deceasedName, $deceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/giftsWithReservation"/>
                                </xsl:call-template>


                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'iht.estateReport.tnrb.stateClaim.question')"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/agriculturalOrBusinessRelief"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.tnrb.benefitFromTrust.question', $preDeceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/benefitFromTrust"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.charity.question', $deceasedName, $deceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/whollyExempt"/>
                                </xsl:call-template>

                                <xsl:call-template name="table-row--yes-no">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesTextWithParameters($translator, 'page.iht.application.tnrbEligibilty.overview.jointlyOwned.question', $deceasedName, $deceasedName)"/>
                                    <xsl:with-param name="value"
                                                    select="deceased/transferOfNilRateBand/deceasedSpouses/spousesEstate/jointAssetsPassingToOther"/>
                                </xsl:call-template>




                            </fo:table-body>
                        </fo:table>
                        <xsl:value-of
                                select="scala:getMessagesText($translator, 'iht.pdf.TnrbEligibilty.thresholdLimit.label')"/>
                    </fo:block>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
