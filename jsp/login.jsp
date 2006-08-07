<%@ include file="headers.inc"%>

<f:subview id="login">
  <h:form id="loginForm">
    <t:panelGrid columns="2" columnClasses="keyColumn,column">
      <t:outputText value="#{login.authenticationIdDescription}:" styleClass="inputLabel" />
      <t:inputText id="userIdInput" value="#{login.userId}" styleClass="input" />
      <t:outputText value="Password:" styleClass="inputLabel" />
      <t:inputSecret id="passwordInput" value="#{login.password}" styleClass="input" />
    </t:panelGrid>
    <t:panelGroup>
      <t:selectBooleanCheckbox id="disableAdminPrivilegesCheckbox"
        value="#{login.disableAdministrativePrivileges}" />
      <t:outputLabel for="disableAdminPrivilegesCheckbox"
        value="Disable my administrative privileges (if applicable)" 
        styleClass="input"/>
    </t:panelGroup>
    <t:div/>
    <t:panelGroup>
      <t:commandButton value="Login" action="#{login.login}" styleClass="command" />
      <t:commandLink value="Forgot your ID or password?" immediate="true"
        action="#{login.forgotIdOrPassword}" styleClass="command" />
    </t:panelGroup>
  </h:form>
</f:subview>
