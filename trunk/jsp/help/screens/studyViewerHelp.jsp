<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="studyViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Study Viewer page is divided into three major sections that you can collapse or expand by
      clicking on their headers.
    </p>
  </f:verbatim>
  <t:panelGroup rendered="#{empty inHelpViewer}">
    <f:verbatim escape="false">
      <p>
        The sections are:
        <ul>
          <li><a href="studyViewer/studyDetailsHelp.jsf">Study Details</a></li>
          <li><a href="studyViewer/annotationTypesHelp.jsf">Annotation Types</a></li>
          <li><a href="studyViewer/annotationDataHelp.jsf">Annotation Data</a></li>
        </ul>
        Click on a section name in the above list for help on that particular section.
      </p>
    </f:verbatim>
  </t:panelGroup>
</f:subview>