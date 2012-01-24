package edu.harvard.med.lincs.screensaver.io.screens;

import edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule.SmallMoleculeReagentFacilitySaltIdGenericImageLocator;
import edu.harvard.med.screensaver.io.screens.StudyImageLocator;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.util.UrlEncrypter;

public class SmallMoleculeReagentFacilitySaltIdStudyImageLocator extends SmallMoleculeReagentFacilitySaltIdGenericImageLocator implements StudyImageLocator<SmallMoleculeReagent>
{
  public SmallMoleculeReagentFacilitySaltIdStudyImageLocator(String baseUrl)
  {
    super(baseUrl);
  }
  
  public SmallMoleculeReagentFacilitySaltIdStudyImageLocator(String baseUrl, UrlEncrypter urlEncrypter)
  {
    super(baseUrl,urlEncrypter);
  }
}
