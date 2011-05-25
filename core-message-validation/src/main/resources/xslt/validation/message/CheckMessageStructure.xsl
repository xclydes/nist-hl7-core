<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	xmlns="urn:hl7-org:v2xml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="2.0">

  <xsl:output method="xml"/>

  <xsl:variable name="msgType">
    <xsl:value-of select="//HL7v2xStaticDef/@MsgStructID"/>
  </xsl:variable>

  <!-- Handle group tags ? -->
  <xsl:param name="groups"/>
  <!-- Is it an XML message -->
  <xsl:param name="xml"/>

  <xsl:key name="segments" match="//Segment" use="@Name"/>

  <xsl:template match="/">
    <xs:schema>
        <xsl:attribute name="targetNamespace">urn:hl7-org:v2xml</xsl:attribute>
        <xsl:attribute name="elementFormDefault">qualified</xsl:attribute>
      <!-- Create the segment types -->
<!--      <xsl:for-each select="//Segment[generate-id(.)=generate-id(key('segments', @Name)[1])]">
        <xsl:apply-templates select="." mode="type"/>
      </xsl:for-each> -->

      <xs:element>
        <xsl:attribute name="name"><xsl:value-of select="$msgType"/></xsl:attribute>
        <xs:complexType>
          <xs:sequence>
            <xsl:apply-templates/>
          </xs:sequence>
        </xs:complexType>
      </xs:element>
    </xs:schema>
  </xsl:template>

  <!-- Segment Group -->
  <xsl:template match="SegGroup">
  	    <xsl:if test="@Usage = 'R'">
     		<xsl:call-template name="handleSegGroup">
     		 </xsl:call-template>
   	 	</xsl:if>
   	 	<xsl:if test="@Usage = 'RE' or @Usage='O' or @Usage = 'C' or @Usage = 'CE'">
<!-- 			<xs:sequence minOccurs="0">-->
     	   	<xsl:call-template name="handleSegGroup">
      	  	</xsl:call-template>
<!--     	 	</xs:sequence> -->
  	  	</xsl:if>

<!--  	<xsl:if test="$groups = 'false'">
      <xsl:apply-templates select="SegGroup | Segment"/>
  	</xsl:if>-->

  </xsl:template>

  <xsl:template name="handleSegGroup">
  	<xsl:if test="$groups = 'true'">
  	  <xsl:if test="$xml = 'true'">
      <xs:element>
        <xsl:attribute name="name"><xsl:value-of select="$msgType"/>.<xsl:value-of select="@Name"/></xsl:attribute>
        <xsl:choose>
          <xsl:when test="@Usage = 'RE' or @Usage = 'C' or @Usage = 'CE' or @Usage = 'O'">
            <xsl:attribute name="minOccurs">0</xsl:attribute>
          </xsl:when>
          <xsl:when test="@Usage = 'R'">
            <xsl:attribute name="minOccurs">1</xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="@Max = '*'">
            <xsl:attribute name="maxOccurs">unbounded</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="maxOccurs"><xsl:value-of select="@Max"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
<!--        <xsl:attribute name="maxOccurs">unbounded</xsl:attribute> -->
        <xs:complexType>
        <xs:sequence>
        <xsl:apply-templates/>
        </xs:sequence>
        </xs:complexType>
      </xs:element>
     </xsl:if>
     </xsl:if>
    <xsl:if test="$groups = 'false'">
     <xsl:if test="$xml = 'false'">
  		<xs:sequence>
        <xsl:choose>
          <xsl:when test="@Usage = 'RE' or @Usage = 'C' or @Usage = 'CE' or @Usage = 'O'">
            <xsl:attribute name="minOccurs">0</xsl:attribute>
          </xsl:when>
          <xsl:when test="@Usage = 'R'">
            <xsl:attribute name="minOccurs">1</xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="@Max = '*'">
            <xsl:attribute name="maxOccurs">unbounded</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="maxOccurs"><xsl:value-of select="@Max"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
<!--        <xsl:attribute name="maxOccurs">unbounded</xsl:attribute> -->
        <xsl:apply-templates/>
        </xs:sequence>
     </xsl:if>
     </xsl:if>
  </xsl:template>

  <!-- Segment -->
  <xsl:template match="Segment">
    <xsl:if test="@Usage = 'R'">
      <xsl:call-template name="handleSegment">
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@Usage = 'RE' or @Usage='O' or @Usage = 'C' or @Usage = 'CE'">
<!--      <xs:sequence minOccurs="0"> -->
        <xsl:call-template name="handleSegment">
        </xsl:call-template>
<!--      </xs:sequence> -->
    </xsl:if>
  </xsl:template>

  <xsl:template name="handleSegment">
      <xs:element>
        <!-- Name of Segment -->
        <xsl:attribute name="name"><xsl:value-of select="@Name"/></xsl:attribute>
<!--        <xsl:attribute name="type"><xsl:value-of select="@Name"/>_type</xsl:attribute> -->
        <!-- Cardinality -->
        <xsl:choose>
          <xsl:when test="@Usage = 'RE' or @Usage = 'C' or @Usage = 'CE' or @Usage = 'O'">
            <xsl:attribute name="minOccurs">0</xsl:attribute>
          </xsl:when>
          <xsl:when test="@Usage = 'R'">
            <xsl:attribute name="minOccurs">1</xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="@Max = '*'">
            <xsl:attribute name="maxOccurs">unbounded</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="maxOccurs"><xsl:value-of select="@Max"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
<!--        <xsl:attribute name="maxOccurs">unbounded</xsl:attribute> -->
      </xs:element>
  </xsl:template>


  <!-- Removes text nodes -->
  <xsl:template match="text()" mode="type"/>
  <xsl:template match="text()"/>
</xsl:stylesheet>