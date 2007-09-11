// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.Arrays;
import java.util.List;

import edu.harvard.med.screensaver.model.screenresults.AnnotationType;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class AnnotationTypesTable extends MetaDataTable<AnnotationType>
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationTypesTable.class);

  // TODO: consider replacing DataHeaderRowDefinition with TableColumn<AnnotationType>
  private final List<MetaDataTableRowDefinition<AnnotationType>> ANNOTATION_TYPE_ROW_DEFINTIIONS =
    Arrays.asList
    (
     new MetaDataTableRowDefinition<AnnotationType>("description", "Description", "A description of the data header"),
     new MetaDataTableRowDefinition<AnnotationType>("numeric", "Numeric", "Whether the annotation type has numeric data")
    );

  @Override
  protected List<MetaDataTableRowDefinition<AnnotationType>> getMetaDataTableRowDefinitions()
  {
    return ANNOTATION_TYPE_ROW_DEFINTIIONS;
  }

  // instance data members

  // public constructors and methods

  // private methods

}

