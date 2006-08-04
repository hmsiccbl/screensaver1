<%@ include file="headers.inc" %>

<f:subview id="body">
  <t:panelGrid columns="1">
    <t:outputText styleClass="title" style="text-align: center;" value="Welcome to #{main.applicationTitle}!" />
    <%@ include file="login.jsp" %>
  </t:panelGrid>
</f:subview>
