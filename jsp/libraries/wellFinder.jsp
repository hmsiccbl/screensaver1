<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="wellFinder">
	<h:form id="wellFinderForm">

		<t:buffer into="#{commandsBuffer}">
			<t:panelGroup id="commandPanel">
				<t:commandButton action="#{wellFinder.findWells}"
					id="findWellsSubmit" value="Find Wells" styleClass="command" />
				<t:commandButton action="#{wellFinder.findWellVolumes}"
					id="findWellVolumes" value="Find Well Volumes" rendered="#{wellFinder.editable}" styleClass="command" />
			</t:panelGroup>
		</t:buffer>

		<t:panelGrid columns="2" rowClasses="topAlignedPanelRow">
			<t:panelGrid columns="1">
				<t:outputText value="#{commandsBuffer}" escape="false" />
				<t:inputTextarea id="plateWellList"
					value="#{wellFinder.plateWellList}" styleClass="inputText"
					cols="50" rows="40" forceId="true"></t:inputTextarea>
				<t:outputText value="#{commandsBuffer}" escape="false" />
			</t:panelGrid>
			<%@ include file="../help/libraries/wellFinderInputHelp.jsp"%>
		</t:panelGrid>
	</h:form>
</f:subview>
