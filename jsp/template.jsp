<%@ include file="headers.inc" %>

<%-- 
   Note!  Warning!  N.B.!  Achtung!  Read this first!  For Developers!  Hey you, yes YOU!
   
   Usage:
   
   - Content inserted by 'tiles:insert' tags must contain only JSF tags/content!  No HTML or text!
     See http://wiki.apache.org/myfaces/Tiles_and_JSF#head-03621bf81a046779a84ef978d6e0ebabdbe85010 
     
   - Content must be wrapped in <f:subview>...</f:subview>
   
   - Content will need "<%@ include file="headers.inc" %>" as first line
--%>


<f:view>
  <t:document>
    <t:documentHead>
      <f:verbatim>
        <meta http-equiv="Content-Type" content="text/html;CHARSET=iso-8859-1" />
        <title>Title</title>
        <link rel="stylesheet" type="text/css" href="css/screensaver.css" />
      </f:verbatim>
    </t:documentHead>

    <t:documentBody>
      <t:panelGrid id="menuAndBodyPanel" columns="2" style="table-layout: fixed"
        columnClasses="menuColumn,contentColumn">
        <tiles:insert attribute="menu" flush="false" />
        <tiles:insert attribute="body" flush="false" />
      </t:panelGrid>

      <t:div style="text-align: center">
        <t:htmlTag value="hr"/>
        <h:outputText value="Screensaver 1.0 | "/>
        <h:outputLink value="http://iccb.med.harvard.edu">
          <h:outputText value="Harvard Medical School: ICCB-Longwood" />
        </h:outputLink>
      </t:div>

    </t:documentBody>
  </t:document>
</f:view>

