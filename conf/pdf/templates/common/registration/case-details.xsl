<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="ihtReference"/>

    <xsl:include href="pdf/templates/common/registration/deceased-details.xsl"/>
    <xsl:include href="pdf/templates/common/registration/applicant-details.xsl"/>
    <xsl:include href="pdf/templates/common/registration/executors-details.xsl"/>

    <xsl:template match="RegistrationDetails">
        <xsl:comment>Deceased Details section starts</xsl:comment>
        <xsl:call-template name="deceased-details">
            <xsl:with-param name="value" select="deceasedDetails"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Deceased Details section ends</xsl:comment>

        <xsl:comment>Applicant Details section starts</xsl:comment>
        <xsl:call-template name="applicant-details">
            <xsl:with-param name="value" select="applicantDetails"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Applicant Details section ends</xsl:comment>

        <xsl:comment>Co executor details section starts</xsl:comment>
        <xsl:choose>
            <xsl:when test="./coExecutors != ''">
                <xsl:call-template name="executors-details">
                    <xsl:with-param name="value" select="./coExecutors"></xsl:with-param>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
        <xsl:comment>Co executor details section ends</xsl:comment>
    </xsl:template>
</xsl:stylesheet>
