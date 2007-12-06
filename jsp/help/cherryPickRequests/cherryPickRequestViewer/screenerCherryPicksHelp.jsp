<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="screenerCherryPicksHelpText">
	<f:verbatim escape="false">
		<p>Screener Cherry Picks represent the wells/genes that the
		screener has requested for a follow-up screening.</p>
		<p>In the case of Dharmacon SMARTPool RNAi libraries, Screener
		Cherry Picks can be requested as either wells from pool library plates
		or as wells from duplex library plates. If pool library plate wells
		have been requested, each Screener Cherry Pick will "map" to multiple
		wells on the associated duplex library plates. The "Source Wells"
		column in the data table displays how many duplex wells will be cherry
		picked for a requested pool well. For SMARTPool libraries, this number
		should always be 4, and other values indicate library anomalies that
		should be investigated before proceeding with cherry pick plate
		creation. <span class="helpTip">Tip: </span>sort the "Source Wells" column to verify whether
		such anomalies exist, by viewing the sorted range of values.</p>
	</f:verbatim>
	<t:panelGroup visibleOnUserRole="cherryPickAdmin">
		<f:verbatim escape="false">
			<p>As a Cherry Pick Request Administrator viewing a newly created
			Cherry Pick Request, this section will allow you to add the
			screener's requested cherry picks. Type, or paste from the clipboard,
			the requested wells, formatted as described in the instructions on
			the page. Click the "Add Cherry Picks (Pool Wells)" button, if the
			wells represent pool wells and the screener wants to perform a
			follow-up screening on the associated duplex wells. Screensaver will
			automatically determine the duplex wells that need to be cherry
			picked and will create corresponding "Lab Cherry Picks" (see next
			section). However, if the screener wants to perform a follow-up
			screening on the pool wells themselves, or if the specified wells
			already represent duplex wells, click the "Add Cherry Picks" button.</p>
			<p>If the Screener Cherry Picks are determined to be in error for
			any reason, you can re-enter the requested wells by first clicking
			the "Delete All" button, and then re-adding them, as described above.
			The "Delete All" button will be disabled after the "Reserve Reagent"
			command (see "Lab Cherry Picks" section) has been invoked.</p>
		</f:verbatim>
	</t:panelGroup>
</f:subview>
