<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="cherryPickPlatesHelpText">
	<f:verbatim escape="false">
		<p>Once Lab Cherry Picks have been "mapped", the Cherry Pick
		Request will have one or more Cherry Pick Plates associated with it.
		Each Cherry Pick Plate can independently progress through the
		following statuses:
		<dl>
			<dt>Not Plated</dt>
			<dd>Screensaver has defined the plate and assigned Lab Cherry
			Picks to it. The Cherry Pick Plate does not yet exist as a tangible
			entity of the physical world.</dd>

			<dt>Plated</dt>
			<dd>The lab has physically created the Cherry Pick Plate (and a
			Cherry Pick Administrator has recorded this fact in Screensaver),
			which can be held, viewed, and admired by real people. Most
			importantly, it can be screened.</dd>

			<dt>Canceled</dt>
			<dd>The lab has decided to never physically create the Cherry
			Pick Plate (and a Cherry Pick Administrator has recorded this fact in
			Screensaver), releasing any reagent reserved by the Lab Cherry Picks
			assigned to the plate.</dd>

			<dt>Failed</dt>
			<dd>The lab was unable to succesfully create the Cherry Pick
			Plate, although the reagent reserved by the plate's Lab Cherry Picks
			was used (depleted wells on the cherry pick library copy plate). See
			the comments field to view attributed blame (see below).</dd>
		</dl>
		</p>
	</f:verbatim>
	<t:panelGroup visibleOnUserRole="cherryPickAdmin">
		<f:verbatim escape="false">
			<p>As a Cherry Pick Request Administrator, you are responsible
			for providing the lab (i.e., probably yourself!) with Cherry Pick
			Plate Mapping files, so that the Cherry Pick Plates can be created in
			the lab. Fortunately, Screensaver does all of the work for you! You
			merely need to download the Plate Mapping Files from Screensaver.</p>

			<p>Plate Mapping Files are fed directly to the plating machines in
			the lab, and specify how reagent from source wells (of cherry pick
			library copy plates) is to be transferred to each Cherry Pick Plate.
			If there is more than 1 cherry pick plate, you may download all or
			just a subset of the plate mapping files./<p>
			<p><span class="helpTip">Tip:</span> Downloading a subset of the
			Plate Mapping Files may be convenient if you intend to create subsets
			of Cherry Pick Plates at different times, or if you are reattempting
			the creation of a subset of plates that previously failed to be
			created.</p>
			<p>To download the files, select the desired rows in the Cherry Pick
			Plates data table, and then click "Download Files for Selected
			Plates". This will download a ZIP file, containing one mapping file
			per Cherry Pick Plate, plus an additional "README.txt" file.
			The README file lists the Cherry Pick Plates, along with their
			respective statuses. It also indicates whether particular Cherry Pick
			Plates require special treatment, such as when:<ul>
				<li>Cherry Pick Plates are to be created from the same source
				plate, requiring manual reloading of one or more source plates.</li>

				<li>Cherry Pick Plates are to be created from multiple source
				plates of non-uniform plate types, in which case a given Cherry Pick
				Plate will be specified across multiple mapping files.</li>
			</ul>
			</p>

			<p>As a Cherry Pick Request Administrator, you are responsible
			for updating the status of each Cherry Pick Plate. Depending upon the
			outcome of the Cherry Pick Plate creation process, you may record one
			or more Cherry Pick Plates as "Plated", "Canceled", or "Failed" by
			doing the following:
			<ol>
				<li>Select the Cherry Picks Plates in the data table by clicking on
				the respective checkbox in each row. <span class="helpTip">Tip: </span>The
				checkbox in the table header will select and deselect all of the
				rows with a single click of the mouse!</li>

				<li>Specify values for the "Performed By", "Date", and
				"Comments" fields, indicating who performed the plate creation, or
				made the decision to cancel the plate or record it as having failed.</li>

				<li>Click the appropriate command button: "Record Selected
				Plates as 'Plated'", "Record Selected Plates as 'Failed'", or
				"Cancel Selected Plates".
				</li>
			</ol>

			<span class="helpTip">Tip: </span>For each plate you record as 'Failed', a new "attempt" will be created for each Cherry Pick Plate, duplicating all of the Lab Cherry Picks on that
plate, maintaining the same layout, and reserving additional reagent.  However, it is possible that some (or all) of the newly created Lab Cherry Picks will be
unfulfilled, due to lack of reagent.  You can invoke the "New Cherry Pick Request for Unfulfilled" command, above, to handle these Lab Cherry Picks in the future, as an entirely new Cherry Pick Request.
    </p>
		</f:verbatim>
	</t:panelGroup>
</f:subview>
