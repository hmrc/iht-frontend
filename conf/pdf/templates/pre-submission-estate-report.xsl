<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="applicantName"/>

    <xsl:include href="pdf/templates/registration/case-details.xsl"/>
    <xsl:include href="pdf/templates/application/application-details.xsl"/>

    <xsl:template match="/">
        <fo:root font-family="OpenSans">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simple"
                                       page-height="29.7cm" page-width="21.0cm"
                                       margin-top="0.2cm" margin-bottom="0.2cm"
                                       margin-left="2cm" margin-right="2cm">
                    <fo:region-body margin-top="2.5cm" margin-bottom="2.5cm"/>
                    <fo:region-before extent="2.4cm"/>
                    <fo:region-after extent="2.4cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="simple">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block border-bottom-style="solid">
                        <fo:external-graphic src="url('pdf/logo/hmrc_logo.jpg')" height="50px" content-width="scale-to-fit"/>
                    </fo:block>
                </fo:static-content>

                <fo:static-content flow-name="xsl-region-after" font-family="OpenSans" font-size="12pt">
                    <fo:block text-align="right" padding-top="6pt">
                        <fo:table>
                            <fo:table-body font-size="8pt">
                                <fo:table-row>
                                    <fo:table-cell text-align= "left">
                                        <fo:block><xsl:value-of select="i18n:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>Page <fo:page-number format="1"/></fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-family="OpenSans-Bold" font-size="24" font-weight="bold">
                        <xsl:value-of select="i18n:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/>
                    </fo:block>
                    <fo:block font-family="OpenSans" font-size="12" font-weight="normal" space-before="0.5cm">
                        <xsl:value-of
                                select="concat(i18n:getMessagesText($translator, 'pdf.inheritance.tax.reference'), ' ', $ihtReference)"/>
                    </fo:block>

                    <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                        <xsl:value-of select="i18n:getMessagesTextWithParameters($translator, 'pdf.inheritance.tax.application.summary.p1', $deceasedName, $applicantName)"/>
                    </fo:block>

                    <fo:block font-family="OpenSans" font-weight="regular" font-size="12pt" space-before="0.5cm">
                        <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.inheritance.tax.application.summary.p2')"/>
                    </fo:block>
                    <xsl:apply-templates/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>

    </xsl:template>
</xsl:stylesheet>
