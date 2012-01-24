package edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.util.UrlEncrypter;

public class SmallMoleculeReagentFacilitySaltIdStructureImageLocator extends SmallMoleculeReagentFacilitySaltIdGenericImageLocator implements StructureImageLocator
{
  public SmallMoleculeReagentFacilitySaltIdStructureImageLocator(String baseUrl)
  {
    super(baseUrl);
  }
  
  public SmallMoleculeReagentFacilitySaltIdStructureImageLocator(String baseUrl, UrlEncrypter urlEncrypter)
  {
    super(baseUrl,urlEncrypter);
  }
}
