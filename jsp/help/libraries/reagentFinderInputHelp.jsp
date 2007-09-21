<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentFinderInputHelp">
	<f:verbatim escape="false">
    <div class="sectionHeader">Instructions:</div>
    <ul>
			<li>Select the vendor who provides the reagents for which you
			are searching.</li>
			<li>Type or paste (from the clipboard) the vendor identifiers.</li>
			<ul>
				<li><span style="font-size: smaller;"> Place each vendor
				identifier on a separate line. </span></li>
				<li><span style="font-size: smaller;"> Case sensitivity
				(uppercase versus lowercase) matters! </span></li>
				<li>Examples of vendor identifiers:
				<ul>
					<li>Asinex: <span class="example">ASN 05444522</span></li>
					<li>CEREP: <span class="example">S0010001~S0002790</span></li>
					<li>ChemBridge: <span class="example">100002</span></li>
					<li>ChemDiv: <span class="example">4965-0014</span></li>
					<li>Dharmacon: <span class="example">M-003000-01</span></li>
					<li>etc.</li>
				</ul>
				</li>
			</ul>
			<li>Click the "Find Reagents" button. (Or press the <span
				style="">Tab</span> key to move focus to the button, and then press
			<span style="">Spacebar</span> or <span style="">Enter</span>.)</li>
		</ul>
	</f:verbatim>
</f:subview>
