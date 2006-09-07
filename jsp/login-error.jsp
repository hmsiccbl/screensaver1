<%@ include file="/headers.inc"%>

<f:subview id="login">
  <h:form id="loginErrorForm">
    <t:outputText value="Based upon the user ID and password you provided, we are not yet sure of who you are.  In keeping with our policy of protecting data from unauthorized users, we cannot give you the benefit of the doubt and let you into this system. Instead, we must ask that you try once again to tell us who you are." styleClass=""/>
    <t:panelGrid columns="1">
      <t:commandButton value="Try again" action="#{login.tryAgain}" styleClass="command"/>
      <t:commandLink value="Forgot your user ID or password?" action="#{login.forgotIdOrPassword}"
        styleClass="command" />
    </t:panelGrid>
  </h:form>
</f:subview>
