<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">
    <xsl:param name="pdfFormatter"/>

    <xsl:template name="table-row">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    1. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--novalue">
        <xsl:param name="label"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    3. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
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


    <xsl:template name="table-row--currency-right">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    5. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    &#xA3;<xsl:value-of select='format-number(number($value), "##,##0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency-total">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:param name="modifierclass"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    7. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                            &#xA3;<xsl:value-of select='format-number(number($value), "##,###0.00")'/>

                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-money-3-values-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value1"/>
        <xsl:param name="value2"/>
        <xsl:param name="value3"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    9. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    <xsl:choose>
                        <xsl:when test="$value1 = '-1'">
                        </xsl:when>
                        <xsl:when test="$value1 &gt; 1">
                            &#xA3;<xsl:value-of select='format-number(number($value1), "##,##0.00")'/>
                        </xsl:when>
                        <xsl:otherwise>
                            &#xA3;<xsl:value-of select="$value1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    <xsl:choose>
                        <xsl:when test="$value2 = '-1'">
                        </xsl:when>
                        <xsl:when test="$value2 &gt; 1">
                            &#xA3;<xsl:value-of select='format-number(number($value2), "##,##0.00")'/>
                        </xsl:when>
                        <xsl:otherwise>
                            &#xA3;<xsl:value-of select="$value2"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    <xsl:choose>
                        <xsl:when test="$value3 = '-1'">
                        </xsl:when>
                        <xsl:when test="$value3 &gt; 1">
                            &#xA3;<xsl:value-of select='format-number(number($value3), "##,##0.00")'/>
                        </xsl:when>
                        <xsl:otherwise>
                            &#xA3;<xsl:value-of select="$value3"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-money-border-top-black-line-height-18">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    10. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
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

    <xsl:template name="table-row-short-vpad-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    11. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    12. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-border-top-black-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    13. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-short-vpad-border-top-grey-thin">
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                14. <fo:block/>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-tall-border-both-grey-thin">
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                15. <fo:block/>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-tall-border-both-black-thick">
        <fo:table-row xsl:use-attribute-sets="row row--heavy row--total">
            <fo:table-cell xsl:use-attribute-sets="cell">
                16. <fo:block/>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-lpad-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>

        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    17. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-lpad-border-top-grey-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    18. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell  xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-yes-no-short-lpad-border-top-grey-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    19. <xsl:value-of
                            select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <!-- choose has been used to show Yes for true and No for false -->
                    <xsl:choose>
                        <xsl:when
                                test="$value = 'true'">
                            <xsl:value-of
                                    select="scala:getMessagesText($translator, 'iht.yes')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of
                                    select="scala:getMessagesText($translator, 'iht.no')"/>
                        </xsl:otherwise>
                    </xsl:choose>

                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-uk-address">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    20. <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$value/ukAddressLine1"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$value/ukAddressLine2"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$value/ukAddressLine3"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$value/ukAddressLine4"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="$value/postCode"/>
                </fo:block>
                <fo:block>
                    <xsl:value-of select="formatter:countryName($pdfFormatter,$value/countryCode)"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>
</xsl:stylesheet>