package edu.harvard.med.iccbl.screensaver.policy;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;

/**
 * Specialized {@link SmallMoleculeReagent} that prevents access to structure-related properties.
 */
public class StructureRestrictedSmallMoleculeReagent extends SmallMoleculeReagent
{
  private SmallMoleculeReagent delegate;

  public StructureRestrictedSmallMoleculeReagent(SmallMoleculeReagent entity)
  {
    delegate = entity;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return delegate.acceptVisitor(visitor);
  }

  @Override
  public String getSmiles()
  {
    if (delegate.isRestrictedStructure()) { 
      return null;
    }
    return delegate.getSmiles();
  }

  @Override
  public String getInchi()
  {
    if (delegate.isRestrictedStructure()) {
      return null;
    }
    return delegate.getInchi();
  }

  @Override
  public List<String> getCompoundNames()
  {
    return delegate.getCompoundNames();
  }

  @Override
  public int getNumCompoundNames()
  {
    return delegate.getNumCompoundNames();
  }

  @Override
  public String getPrimaryCompoundName()
  {
    return delegate.getPrimaryCompoundName();
  }

  @Override
  public Set<Integer> getPubchemCids()
  {
    return delegate.getPubchemCids();
  }

  @Override
  public int getNumPubchemCids()
  {
    return delegate.getNumPubchemCids();
  }

  @Override
  public Set<Integer> getChembankIds()
  {
    return delegate.getChembankIds();
  }

  @Override
  public int getNumChembankIds()
  {
    return delegate.getNumChembankIds();
  }

  @Override
  public Set<Integer> getChemblIds()
  {
    return delegate.getChemblIds();
  }

  @Override
  public int getNumChemblIds()
  {
    return delegate.getNumChemblIds();
  }

  @Override
  public String getMolfile()
  {
    if (delegate.isRestrictedStructure()) {
      return null;
    }
    return delegate.getMolfile();
  }

  @Override
  public void setMolfile(String molfile)
  {
    delegate.setMolfile(molfile);
  }

  @Override
  public BigDecimal getMolecularMass()
  {
    if (delegate.isRestrictedStructure()) {
      return null;
    }
    return delegate.getMolecularMass();
  }

  @Override
  public BigDecimal getMolecularWeight()
  {
    if (delegate.isRestrictedStructure()) {
      return null;
    }
    return delegate.getMolecularWeight();
  }

  @Override
  public MolecularFormula getMolecularFormula()
  {
    if (delegate.isRestrictedStructure()) {
      return null;
    }
    return delegate.getMolecularFormula();
  }

  @Override
  public String getVendorBatchId()
  {
    return delegate.getVendorBatchId();
  }

  @Override
  public Integer getFacilityBatchId()
  {
    return delegate.getFacilityBatchId();
  }

  @Override
  public Integer getSaltFormId()
  {
    return delegate.getSaltFormId();
  }

  @Override
  public SmallMoleculeReagent forSaltFormId(Integer saltFormId)
  {
    return delegate.forSaltFormId(saltFormId);
  }

  @Override
  public boolean isRestrictedStructure()
  {
    return delegate.isRestrictedStructure();
  }

  @Override
  public int compareTo(Reagent o)
  {
    return delegate.compareTo(o);
  }

  @Override
  public Integer getReagentId()
  {
    return delegate.getReagentId();
  }

  @Override
  public ReagentVendorIdentifier getVendorId()
  {
    return delegate.getVendorId();
  }

  @Override
  public Well getWell()
  {
    return delegate.getWell();
  }

  @Override
  public LibraryContentsVersion getLibraryContentsVersion()
  {
    return delegate.getLibraryContentsVersion();
  }

  @Override
  public Map<AnnotationType,AnnotationValue> getAnnotationValues()
  {
    return delegate.getAnnotationValues();
  }

  @Override
  public Set<Screen> getStudies()
  {
    return delegate.getStudies();
  }

  @Override
  public boolean addStudy(Screen study)
  {
    return delegate.addStudy(study);
  }

  @Override
  public boolean removeStudy(Screen study)
  {
    return delegate.removeStudy(study);
  }

  @Override
  public boolean removeStudy(Screen study, boolean removeReagentStudyLink)
  {
    return delegate.removeStudy(study, removeReagentStudyLink);
  }

  @Override
  public Set<Publication> getPublications()
  {
    return delegate.getPublications();
  }

  @Override
  public boolean addPublication(Publication p)
  {
    return delegate.addPublication(p);
  }

  @Override
  public Set<AttachedFile> getAttachedFiles()
  {
    return delegate.getAttachedFiles();
  }

  @Override
  public AttachedFile createAttachedFile(String filename,
                                         ReagentAttachedFileType fileType,
                                         LocalDate fileDate,
                                         String fileContents) throws IOException
  {
    return delegate.createAttachedFile(filename, fileType, fileDate, fileContents);
  }

  @Override
  public AttachedFile createAttachedFile(String filename,
                                         ReagentAttachedFileType fileType,
                                         LocalDate fileDate,
                                         InputStream fileContents) throws IOException
  {
    return delegate.createAttachedFile(filename, fileType, fileDate, fileContents);
  }

  @Override
  public void removeAttachedFile(AttachedFile attachedFile)
  {
    delegate.removeAttachedFile(attachedFile);
  }

  @Override
  public Integer getEntityId()
  {
    return delegate.getEntityId();
  }

  @Override
  public boolean isTransient()
  {
    return delegate.isTransient();
  }

  @Override
  public boolean equals(Object obj)
  {
    return delegate.equals(obj);
  }

  @Override
  public int hashCode()
  {
    return delegate.hashCode();
  }

  @Override
  public boolean isEquivalent(AbstractEntity that)
  {
    return delegate.isEquivalent(that);
  }

  @Override
  public String toString()
  {
    return delegate.toString();
  }

  @Override
  public boolean isRestricted()
  {
    return delegate.isRestricted();
  }

  @Override
  public Entity<Integer> restrict()
  {
    return delegate.restrict();
  }

  @Override
  public void setEntityViewPolicy(EntityViewPolicy<Entity> entityViewPolicy)
  {
    delegate.setEntityViewPolicy(entityViewPolicy);
  }

  @Override
  public EntityViewPolicy getEntityViewPolicy()
  {
    return delegate.getEntityViewPolicy();
  }

  @Override
  public <P> P getPropertyValue(String propertyName, Class<P> propertyType)
  {
    return delegate.getPropertyValue(propertyName, propertyType);
  }
}