package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.DataModelViolationException;

public class AssayWellControlTypeException extends DataModelViolationException
{
  public AssayWellControlTypeException(WellKey wellKey)
  {
    super("Well " + wellKey + " must be 'empty' or 'DMSO' to be used as an assay control");
  }
}
