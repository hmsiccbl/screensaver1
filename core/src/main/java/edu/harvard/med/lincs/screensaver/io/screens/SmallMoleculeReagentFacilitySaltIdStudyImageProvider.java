package edu.harvard.med.lincs.screensaver.io.screens;

import edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule.SmallMoleculeReagentFacilitySaltIdGenericImageProvider;
import edu.harvard.med.screensaver.io.screens.StudyImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

public class SmallMoleculeReagentFacilitySaltIdStudyImageProvider extends SmallMoleculeReagentFacilitySaltIdGenericImageProvider implements StudyImageProvider<SmallMoleculeReagent>
{
  public SmallMoleculeReagentFacilitySaltIdStudyImageProvider(String baseUrl)
  {
    super(baseUrl);
  }
}
