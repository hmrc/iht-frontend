<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">
    <xsl:param name="pdfFormatter"/>

    <xsl:template name="table-row-tall">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-short-vpad">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-money-tall">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
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

    <xsl:template name="table-row-money-tall-value-align-right">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right" padding-left="4pt">
                <fo:block>
                    &#xA3;<xsl:value-of select='format-number(number($value), "##,###0.00")'/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-money-tall-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.3mm black" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
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

    <xsl:template name="table-row-money-short-vpad-no-border">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="0pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="0pt" padding-bottom="6pt">
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

    <xsl:template name="table-row-money-3-values-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value1"/>
        <xsl:param name="value2"/>
        <xsl:param name="value3"/>
        <fo:table-row border-top="solid 0.3mm black" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right" padding-left="4pt">
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
            <fo:table-cell text-align="right" padding-left="4pt">
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
            <fo:table-cell text-align="right" padding-left="4pt">
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

        <fo:table-row border-top="solid 0.3mm black" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt"
                           padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt"
                           padding-bottom="6pt">
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
        <fo:table-row border-top="solid 0.3mm black" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.3mm black" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-border-top-black-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm black" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-short-vpad-border-top-grey-thin">
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block/>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-tall-border-both-grey-thin">
        <fo:table-row border-top="solid 0.1mm gray" border-bottom="solid 0.1mm gray"
                      line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block/>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-blank-tall-border-both-black-thick">
        <fo:table-row border-top="solid 0.3mm black" border-bottom="solid 0.1mm gray"
                      line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block/>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-lpad-border-top-black">
        <xsl:param name="label"/>
        <xsl:param name="value"/>

        <fo:table-row border-top="solid 0.3mm black" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="20pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-tall-lpad-border-top-grey-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="30pt">
            <fo:table-cell text-align="left" padding-left="4pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="20pt">
                <fo:block>
                    <xsl:value-of select="$value"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-yes-no-short-lpad-border-top-grey-thin">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt"
                           padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of
                            select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="20pt" padding-top="6pt"
                           padding-bottom="6pt">
                <fo:block>
                    <!-- choose has been used to show Yes for true and No for false -->
                    <xsl:choose>
                        <xsl:when
                                test="$value = 'true'">
                            <xsl:value-of
                                    select="i18n:getMessagesText($translator, 'iht.yes')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of
                                    select="i18n:getMessagesText($translator, 'iht.no')"/>
                        </xsl:otherwise>
                    </xsl:choose>

                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="table-row-uk-address">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
                <fo:block>
                    <xsl:value-of select="$label"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="left" padding-left="4pt" padding-top="6pt" padding-bottom="6pt">
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