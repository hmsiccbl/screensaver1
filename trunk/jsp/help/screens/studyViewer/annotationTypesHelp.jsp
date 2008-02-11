<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="studyAnnotationTypesHelpText">
  <f:verbatim escape="false">
    <p>
      The Annotation Types section of the Study Viewer page summarizes the different annotation types
      provided by the study.
    </p>
    <p>The columns in the table contain the name and description of the
		annotation types, along with an indicator for whether the data
		associated with that annotation type is numeric.</p>
    <p>You can choose to select a subset of the annotation types for
		viewing in both the Annotation Types and Annotation Data sections by
		using the "Show selected annotations" controls that appear above the
		Annotation Types section (when either of the subsections are
		expanded). Just uncheck the checkboxes next to the annotation types
		that you don't want to see, and click the "Update" button. As a
		shortcut, click the "All" button to show all the columns, or "First"
		to show only the first column.</p>
  </f:verbatim>
</f:subview>