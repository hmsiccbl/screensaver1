<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="helpViewer">

  <h:form id="helpViewerForm">
    <t:panelGrid columns="1" width="100%">

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['wellFinderHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Finding Wells" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/wellFinderHelp.jsp" %>

        <t:div style="margin-left: 30px">
          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['wellFinderInputHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Entering Wells into the Well Finder" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/libraries/wellFinderInputHelp.jsp" %>
          </t:collapsiblePanel>
        </t:div>

      </t:collapsiblePanel>


      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['wellSearchResultsHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing Well Search Results" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/wellSearchResultsHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['wellVolumeSearchResultsHelp']}"
        var="isCollapsed"
        rendered="#{helpViewer.screensaverUser.isUserInRoleOfNameMap[\"librariesAdmin\"]}"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing Well Volume Search Results" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/wellVolumeSearchResultsHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['wellViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing A Well" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/wellViewerHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['compoundViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing a Compound" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/compoundViewerHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['geneViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing a Gene" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/geneViewerHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['librariesBrowserHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Browsing Libraries" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/librariesBrowserHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['libraryViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing a Library" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/libraries/libraryViewerHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['screensBrowserHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Browsing Screens" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <%@ include file="help/screens/screensBrowserHelp.jsp" %>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['screenViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing a Screen and its Results" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <t:aliasBean alias="#{inHelpViewer}" value="true">
          <%@ include file="help/screens/screenViewerHelp.jsp" %>
        </t:aliasBean>

        <t:div style="margin-left: 30px">

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['screenDetailsHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Screen Details" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/screenViewer/screenDetailsHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['screenResultsSummaryHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Screen Results Summary" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/screenViewer/screenResultsSummaryHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['dataHeadersHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Data Headers" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/screenViewer/dataHeadersHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['dataHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Data" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/screenViewer/dataHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['heatMapsHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Heat Maps" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/screenViewer/heatMapsHelp.jsp" %>
          </t:collapsiblePanel>
        </t:div>
      </t:collapsiblePanel>

      <t:collapsiblePanel
        value="#{helpViewer.isPanelCollapsedMap['cherryPickRequestViewerHelp']}"
        var="isCollapsed"
      >
        <f:facet name="header">
          <t:div styleClass="sectionHeader">
            <t:headerLink immediate="true" styleClass="sectionHeader">
              <h:graphicImage
                value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                styleClass="icon"
              />
              <h:outputText value="Viewing a Cherry Pick Request" styleClass="sectionHeader" />
            </t:headerLink>
          </t:div>
        </f:facet>
        <t:aliasBean alias="#{inHelpViewer}" value="true">
          <%@ include file="help/screens/cherryPickRequestViewerHelp.jsp" %>
        </t:aliasBean>

        <t:div style="margin-left: 30px">

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['cprvScreenSummaryHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Screen Summary" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/cherryPickRequestViewer/screenSummaryHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['cherryPickRequestSummaryHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Cherry Pick Request Details" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/cherryPickRequestViewer/cherryPickRequestDetailsHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['screenerCherryPicksHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Screener Cherry Picks" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/cherryPickRequestViewer/screenerCherryPicksHelp.jsp" %>
					</t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['labCherryPicksHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Lab Cherry Picks" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/cherryPickRequestViewer/labCherryPicksHelp.jsp" %>
          </t:collapsiblePanel>

          <t:collapsiblePanel
            value="#{helpViewer.isPanelCollapsedMap['cherryPickPlatesHelp']}"
            var="isCollapsed"
          >
            <f:facet name="header">
              <t:div styleClass="subsectionHeader">
                <t:headerLink immediate="true" styleClass="subsectionHeader">
                  <h:graphicImage
                    value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
                    styleClass="icon"
                  />
                  <h:outputText value="Cherry Pick Plates" styleClass="subsectionHeader" />
                </t:headerLink>
              </t:div>
            </f:facet>
            <%@ include file="help/screens/cherryPickRequestViewer/cherryPickPlatesHelp.jsp" %>
          </t:collapsiblePanel>
        </t:div>
      </t:collapsiblePanel>

    </t:panelGrid>
  </h:form>

</f:subview>
