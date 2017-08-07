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

    <xsl:template name="deceased-details">
        <xsl:param name="value"/>

        <fo:block xsl:use-attribute-sets="h2">
            <xsl:value-of
                    select="scala:getMessagesText($translator, 'page.iht.registration.registrationSummary.deceasedTable.title')"/>
        </fo:block>

        <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
            <fo:block>
                <fo:table space-before="0.5cm">
                    <fo:table-column column-number="1" column-width="22%"/>
                    <fo:table-column column-number="2" column-width="78%"/>
                    <fo:table-body font-size="12pt">

                        <xsl:call-template name="table-row-short-vpad-border-top-black">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.firstName')"/>
                            <xsl:with-param name="value" select="deceasedDetails/firstName"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.lastName')"/>
                            <xsl:with-param name="value" select="deceasedDetails/lastName"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.dateofbirth')"/>
                            <xsl:with-param name="value"
                                            select="scala:getDateForDisplay($translator,deceasedDetails/dateOfBirth)"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.dateOfDeath')"/>
                            <xsl:with-param name="value"
                                            select="scala:getDateForDisplay($translator,deceasedDateOfDeath/dateOfDeath)"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.nationalInsuranceNo')"/>
                            <xsl:with-param name="value" select="deceasedDetails/nino"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-uk-address">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'pdf.registration.lastContactAddress')"/>
                            <xsl:with-param name="value" select="deceasedDetails/ukAddress"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'iht.registration.deceased.locationOfPermanentHome')"/>
                            <xsl:with-param name="value" select="deceasedDetails/domicile"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row">
                            <xsl:with-param name="label"
                                            select="scala:getMessagesText($translator, 'page.iht.registration.deceasedDetails.maritalStatus.label')"/>
                            <xsl:with-param name="value" select="deceasedDetails/maritalStatus"/>
                        </xsl:call-template>
                        <xsl:comment>Blank row to display line at end of section</xsl:comment>
                        <xsl:call-template name="table-row-blank-short-vpad-border-top-grey-thin"/>
                    </fo:table-body>
                </fo:table>
            </fo:block>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
