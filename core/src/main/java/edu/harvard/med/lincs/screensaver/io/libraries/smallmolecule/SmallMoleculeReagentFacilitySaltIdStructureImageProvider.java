package edu.harvard.med.lincs.screensaver.io.libraries.smallmolecule;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.ui.arch.util.servlet.ImageProviderServlet;

public class SmallMoleculeReagentFacilitySaltIdStructureImageProvider extends SmallMoleculeReagentFacilitySaltIdGenericImageProvider implements StructureImageProvider<SmallMoleculeReagent>
{
  public SmallMoleculeReagentFacilitySaltIdStructureImageProvider(String baseUrl, ImageProviderServlet imageProviderServlet)
  {
    super(baseUrl, imageProviderServlet);
  }
}
