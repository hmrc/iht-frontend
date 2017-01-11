<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="pre-gifts">
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" page-break-before="always">
            <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.givenAway.title')"/>
        </fo:block>

        <fo:block font-family="OpenSans" font-size="12pt" font-weight="regular" space-before="0.5cm">
            <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.gifts.summary.p1')"/>
        </fo:block>

        <xsl:comment>Gifts Given Away section starts</xsl:comment>
        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm">
            <xsl:choose>
                <xsl:when test="allGifts != ''">
                    <fo:block font-family="OpenSans" font-size="12pt">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesTextWithParameter($translator, 'page.iht.application.gifts.overview.givenAway.question1', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allGifts/isGivenAway='false'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/></xsl:if>
                                        <xsl:if test="allGifts/isGivenAway='true'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/></xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:comment>Gifts With Reservation section starts</xsl:comment>
                    <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
                        <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.withReservation.title')"/>
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesTextWithParameter($translator, 'iht.estateReport.gifts.reservation.question', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allGifts/isReservation='false'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/></xsl:if>
                                        <xsl:if test="allGifts/isReservation='true'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/></xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:comment>Gifts With Reservation section starts</xsl:comment>
                    <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm" page-break-inside="avoid">
                        <xsl:value-of select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath')"/>
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="70%"/>
                            <fo:table-column column-number="2" column-width="30%"/>
                            <fo:table-body font-size="12pt">
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesTextWithParameter($translator, 'page.iht.application.gifts.overview.sevenYears.question1', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allGifts/isGivenInLast7Years='false'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/></xsl:if>
                                        <xsl:if test="allGifts/isGivenInLast7Years='true'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/></xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                                <xsl:call-template name="table-row">
                                    <xsl:with-param name="label"
                                                    select="i18n:getMessagesTextWithParameter($translator, 'page.iht.application.gifts.overview.sevenYears.question2', $deceasedName)"/>
                                    <xsl:with-param name="value">
                                        <xsl:if test="allGifts/isToTrust='false'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.no')"/></xsl:if>
                                        <xsl:if test="allGifts/isToTrust='true'"><xsl:value-of select="i18n:getMessagesText($translator, 'iht.yes')"/></xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:comment>Gifts Value and Year Break down</xsl:comment>
                    <xsl:if test="allGifts/isGivenAway='true'">
                        <fo:block font-family="OpenSans" font-size="16pt" font-weight="regular" space-before="0.5cm"
                                  page-break-inside="avoid">
                            <xsl:value-of
                                    select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.valueOfGiftsGivenAway')"/>
                            <fo:block font-family="OpenSans" font-size="12pt" font-weight="regular"
                                      space-before="0.5cm">
                                <fo:table>
                                    <fo:table-header>
                                        <fo:table-row line-height="18pt">
                                            <fo:table-cell text-align="left" padding-left="4pt">
                                                <fo:block/>
                                            </fo:table-cell>
                                            <fo:table-cell text-align="left" padding-left="4pt">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle1')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell text-align="left" padding-left="4pt">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle2')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell text-align="left" padding-left="4pt">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle3')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </fo:table-header>
                                    <fo:table-body>
                                        <xsl:choose>
                                            <xsl:when test="giftsList">
                                                <xsl:for-each select="giftsList">
                                                    <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
                                                        <fo:table-cell text-align="left" padding-left="4pt">
                                                            <fo:block>
                                                                <xsl:value-of select="startDate"/> to
                                                                <xsl:value-of select="endDate"/>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                        <fo:table-cell text-align="left" padding-left="4pt">
                                                            <fo:block>
                                                                <xsl:if test="value">
                                                                    <xsl:choose>
                                                                        <xsl:when test="value &gt; 1">
                                                                            &#xA3;<xsl:value-of select='format-number(number(value), "##,###.00")'/>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            &#xA3;<xsl:value-of select="value"/>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </xsl:if>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                        <fo:table-cell>
                                                            <fo:block>
                                                                <xsl:if test="exemptions">
                                                                    <xsl:choose>
                                                                        <xsl:when test="exemptions &gt; 1">
                                                                            &#xA3;<xsl:value-of select='format-number(number(exemptions), "##,###.00")'/>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            &#xA3;<xsl:value-of select="exemptions"/>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </xsl:if>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                        <fo:table-cell text-align="left" padding-left="4pt">
                                                            <fo:block>
                                                                <xsl:choose>
                                                                    <xsl:when test="value and exemptions">
                                                                        <xsl:choose>
                                                                            <xsl:when test="value - exemptions &gt; 1">
                                                                                &#xA3;<xsl:value-of
                                                                                    select='format-number(number(value - exemptions), "##,###.00")'/>
                                                                            </xsl:when>
                                                                            <xsl:otherwise>
                                                                                &#xA3;<xsl:value-of
                                                                                        select='format-number(number(value - exemptions), "0.00")'/>
                                                                            </xsl:otherwise>
                                                                        </xsl:choose>
                                                                    </xsl:when>
                                                                    <xsl:when test="value and value &gt; 1">
                                                                        &#xA3;<xsl:value-of
                                                                            select='format-number(number(value), "##,###.00")'/>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        &#xA3;0.00
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </fo:block>
                                                        </fo:table-cell>
                                                    </fo:table-row>
                                                </xsl:for-each>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <fo:table-row border-top="solid 0.1mm gray" line-height="18pt">
                                                    <fo:table-cell>
                                                        <fo:block/>
                                                    </fo:table-cell>
                                                    <fo:table-cell>
                                                        <fo:block/>
                                                    </fo:table-cell>
                                                    <fo:table-cell>
                                                        <fo:block/>
                                                    </fo:table-cell>
                                                    <fo:table-cell>
                                                        <fo:block/>
                                                    </fo:table-cell>
                                                </fo:table-row>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:table-body>
                                </fo:table>
                                <fo:table space-before="0.5cm">
                                    <fo:table-column column-number="1" column-width="70%"/>
                                    <fo:table-column column-number="2" column-width="30%"/>
                                    <fo:table-body font-size="12pt">
                                        <xsl:call-template name="table-row-money-border-top-black">
                                            <xsl:with-param name="label"
                                                            select="i18n:getMessagesText($translator, 'page.iht.application.gifts.overview.value.question1')"/>
                                            <xsl:with-param name="value">
                                                <xsl:value-of select='$giftsTotal'/>
                                            </xsl:with-param>
                                        </xsl:call-template>
                                        <xsl:call-template name="table-row-bottom-blank"/>
                                    </fo:table-body>
                                </fo:table>
                            </fo:block>
                        </fo:block>
                    </xsl:if>

                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="i18n:getMessagesText($translator, 'site.noneInEstate')"/>
                </xsl:otherwise>
            </xsl:choose>
        </fo:block>


    </xsl:template>
</xsl:stylesheet>
