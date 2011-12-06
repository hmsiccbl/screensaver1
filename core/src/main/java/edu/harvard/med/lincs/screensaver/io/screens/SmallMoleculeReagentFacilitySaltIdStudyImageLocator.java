package edu.harvard.med.lincs.screensaver.io.screens;

import edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule.SmallMoleculeReagentFacilitySaltIdGenericImageLocator;
import edu.harvard.med.screensaver.io.screens.StudyImageLocator;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

public class SmallMoleculeReagentFacilitySaltIdStudyImageLocator extends SmallMoleculeReagentFacilitySaltIdGenericImageLocator implements StudyImageLocator<SmallMoleculeReagent>
{
  public SmallMoleculeReagentFacilitySaltIdStudyImageLocator(String baseUrl)
  {
    super(baseUrl);
  }
}
