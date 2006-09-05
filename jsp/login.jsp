<%@ include file="headers.inc"%>

<f:subview id="login">
  <%-- To invoke the Servlet container's /j_security_check servlet (instead of FacesServlet), we have to invoke some Javascript to override JSF behavior --%>
  <h:form id="loginForm" onsubmit="javascript:document.forms['login:loginForm'].action='j_security_check'">
    <t:outputText value="If you are a screener, you may use your eCommons ID and password to login."/>
    <t:panelGrid columns="2" columnClasses="keyColumn,column">
      <t:outputText value="#{login.authenticationIdDescription}:" styleClass="inputLabel" />
      <t:inputText id="j_username" forceId="true" styleClass="input" />
      <t:outputText value="Password:" styleClass="inputLabel" />
      <t:inputSecret id="j_password" forceId="true" styleClass="input" />
    </t:panelGrid>
    <t:div/>
    <t:panelGroup>
      <t:commandButton value="Login" styleClass="command" />
      <t:commandLink value="Forgot your ID or password?" immediate="true"
        action="#{login.forgotIdOrPassword}" styleClass="command" />
    </t:panelGroup>
  </h:form>
  
</f:subview>
