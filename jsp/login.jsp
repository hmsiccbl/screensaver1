<%@ include file="headers.inc"%>

<f:subview id="login">
  <%-- To invoke the Servlet container's /j_security_check servlet (instead of FacesServlet), we have to invoke some Javascript to override JSF behavior --%>
  <h:form id="loginForm" onsubmit="javascript:document.forms['login:loginForm'].action='j_security_check'">
    <t:panelGrid columns="2" columnClasses="keyColumn,column">
      <t:outputText value="#{login.authenticationIdDescription}:" styleClass="inputLabel" />
      <t:inputText id="j_username" forceId="true" value="#{login.userId}" styleClass="input" />
      <t:outputText value="Password:" styleClass="inputLabel" />
      <t:inputSecret id="j_password" forceId="true" value="#{login.password}" styleClass="input" />
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
      <t:commandButton value="Login" styleClass="command" />
      <t:commandLink value="Forgot your ID or password?" immediate="true"
        action="#{login.forgotIdOrPassword}" styleClass="command" />
    </t:panelGroup>
  </h:form>
  
</f:subview>
