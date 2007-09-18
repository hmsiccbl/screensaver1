<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentFinder">
	<h:form id="reagentFinderForm">

		<t:buffer into="#{commandsBuffer}">
			<t:panelGroup id="commandPanel">
				<t:commandButton action="#{reagentFinder.findReagents}"
					id="findReagentsSubmit" value="Find Reagents" styleClass="command" />
			</t:panelGroup>
		</t:buffer>

		<t:panelGrid columns="2">
			<t:panelGrid columns="1">
				<t:panelGroup>
					<t:outputLabel for="vendorMenu" value="Vendor:" styleClass="label" />
					<t:selectOneMenu id="vendorMenu"
						value="#{reagentFinder.vendorSelector.value}"
						styleClass="inputText">
						<f:selectItems value="#{reagentFinder.vendorSelector.selectItems}" />
					</t:selectOneMenu>
					<t:outputText value="#{commandsBuffer}" escape="false" />
				</t:panelGroup>
				<t:inputTextarea id="reagentVendorIdentifierList"
					value="#{reagentFinder.reagentVendorIdentifierList}"
					styleClass="inputText" cols="50" rows="40" forceId="true" />
				<t:outputText value="#{commandsBuffer}" escape="false" />
			</t:panelGrid>
			<t:outputText value="Help text coming soon!" />
			<%--@ include file="../help/libraries/reagentFinderInputHelp.jsp"--%>
		</t:panelGrid>
	</h:form>
</f:subview>
