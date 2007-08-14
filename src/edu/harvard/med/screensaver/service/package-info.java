/**
Contains packages and classes that implement discrete, independent services that read and modify data in the Screensaver data model.  
Services are high-level "business" processes (well, lab processes, in our case).  Usually a business process will perform non-trivial 
manipulation of Screensaver data, and will usually interact with disparate parts of the data model.  These services are user 
interface-agnostic.

For basic input/output functions see the {@link edu.harvard.med.screensaver.io} package (e.g. importers and exporters).
*/
package edu.harvard.med.screensaver.service;