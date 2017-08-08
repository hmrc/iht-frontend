<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="deceasedName"/>

    <xsl:template name="declaration">
        <fo:block page-break-before="always" xsl:use-attribute-sets="h2">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.declaration.title')"/>
        </fo:block>
        <xsl:comment>First bullet block starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="copy">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.declaration.youMayFaceProsecution')" />
        </fo:block>

        <fo:list-block xsl:use-attribute-sets="copy list">
            <fo:list-item xsl:use-attribute-sets="list__item">
                <fo:list-item-label end-indent="1em">
                    <fo:block>
                        &#x2022;
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="2em">
                    <fo:block>
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.declaration.withholdInformation')" />
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list__item">
                <fo:list-item-label end-indent="1em">
                    <fo:block>
                        &#x2022;
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="2em">
                    <fo:block>
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.declaration.dontTellHMRC')" />
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
        <xsl:comment>First bullet block ends</xsl:comment>

        <xsl:comment>Second bullet block starts</xsl:comment>
        <fo:block xsl:use-attribute-sets="copy">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.pdf.declaration.declaringThat')" />
        </fo:block>

        <fo:list-block xsl:use-attribute-sets="copy list">
            <fo:list-item xsl:use-attribute-sets="list__item">
                <fo:list-item-label start-indent="1em">
                    <fo:block>
                        &#x2022;
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="2em">
                    <fo:block>
                        <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'iht.estateReport.declaration.completedAllReasonableEnquiries', $deceasedName)" />
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list__item">
                <fo:list-item-label start-indent="1em">
                    <fo:block>
                        &#x2022;
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="2em">
                    <fo:block>
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.declaration.correctAndComplete')" />
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list__item">
                <fo:list-item-label start-indent="1em">
                    <fo:block>
                        &#x2022;
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="2em">
                    <fo:block>
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.noInheritanceTaxPayable')" />
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
        <xsl:comment>Second bullet block ends</xsl:comment>

        <fo:block xsl:use-attribute-sets="copy">
            <xsl:value-of select="scala:getMessagesText($translator, 'iht.iht.estateReport.declaration.acceptanceOfDeclaration')" />
        </fo:block>

        <xsl:comment>Co executors Signatures</xsl:comment>

            <xsl:for-each select="../RegistrationDetails/coExecutors">
            <fo:block xsl:use-attribute-sets="copy section">
            <fo:table>
                <fo:table-column column-number="1" column-width="30%"/>
                <fo:table-column column-number="2" column-width="70%"/>
                <fo:table-body>
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'pdf.name.text')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <xsl:value-of select="concat(firstName, ' ', lastName)"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'pdf.signature.date.text')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <fo:leader leader-pattern="dots" leader-length="8cm"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'pdf.signature.signature.text')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block xsl:use-attribute-sets="copy declaration">
                                    <fo:leader leader-pattern="dots" leader-length="8cm"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
            </fo:block>
            </xsl:for-each>

    </xsl:template>
</xsl:stylesheet>
