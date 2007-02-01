<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="downloads">

  <f:verbatim escape="false">

<h2>SD Files for the Compound Libraries</h2>

<p>
The ICCB-Longwood compound libraries are bundled in two ways: A zip of
a single SD File containing all the libraries; and a zip of a
collection of SD Files, broken up by library. Use the former if you
want to search against all of the ICCB-Longwood compound libraries,
and the latter to search against specific libraries.
</p>

<p style="color: fuchsia;">
Please note that we do not have SD Files for every ICCB-L Compound Library. In
particular, there are no structures available for natural product extracts.
</p>

<div style="margin-left: 20px;">
<h3>Actively Screened Compound Libraries</h3>
<p>
A collection of libraries currently available for screening, containing:
ActiMolTimTec1; BiomolICCB1-2; Bionet1-2;
CBMicro; CEREP; ChemBridge3; ChemDiv1-5; ChemDivAM; Enamine1; IFLab1-2;
Maybridge1-5; MixCom1-5; NINDS; Peakdale1-2; Prestwick1.
</p>
<ul>
<li>
<b><a href="downloads/active-compound-libraries.zip">active-compound-libraries.zip</a></b>:
A zip of a collection of SD Files, broken up by library.
</li>
<li>
<b><a href="downloads/active-compound-libraries-sdf.zip">active-compound-libraries-sdf.zip</a></b>:
A zip of a single SD File containing all the libraries.
</li>
</ul>
</div>

<div style="margin-left: 20px;">
<h3>Complete Compound Library Set</h3>
<p>
A collection of all ICCB-L libaries. This includes all the active screening libraries
above, plus the following retired libraries:
CBDivE; CDS1; ICCBBio1; Prestwick_MMRF; Specplus.
</p>
<ul>
<li>
<b><a href="downloads/complete-compound-libraries.zip">complete-compound-libraries.zip</a></b>:
A zip of a collection of SD Files, broken up by library.
</li>
<li>
<b><a href="downloads/complete-compound-libraries-sdf.zip">complete-compound-libraries-sdf.zip</a></b>:
A zip of a single SD File containing all the libraries.
</li>
</ul>
</div>

<div style="margin-left: 20px;">
<h3>Instructions for Similarity Searching using ChemFinder</h3>

<p>
SDF file downloads of the ICCB-Longwood compound libraries are
provided for your convenience, particularly in performing various
searches. For instance, to do similarity searching in ChemFinder Ultra
9.0 (other versions of ChemFinder should be similar):
</p>

<ol>
<li>Open ChemFinder</li>

<li>Open the File Menu</li>
<li>Click on Database...</li>
<li>Click on Create Database...</li>
<li>Choose a filename for your database</li>
<li>Click on OK</li>
<li>Open the File Menu</li>
<li>Open the Import Submenu</li>
<li>Click on SDFile...</li>
<li>Select the SDFile you want to search against</li>

<li>Wait for input file scanning to complete (this may take a while)</li>
<li>Click on Import</li>
<li>Wait for import to complete (this may take a while too)</li>
</ol>

You are now ready to search against the structures in your SDFile. Use
the options in the Search menu to construct and perform your
search. (You can read the tutorial to find out more about searching -
select Tutorial from the Help Menu.)

</p>

  </f:verbatim>
  
</f:subview>
