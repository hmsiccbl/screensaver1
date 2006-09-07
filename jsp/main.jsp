<%@ include file="/headers.inc"%>

<f:subview id="main">
  <t:outputText value="Welcome #{menu.userPrincipalName}!" styleClass="title"/>
  <t:div/>
  <t:outputText value="Please use the navigation menu, to the left, to start using #{menu.applicationName}"/>
</f:subview>
