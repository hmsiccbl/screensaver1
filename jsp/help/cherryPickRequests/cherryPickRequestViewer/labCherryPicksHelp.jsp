<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="labCherryPicksHelpText">
	<f:verbatim escape="false">
		<p>A Lab Cherry Pick represents the physical withdrawal of
		reagent, from a particular well of a cherry pick library plate copy.
		Lab Cherry Picks track the lab's effort of creating new cherry pick
		plates for the screener, and allow Screensaver to determine remaining
		volumes in library plate copies. Each Lab Cherry Pick has an
		associated "status" that tracks the following conditions:
		<dl>
			<dt>Unfulfilled</dt>
			<dd>The cherry pick library plate copy from which the reagent
			will be drawn has <i>not yet</i> been determined (it is possible that
			no copy will have sufficient volume to satisfy the Lab Cherry Pick)</dd>

			<dt>Reserved</dt>
			<dd>The cherry pick library plate copy from which the reagent
			will be drawn <i>has</i> been determined; Screensaver has reserved
			the necessary volume, such that subsequent Cherry Pick Requests
			cannot will only be allowed to use any additional remaining reagent
			volume in that well (regardless of whether the Lab Cherry Pick well
			ends up being physically depleted).</dd>

			<dt>Mapped</dt>
			<dd>The Lab Cherry Pick has been assigned to a particular plate
			and destination well in the set of new cherry pick plates that are to
			be created for this Cherry Pick Request. The "Cherry Pick Plate #"
			column displays the cherry pick plate number to which it has been
			mapped.</dd>

			<dt>Canceled</dt>
			<dd>The cherry pick plate to which this Lab Cherry Pick was
			mapped has been canceled, meaning that the lab has decided to never
			produce the cherry pick plate. The Lab Cherry Pick's reserved reagent
			volume has been released for future use by another Cherry Pick
			Request.</dd>

			<dt>Plated</dt>
			<dd>The cherry pick plate to which this Lab Cherry Pick was
			mapped has been physically created. The Lab Cherry Pick's reserved
			reagent volume has been <i>used</i>, never to be seen again.</dd>

			<dt>Failed</dt>
			<dd>The cherry pick plate to which this Lab Cherry Pick was
			mapped failed to be created succesfully. However, the Lab Cherry
			Pick's reserved reagent volume has been <i>used</i> , never to be
			seen again (likely having met the working end of a lab mop or paper
			towel). The "Attempt #" column displays the plate-creation attempt
			(1, 2, 3, ...) of the cherry pick plate number to which this Lab
			Cherry Pick has been mapped. Note that a given Lab Cherry Pick is
			associated with both a cherry pick plate number <i>and</i> and an
			attempt number.</dd>
		</dl>
		</p>

		<p>A single Screener Cherry Pick may produce <i>multiple</i> Lab
		Cherry Picks as follows:
		<ul>
			<li>If the Screener Cherry Pick represents a Dharmacon SMARTPool
			well, 4 Lab Cherry Picks will created, 1 for each silencing reagent
			targeting the gene in the associated pool well.</li>

			<li>If the lab fails to successfully create a cherry pick plate
			and records the cherry pick plate as "failed" (see below), all of the
			Lab Cherry Picks mapped to the plate will be marked as "failed", and
			a new set of duplicated Lab Cherry Picks will be created for that
			plate.</li>
		</ul>
		</p>
	</f:verbatim>
	<t:panelGroup visibleOnUserRole="cherryPickAdmin">
		<f:verbatim escape="false">
			<p>As a Cherry Pick Request Administrator, the following commands
			will be available:
			<dl>
				<dt>View Well Volumes</dt>
				<dd>Show the volume information for all Lab Cherry Pick source wells.  
				This can be used to determine if the "Reserve Reagent" command (below) 
				will be able to fulfill all Lab Cherry Picks.</dd>

				<dt>Reserve Reagent</dt>
				<dd>For each unfulfilled Lab Cherry Pick, have Screenasver
				determine the cherry pick library plate copy from which reagent will
				be reserved, and ultimately withrawn from. If the "Approved Volume"
				field has not yet been specified, a reminder message will be
				displayed prompting you to enter it. After reserving reagent, note
				that the "Source Copy" column of the "Lab Cherry Picks" data table
				is populated with values. It is possible, however, that some or all
				Lab Cherry Picks were unfulfillable (their status will remain
				"unfulfilled" and the "Source Copy" value will be blank). This
				occurs for any Lab Cherry Pick well for which no <i>single</i>
				library plate copy has sufficient reagent volume to satisfy the
				requested volume. See "New Cherry Pick Request for Unfulfilled"
				command, below. After the "Reserve Reagent" command is invoked, it
				becomes disabled if at least one Lab Cherry Pick was succesfully
				"reserved".</dd>

				<dt>Cancel Reservation</dt>
				<dd>This is effectively an "undo" command for the "Reserve
				Reagent" command.. All Lab Cherry Picks will revert to the
				"unfulfilled" status. <span class="helpTip">Tip: </span>Use "Cancel Reservation" to allow
				for another attempt at reserving reagent; this makes sense to do if
				1) new cherry pick library plate copies are created by the lab, or
				2) another Cherry Pick Request with "reserved" Lab Cherry Picks is
				competing for the reagent from the same wells as this Cherry Pick
				Request, and you would like to have this Cherry Pick Request be
				given the reagent (perform "Cancel Reservation" on both Cherry Pick
				Requests, and then invoke "Reserve Reagent" once more on this Cherry
				Pick Request).</dd>

				<dt>Map to Plates</dt>
				<dd>After "Reserve Reagent" has been run, you will be able to
				run the "Map to Plates" command. This will create the necessary
				number of cherry pick plates and will assign each Lab Cherry Pick to
				a particular well on a particular cherry pick plate. If the "Random
				plate well layout" checkbox has been selected, the Lab Cherry Picks
				will be assigned randomly to wells on the plate to which they have
				been mapped; otherwise they are assigned in order, filling columns
				left-to-right. <i>Note: This command cannot be undone! Once
				cherry pick plates are created, they are permanently created.</i> You
				may however, "cancel" these plates via the "Cancel Selected Plates"
				command (see below).</dd>

				<dt>New Cherry Pick Request for Unfulfilled</dt>
				<dd>After reserving reagent, if some Lab Cherry Picks remain
				unfulfilled, you may choose to create an entirely new Cherry Pick
				Request, containing only the unfulfilable Lab Cherry Picks. The
				intention is that these Lab Cherry Picks will be handled later, as a
				separate request, once additional cherry pick library plate copies
				have been created (or additional reagent otherwise becomes newly
				available). If invoked, the newly created Cherry Pick Request will:

				<ul>
					<li>belong to the same Screen as the current Cherry Pick
					Request
					<li>have the current date as its "Date Requested"
					<li>have the same values for the "Requested Volume", "Approved
					volume", "Approved By", "Date Volume Approved", "Random plate well
					layout", "Empty columns on plate" fields
					<li>have a comment indicating that it was created to handle
					unfulfilled cherry picks from the current Cherry Pick Request (the
					number will be specified).</li>
				</ul>

				<span class="helpTip">Tip: </span>Wait until the lab has successfully created as many
				cherry pick plates as possible before invoking this command.
				Consider that if the creation of one more cherry pick plates fail in
				the lab, new Lab Cherry Picks will be created by Screensaver, but
				may be found unfulfillable if the failed plating attempts consumed
				the last of the reagent for particular wells. Waiting until the end
				of the entire Cherry Pick Request process ensures that only a
				single, new Cherry Pick Request will encompass <i>all</i> of the
				unfulfilled Lab Cherry Picks for future processing.</dd>

				<dt>Show failed</dt>
				<dd>Click this checkbox if you want to view the Lab Cherry
				Picks that are associated with failed cherry pick plates. Normally,
				it is not important to see these, but curious individuals may oblige
				themselves of the opportunity.</dd>
			</dl>

			</p>
		</f:verbatim>
	</t:panelGroup>
</f:subview>
