<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="declaration">
        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold" page-break-before="always">
            <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.signatures.heading')"/>
        </fo:block>
        <xsl:comment>Co executors Signatures</xsl:comment>
        <fo:block font-family="OpenSans" font-size="12pt" font-weight="regular">
            <xsl:for-each select="../RegistrationDetails/coExecutors">
            <fo:table space-before="0.5cm">
                <fo:table-column column-number="1" column-width="30%"/>
                <fo:table-column column-number="2" column-width="70%"/>
                <fo:table-body font-size="12pt">
                        <fo:table-row line-height="30pt">
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'iht.name.upperCaseInitial')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="concat(firstName, ' ', lastName)"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row line-height="30pt">
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.signature.date.text')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <fo:leader leader-pattern="dots" leader-length="8cm"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row line-height="30pt">
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.signature.signed.text')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <fo:leader leader-pattern="dots" leader-length="8cm"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
            </xsl:for-each>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
