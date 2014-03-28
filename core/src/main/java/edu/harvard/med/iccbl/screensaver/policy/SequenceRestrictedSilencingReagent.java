package edu.harvard.med.iccbl.screensaver.policy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;

/**
 * Specialized {@link SilencingReagent} that prevents access to sequence-related properties.
 */
public class SequenceRestrictedSilencingReagent extends SilencingReagent
{
  private static final long serialVersionUID = 1L;
  
  private SilencingReagent delegate;

  public SequenceRestrictedSilencingReagent(SilencingReagent entity)
  {
    delegate = entity;
  }

  @Override
  public SilencingReagentType getSilencingReagentType()
  {
    return delegate.getSilencingReagentType();
  }

  @Override
  public String getSequence()
  {
    if (isRestrictedSequence()) {
      return null;
    }
    return delegate.getSequence();
  }

  @Override
  public String getAntiSenseSequence()
  {
    if (isRestrictedSequence()) {
      return null;
    }
    return delegate.getAntiSenseSequence();
  }

  @Override
  public Gene getVendorGene()
  {
    return delegate.getVendorGene();
  }

  @Override
  public Gene getFacilityGene()
  {
    return delegate.getFacilityGene();
  }

  @Override
  public List<Gene> getVendorGenes()
  {
    return delegate.getVendorGenes();
  }

  @Override
  public List<Gene> getFacilityGenes()
  {
    return delegate.getFacilityGenes();
  }

  @Override
  public Set<Well> getDuplexWells()
  {
    return delegate.getDuplexWells();
  }

  @Override
  public SilencingReagent withDuplexWell(Well duplexWell)
  {
    return delegate.withDuplexWell(duplexWell);
  }

  @Override
  public Set<SilencingReagent> getDuplexSilencingReagents()
  {
    return delegate.getDuplexSilencingReagents();
  }

  @Override
  public boolean isRestrictedSequence()
  {
    return delegate.isRestrictedSequence();
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
