// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens.rnaiglobal;

import java.util.Set;

import jxl.Cell;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;

class AnnotationValueBuilderImpl implements AnnotationValueBuilder
{
  private int _sourceColumnIndex;
  private AnnotationType _annotationType;
  private LibrariesDAO _librariesDao;

  public AnnotationValueBuilderImpl(int sourceColumnIndex,
                                    AnnotationType annotationType,
                                    LibrariesDAO librariesDao)
  {
    _sourceColumnIndex = sourceColumnIndex;
    _annotationType = annotationType;
    _librariesDao = librariesDao;
  }

  public void addAnnotationValue(Cell[] row)
  {
    String value = transformValue(row[_sourceColumnIndex].getContents());
    ReagentVendorIdentifier reagentVendorIdentifier = new ReagentVendorIdentifier(DHARMACON_VENDOR_NAME,
                                                                                  row[0].getContents());
    Set<Reagent> reagents = _librariesDao.findReagents(reagentVendorIdentifier, true);
    if (reagents.isEmpty()) {
      throw new DataModelViolationException("no such reagent " + reagentVendorIdentifier);
    }
    for (Reagent reagent : reagents) {
      _annotationType.createAnnotationValue(reagent, value);
    }
  }

  public AnnotationType getAnnotationType()
  {
    return _annotationType;
  }

  public String transformValue(String value)
  {
    return value;
  }
}
