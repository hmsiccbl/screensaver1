
package edu.harvard.med.screensaver.rest;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Publication;

public class WellConverter extends RestConverter
{
  private static final Logger log = Logger.getLogger(WellConverter.class);

  @Autowired
  private LibrariesDAO librariesDao;

  @Autowired
  private StructureImageProvider structureImageProvider;

  public boolean canConvert(Class clazz)
  {
    return Well.class.isAssignableFrom(clazz);
  }

  public void marshal(final Object value, final HierarchicalStreamWriter writer, MarshallingContext context)
  {
    final XStreamUtil util = new XStreamUtil(writer, context, getEntityUriGenerator());

    getDao().doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Well well = (Well) value;
        well = getDao().reloadEntity(well);
        Reagent reagent = ((Reagent)well.getLatestReleasedReagent().restrict());
        SmallMoleculeReagent smr = null;
        if (reagent != null && reagent instanceof SmallMoleculeReagent) {
          smr = (SmallMoleculeReagent) reagent;
        }

        util.writeNode(well.getPlateNumber(), "plateNumber");
        util.writeNode(well.getWellName(), "wellName");
        util.writeNode(well.getLibraryWellType(), "libraryWellType");
        util.writeNode(well.getFacilityId(), "hmsLincsFacilityId");
        if (smr != null) {
          util.writeNode(smr.getSaltFormId(), "hmsLincsSaltFormId");
          util.writeNode(smr.getFacilityBatchId(), "hmsLincsFacilityBatchId");
          util.writeNode(smr.getPrimaryCompoundName(), "primaryCompoundName");
          util.writeNodes(smr.getCompoundNames(), "compoundNames", "compoundName");
          util.writeNode(structureImageProvider.getImageUrl(smr), "compoundStructureImageUrl");
          util.writeNode(smr.getSmiles(), "smiles");
          util.writeNode(smr.getInchi(), "inChi");
        }
        if (reagent != null) {
          util.writeNode(reagent.getVendorId().getVendorName(), "vendorProviderName");
          util.writeNode(reagent.getVendorId().getVendorIdentifier(), "vendorProviderIdNumber");
          util.writeNode(reagent.getVendorBatchId(), "vendorProviderBatchIdNumber");
        }
        if (smr != null) {
          util.writeNodes(smr.getPubchemCids(), "pubchemCids", "pubchemCid");
          util.writeNodes(smr.getChemblIds(), "chemblIds", "chemblId");
          util.writeNode(smr.getMolecularMass(), "molecularMass");
          if(smr.getMolecularFormula() != null )  util.writeNode(smr.getMolecularFormula().getMolecularFormula(), "molecularFormula");
          else util.writeNode(null, "molecularFormula");
          if(smr.getMolfile() != null) {
            String uri = well.acceptVisitor(getEntityUriGenerator());
            writer.startNode("molfile");
            writer.addAttribute("href",uri + "/molfile");
            writer.endNode();            
          }
        }
        util.writeNode(well.getMolarConcentration().toString(), "molarConcentration");
        util.writeNode(new EntityCollection<Publication>(Publication.class, smr.getPublications()), "publications");
        // TODO: this proposed name should be moved into the metadata spec
        writer.startNode("qualityControlDocuments");
        for (AttachedFile af : reagent.getAttachedFiles()) {
          writeAttachedFile(af, writer);
        }
        writer.endNode();

        // TODO: code copied from WellViewer; need to encapsulate into a DAO method
        Set<Reagent> reagents = librariesDao.findReagents(well.getLatestReleasedReagent().getVendorId(), true);                                                                                                  
        reagents.remove(well.getLatestReleasedReagent());
        List<Well> otherWellsWithReagent = Lists.newArrayList(Iterables.transform(reagents, new Function<Reagent,Well>() { public Well apply(Reagent r) { return r.getWell(); } }));
        util.writeNode(new EntityCollection<Well>(Well.class, otherWellsWithReagent), "otherWellsWithReagent");

        util.writeUri(well.getLibrary(), "library");
      }
    });
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context)
  {
    return null;
  }

}