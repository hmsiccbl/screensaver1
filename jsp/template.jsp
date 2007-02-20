<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%-- 
   Note!  Warning!  N.B.!  Achtung!  Read this first!  For Developers!  Hey you, yes YOU!
   
   Usage:
   
   - Content inserted by 'tiles:insert' tags must contain only JSF tags/content!  No HTML or text!
     See http://wiki.apache.org/myfaces/Tiles_and_JSF#head-03621bf81a046779a84ef978d6e0ebabdbe85010 
     
   - Content must be wrapped in <f:subview>...</f:subview>
   
--%>

<%-- Push component attributes defined in Tiles definitions into request context (scope="request" required for JSF integration) --%>
<tiles:importAttribute scope="request" name="pageTitle" />
<tiles:importAttribute scope="request" name="inputFocusId" />

<f:view>
  <t:document>
    <t:documentHead>
			<t:stylesheet path="/css/screensaver.css"/>
    </t:documentHead>

    <t:documentBody>
      <t:panelGrid id="menuAndBodyPanel" columns="2" styleClass="menuAndContent"
        columnClasses="menuColumn,contentColumn">
        <tiles:insert attribute="menu" flush="false" />
        <t:panelGrid id="bodyPanel" columns="1" style="width: 100%">
          <tiles:insert attribute="header" flush="false" />
          <tiles:insert attribute="body" flush="false" />
        </t:panelGrid>
      </t:panelGrid>

      <t:div styleClass="footer">
        <t:htmlTag value="hr"/>
        <h:outputText value="#{appInfo.applicationTitle} | "/>
        <h:outputLink value="http://iccb.med.harvard.edu">
          <h:outputText value="Harvard Medical School: ICCB-Longwood" />
        </h:outputLink>
        <h:outputText value=" | "/>
        <h:outputLink value="#{appInfo.feedbackUrl}">
          <h:outputText value="Feedback" />
        </h:outputLink>
      </t:div>

    </t:documentBody>
    
			<%-- HACK: provide a generic mechanism for setting the input element to have the initial focus --%>
			<%-- To specify the element having the initial input focus, define inputFocusId arg in tiles.xml for 
			     the page, where the value is the JSF ID of the input component (also, use forceId="true") --%>
			<t:jsValueSet name="jsInputFocusId" value="#{inputFocusId}"/>
			<f:verbatim>
				<script type="text/javascript">
        function setFocus(id) 
        {
          var elt = document.getElementById(id);
          if (elt && elt.focus) {
            elt.focus();
          }
        }
        setFocus(jsInputFocusId);
        </script>
			</f:verbatim>
    
  </t:document>
</f:view>

