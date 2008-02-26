<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>


<f:subview id="login">
  <%-- To invoke the Servlet container's /j_security_check servlet (instead of FacesServlet), we have to invoke some Javascript to override JSF behavior --%>
  <h:form id="loginForm" onsubmit="javascript:document.forms['login:loginForm'].action='j_security_check'">
    <t:outputText value="If you are a screener, you may use your eCommons ID and password to login."/>
    <t:panelGrid columns="2" columnClasses="keyColumn,column">
      <t:outputText value="User&nbsp;ID:" styleClass="inputText" escape="false"
        title="Enter your eCommons ID or Screensaver login ID here"
      />
      <t:inputText id="j_username" forceId="true" styleClass="inputText" />
      <t:outputText value="Password:" styleClass="label"
        title="Enter your password here"
      />
      <t:inputSecret id="j_password" forceId="true" styleClass="inputText" />
			<s:focus id="focuser" for="j_username" />
    </t:panelGrid>
    <t:div/>
    <f:verbatim>
			<p><font size="-2">Confidentiality Agreement: The information in
			this database is confidential and is to be shared only among members
			of the ICCB-Longwood and NSRB screening communities. By logging into
			this database, I am agreeing to hold in confidence all information
			that I learn, download, or print until the information is publicly
			available. Thus, deposition of information into this database does
			not constitute a public disclosure and those who deposit information,
			including myself, can preserve their ability to publish and patent
			the results of their work if they so choose.</font></p>
		</f:verbatim>
    <t:panelGroup>
      <t:commandButton value="Login" styleClass="command"
        title="Once you have entered your username and password, and read the Confidentiality Agreement, click this button to log in"
      />
      <%--t:commandLink value="Forgot your ID or password?" immediate="true"
        action="#{menu.forgotIdOrPassword}" styleClass="command" /--%>
    </t:panelGroup>
    
  </h:form>
  
</f:subview>
