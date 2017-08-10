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
                    <xsl:value-of select="$label"/>
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
                    <xsl:value-of select="$label"/>
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

    <xsl:template name="table-row--currency-total">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
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

    <xsl:template name="table-row--currency-right-total">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row--bottom">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    <xsl:if test='starts-with($value, "-") and $value!="-0"'>-</xsl:if>&#xA3;<xsl:value-of select='format-number(number(translate($value, "-", "")), "##,##0.00")'/>
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
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell set-right">
                <fo:block>
                    <xsl:if test='starts-with($value, "-") and $value!="-0"'>-</xsl:if>&#xA3;<xsl:value-of select='format-number(number(translate($value, "-", "")), "##,##0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency-right--heavy">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell--heavy">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell--heavy set-right">
                <fo:block>
                    <xsl:if test='starts-with($value, "-")'>-</xsl:if>&#xA3;<xsl:value-of select='format-number(number(translate($value, "-", "")), "##,##0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency-right-total-heavy">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row--bottom">
            <fo:table-cell xsl:use-attribute-sets="cell--heavy">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell--heavy set-right">
                <fo:block>
                    <xsl:if test='starts-with($value, "-")'>-</xsl:if>&#xA3;<xsl:value-of select='format-number(number(translate($value, "-", "")), "##,##0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency-right-total-shaded">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell--heavy shaded">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="cell--heavy shaded set-right">
                <fo:block>
                    <xsl:if test='starts-with($value, "-")'>-</xsl:if>&#xA3;<xsl:value-of select='format-number(number(translate($value,"-","")), "##,##0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row--currency-3-col">
        <xsl:param name="label"/>
        <xsl:param name="value1"/>
        <xsl:param name="value2"/>
        <xsl:param name="value3"/>
        <fo:table-row xsl:use-attribute-sets="row row--heavy">
            <fo:table-cell xsl:use-attribute-sets="tabular-cell">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tabular-cell set-right">
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
            <fo:table-cell xsl:use-attribute-sets="tabular-cell set-right">
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
            <fo:table-cell xsl:use-attribute-sets="tabular-cell tabular-cell--no-right-border set-right">
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

    <xsl:template name="table-row--yes-no">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row xsl:use-attribute-sets="row">
            <fo:table-cell xsl:use-attribute-sets="cell">
                <fo:block>
                    <xsl:value-of
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

    <xsl:template name="table-row--address">
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