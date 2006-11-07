<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>


<f:subview id="reportError">

	<t:panelGrid>
		<t:outputText value="An unexpected error has occurred."
			styleClass="errorMessage" />
		<t:outputText
			value="You have been automatically logged out and must log back in now to resume using #{exceptionReporter.applicationName}."
			styleClass="errorMessage" />
		<t:outputText
			value="We apologize for this most unfortunate occurrence."
			styleClass="errorMessage" />
		<t:outputText
			value="Rest assured, the developers have been notified and are hard at work trying to fix the problem right now! (well, soon anyway)."
			styleClass="errorMessage" />
		<h:outputLink value="/screensaver/main.jsf">
			<t:outputText value="Login Again"
				style="font-size: large; font-weight: bold" />
		</h:outputLink>
	</t:panelGrid>

	<t:panelGrid visibleOnUserRole="developer">
		<t:dataList value="#{exceptionReporter.throwablesDataModel}"
			var="throwableInfo" layout="unorderedList">
			<%--t:collapsiblePanel value="true">
				<f:facet name="header"--%>
					<t:outputText value="#{throwableInfo.nameAndMessage}"
						styleClass="stackTraceException" />
				<%--/f:facet--%>
			<t:dataTable value="#{throwableInfo.stackTraceDataModel}" var="item"
				styleClass="standardTable">
				<t:column>
					<t:outputText value="#{item.second}"
						styleClass="#{item.first ? \"highlightedStackTraceDetail\" : \"stackTraceDetail\"}" />
				</t:column>
			</t:dataTable>
			<%--/t:collapsiblePanel--%>
		</t:dataList>
	</t:panelGrid>

</f:subview>
