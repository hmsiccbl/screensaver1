<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="cherryPickRequestDetailsHelpText">
	<f:verbatim escape="false">
		<p>The Cherry Pick Request Details section of the Cherry Pick Request
		Viewer page displays detailed information about the Cherry Pick Request.
		Position your mouse over any of the headers in the left-hand column
		for a description of what that field contains.</p>

		<p>The Download button allows you to download an Excel spreadsheet
		containing the Screener Cherry Picks and Lab Cherry Picks data. The
		contents of the spreadsheet will mirror the data displayed in the data
		tables of the "Screener Cherry Picks" and "Lab Cherry Picks" sections,
		but will also include the sequences for the silencing reagents of each
		well.</p>
	</f:verbatim>
	<t:panelGroup visibleOnUserRole="cherryPickAdmin">
		<f:verbatim escape="false">
			<p>As a Cherry Pick Request Administrator, an "Edit"
			button will be available and when clicked will allow you to edit
			appropriate fields of the Cherry Pick Request. Note that the
			following fields will not be editable after the "Reserve Reagent"
			command (see "Lab Cherry Picks" section) has been invoked: "Requested
			Volume", "Approved Volume", "Random plate well layout", "Empty
			columns on plate".</p>

			<p>As a Cherry Pick Request Administrator, a "Delete" button will
			also be available if the Cherry Pick Request has no Screener Cherry
			Picks or only has Lab Cherry Picks that are "unfulfilled" (see
			below). Clicking "Delete" will delete the entire Cherry Pick Request,
			and cannot be undone. Fortunately, you will be prompted once to
			confirm your choice before the deletion occurs.</p>
		</f:verbatim>
	</t:panelGroup>
</f:subview>
