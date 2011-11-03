package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.iccbl.screensaver.io.ImageProvider;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

public class RestrictedStructureImageProviderDecorator implements StructureImageProvider
{
  private static final Logger log = Logger.getLogger(RestrictedStructureImageProviderDecorator.class);

  private ImageProvider<SmallMoleculeReagent> _delegate;

  public RestrictedStructureImageProviderDecorator(ImageProvider<SmallMoleculeReagent> delegate)
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
