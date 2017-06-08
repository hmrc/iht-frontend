<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">
    <xsl:param name="pdfFormatter"/>

    <xsl:template name="table-row-mortgage-money">
        <xsl:param name="address"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine1"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine2"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine3"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine4"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/postCode"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="formatter:countryName($pdfFormatter,$address/countryCode)"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:choose>
                        <xsl:when test="$value &gt; 1">
                            &#xA3;<xsl:value-of select='format-number(number($value), "##,###.00")'/>
                        </xsl:when>
                        <xsl:otherwise>
                            &#xA3;<xsl:value-of select="$value"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:block>

            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-mortgage">
        <xsl:param name="address"/>
        <xsl:param name="label"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine1"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine2"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine3"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/ukAddressLine4"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$address/postCode"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="formatter:countryName($pdfFormatter,$address/countryCode)"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>
</xsl:stylesheet>
