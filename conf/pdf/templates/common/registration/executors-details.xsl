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

    <xsl:template name="executors-details">
        <xsl:param name="value"/>

        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" page-break-before="always">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.registration.othersApplyingForProbate')"/>
        </fo:block>

        <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before=".5cm">
            <fo:block>
                <xsl:for-each select="./coExecutors">
                    <fo:table space-before="0.5cm" space-after="1.5cm">
                        <fo:table-column column-number="1" column-width="22%"/>
                        <fo:table-column column-number="2" column-width="78%"/>
                        <fo:table-body font-size="12pt">
                            <xsl:call-template name="table-row-short-vpad-border-top-black">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.firstName')"/>
                                <xsl:with-param name="value" select="firstName"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row-short-vpad">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.lastName')"/>
                                <xsl:with-param name="value" select="lastName"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row-short-vpad">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.dateofbirth')"/>
                                <xsl:with-param name="value"
                                                select="scala:getDateForDisplay($translator,dateOfBirth)"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row-short-vpad">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.nationalInsuranceNo')"/>
                                <xsl:with-param name="value" select="nino"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row-uk-address">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.registration.contactAddress')"/>
                                <xsl:with-param name="value" select="ukAddress"/>
                            </xsl:call-template>
                            <xsl:call-template name="table-row-short-vpad">
                                <xsl:with-param name="label"
                                                select="scala:getMessagesText($translator, 'iht.registration.checklist.phoneNo.upperCaseInitial')"/>
                                <xsl:with-param name="value" select="contactDetails/phoneNo"/>
                            </xsl:call-template>
                            <xsl:comment>Blank row to display line at end of section</xsl:comment>
                            <xsl:call-template name="table-row-blank-short-vpad-border-top-grey-thin"/>
                        </fo:table-body>
                    </fo:table>
                </xsl:for-each>
            </fo:block>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
