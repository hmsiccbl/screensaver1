<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="screensAndResultViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Screen Viewer page is divided into five sections that you can collapse or expand by
      clicking on their headers.
      The final three sections are only shown when screen results are available for the screen.
    </p>
  </f:verbatim>
  <t:panelGroup rendered="#{empty inHelpViewer}">
    <f:verbatim escape="false">
      <p>
        The five sections are:
        <ul>
          <li><a href="screenViewer/screenDetailsHelp.jsf">Screen Details</a></li>
          <li><a href="screenViewer/screenResultsSummaryHelp.jsf">Screen Results Summary</a></li>
          <li><a href="screenViewer/dataHeadersHelp.jsf">Data Headers</a></li>
          <li><a href="screenViewer/dataHelp.jsf">Data</a></li>
          <li><a href="screenViewer/heatMapsHelp.jsf">Heat Maps</a></li>
        </ul>
        Click on a section name in the above list for help on that particular section.
      </p>
    </f:verbatim>
  </t:panelGroup>
</f:subview>