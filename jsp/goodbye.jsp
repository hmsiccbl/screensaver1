<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="goodbyeSubview">

	<h:form id="loginAgainForm">
		<t:commandButton id="loginAgain" action="#{mainController.viewMain}"
			value="Login again" style="command"
			title="Click this button to return to the login page" />
	</h:form>

</f:subview>
