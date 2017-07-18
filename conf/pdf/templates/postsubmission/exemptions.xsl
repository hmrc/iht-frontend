<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
>

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="exemptionsTotal"/>

    <xsl:template name="exemptions">
        <xsl:param name="value"/>

        <xsl:choose>
            <xsl:when test="freeEstate/estateExemptions != ''">

                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="1.5cm">
                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
                </fo:block>

                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
                    <fo:block>
                        <xsl:for-each select="freeEstate/estateExemptions">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-tall-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.exemption.table.text')"/>
                                        <xsl:with-param name="value" select="exemptionType"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.total.text')"/>
                                        <xsl:with-param name="value" select='format-number(number(overrideValue), "##,###.00")'/>
                                    </xsl:call-template>

                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:for-each>

                        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold">
                            <fo:table space-before="0.5cm">
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-money-short-vpad-no-border">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.totalexemptions.text')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number($exemptionsTotal), "##,###.00")'/>
                                    </xsl:call-template>

                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </fo:block>
                </fo:block>
            </xsl:when>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>
