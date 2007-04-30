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

	<h:form id="reportErrorForm">
		<t:panelGrid>
			<t:outputText
				value="A mysterious and untoward error has occurred, which was not your fault."
				styleClass="errorMessage"
				title="You are blameless" />
			<t:outputText
				value="(On second thought, YOU are a developer, so it probably WAS your fault.  Back to work!)"
				visibleOnUserRole="developer"
				title="You can't do anything right" />
			<t:outputText
				value="You may go back and try again, though the error will probably reoccur.  If it does, we recommend you click the &quot;Login Again&quot; button, below, and then retry the operation.  We sincerely apologize for the inconvenience."
				escape="true"
				title="If you are really upset about it, try the 'Feedback' link down at the very bottom" />
			<h:panelGroup>
				<h:commandButton id="tryAgain" value="Back"
					style="font-size: large; font-weight: bold"
					onclick="javascript:history.back()" disabled="true" />
				<h:commandButton id="loginAgain"
					action="#{exceptionReporter.loginAgain}" value="Login Again"
					style="font-size: large; font-weight: bold" />
			</h:panelGroup>
		</t:panelGrid>

		<t:panelGrid visibleOnUserRole="developer">
			<t:dataList id="exceptionTable"
				value="#{exceptionReporter.throwablesDataModel}" var="throwableInfo"
				layout="unorderedList">
				<%--t:collapsiblePanel value="true">
				<f:facet name="header"--%>
				<t:outputText value="#{throwableInfo.nameAndMessage}"
					styleClass="stackTraceException" />
				<%--/f:facet--%>
				<t:dataTable id="stackTraceTable"
					value="#{throwableInfo.stackTraceDataModel}" var="item"
					styleClass="standardTable">
					<t:column>
						<t:outputText id="stackTraceDetail" value="#{item.second}"
							styleClass="#{item.first ? \"highlightedStackTraceDetail\" : \"stackTraceDetail\"}" />
					</t:column>
				</t:dataTable>
				<%--/t:collapsiblePanel--%>
			</t:dataList>
		</t:panelGrid>
	</h:form>

</f:subview>
