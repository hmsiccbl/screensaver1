<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<tiles:importAttribute scope="request" name="pageTitle" ignore="true" />
<tiles:importAttribute scope="request" name="pageHelpURL" ignore="true" />

<f:subview id="headerSubview">

  <h:form id="headerForm">
    <t:panelGroup rendered="#{! empty pageTitle}">

      <t:outputText styleClass="title" value="#{pageTitle}" />

			<t:jsValueSet name="pageHelpURL" value="#{pageHelpURL}"/>
      
      <t:graphicImage
        id="headerSubviewHelpIcon"
        forceId="true"
        alt="help icon"
        url="/images/help.png"
        title="Click here to open help for this page in a separate window"
        styleClass="helpIcon"
        onmouseover="getElementById('headerSubviewHelpIcon').className='helpIconHover';"
        onmouseout="getElementById('headerSubviewHelpIcon').className='helpIcon';"
        onclick="window.open(pageHelpURL,pageHelpURL,'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars=1,height=450,width=650');"
        rendered="#{! empty pageHelpURL}"
      />

    </t:panelGroup>
  </h:form>
  
  <h:messages id="allMessages" globalOnly="false" showDetail="true" styleClass="errorMessage" />
</f:subview>