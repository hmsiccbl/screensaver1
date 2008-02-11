<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<tiles:importAttribute scope="request" name="pageTitle" ignore="true" />

<f:view>
  <t:document>
    <t:documentHead>
			<t:stylesheet path="/css/screensaver.css" />
			<t:htmlTag value="title">
        <h:outputText value="#{appInfo.applicationTitle}" />
			  <t:outputText value=" - #{pageTitle}" rendered="#{! empty pageTitle}" />
			</t:htmlTag>
    </t:documentHead>

    <t:documentBody>
      <t:panelGrid columns="1">
        <t:div styleClass="sectionHeader">
          <t:outputText value="#{pageTitle}" styleClass="sectionHeader" rendered="#{! empty pageTitle}" />
        </t:div>
        <tiles:insert attribute="body" flush="false" />
      </t:panelGrid>
    </t:documentBody>
  </t:document>
</f:view>

