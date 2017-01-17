<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:param name="translator"/>
    <xsl:param name="ihtReference"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="assetsTotal"/>
    <xsl:param name="debtsTotal"/>
    <xsl:param name="exemptionsTotal"/>
    <xsl:param name="giftsTotal"/>
    <xsl:param name="estateValue"/>
    <xsl:param name="thresholdValue"/>
    <xsl:param name="deceasedName"/>
    <xsl:param name="preDeceasedName"/>
    <xsl:param name="marriageLabel"/>
    <xsl:param name="marriedOrCivilPartnershipLabel"/>
    <xsl:param name="kickout"/>
    <xsl:param name="giftsTotalExclExemptions"/>

    <xsl:include href="pdf/templates/application/estate-summary.xsl"/>
    <xsl:include href="pdf/templates/application/assets.xsl"/>
    <xsl:include href="pdf/templates/application/gifts.xsl"/>
    <xsl:include href="pdf/templates/application/debts.xsl"/>
    <xsl:include href="pdf/templates/application/exemptions.xsl"/>
    <xsl:include href="pdf/templates/application/tnrb.xsl"/>
    <xsl:include href="pdf/templates/application/declaration.xsl"/>

    <xsl:template match="ApplicationDetails">
        <xsl:call-template name="estate-summary"/>
        <xsl:call-template name="pre-assets"/>
        <xsl:call-template name="pre-gifts"/>
        <xsl:call-template name="pre-debts"/>
        <xsl:call-template name="pre-exemptions"/>
        <xsl:if test="increaseIhtThreshold">
            <xsl:call-template name="pre-tnrb"/>
        </xsl:if>
        <xsl:if test="$kickout='false'">
            <xsl:call-template name="declaration"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
