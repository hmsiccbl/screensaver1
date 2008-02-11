<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="annotationDataHelpText">
	<f:verbatim escape="false">
		<p>The Annotation Data section of the Study Viewer page allows you
		to browse all the annotation data in the study. The first column is
		always the source/vendor identifier. You can sort and browse the data
		in many different ways:
		<ul>
			<li>Navigate through the data using the first page, previous
			page, next page, and last page buttons.</li>
			<li>From the "Rows per page", select the number of rows you
			would like to view on the page.
			<li>To the right of the blue navigation buttons, you can enter
			in a data row number and click "Go" to view that data.</li>
			<li>Change the sort order by clicking on any of the column
			headers. Clicking once will do a forward sort, and clicking a second
			time on the same header will change to a reverse sort.</li>
			<li>View information about a reagent, including the gene or
			compound information for the reagent, by clicking on the reagent
			identifier.</li>
		</ul>
		</p>
		<p>You can choose to select a subset of the annotation types for
		viewing in both the Annotation Types and Annotation Data sections by
		using the "Show selected annotations" controls that appear above the
		Annotation Types section when either of the Annotation Types and
		Annotation Data sections is expanded. Just uncheck the checkboxes next
		to the data header names that you don't want to see, and click the
		"Update" button. As a shortcut, click the "All" button to show all the
		columns, or "First" to show only the first column.</p>
	</f:verbatim>
</f:subview>