package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.image.ImageLocator;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

public class RestrictedStructureImageLocatorDecorator implements StructureImageLocator
{
  private static final Logger log = Logger.getLogger(RestrictedStructureImageLocatorDecorator.class);

  private ImageLocator<SmallMoleculeReagent> _delegate;

  public RestrictedStructureImageLocatorDecorator(ImageLocator<SmallMoleculeReagent> delegate)
  {
    _delegate = delegate;
  }

  @Override
  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    reagent = (SmallMoleculeReagent) reagent.restrict();
    if (reagent == null ||
      // if any structure-related property is restricted, then so should the image be restricted
      reagent.getSmiles() == null) {
      log.info("restricting access to " + reagent);
      return null;
    }
    return _delegate.getImageUrl(reagent);
  }
}
