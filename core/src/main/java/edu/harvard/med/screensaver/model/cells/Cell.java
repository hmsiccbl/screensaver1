// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/libraries/Gene.java $
// $Id: Gene.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;


/**
 * Information about a Cell Lines or Primary Cells used in Screens.
 * Note: preliminary implementation, fields will be strongly typed post-decision about using ontology.
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@org.hibernate.annotations.Proxy
public abstract class Cell extends AuditedAbstractEntity<Integer> implements Comparable<Cell>
{
  private static final long serialVersionUID = 0L;
  
	public static final RelationshipPath<Cell> experimentalCellInformationSetPath = RelationshipPath.from(Cell.class).to("experimentalCellInformationSet");//, Cardinality.TO_MANY);

  private SortedSet<ExperimentalCellInformation> experimentalCellInformationSet = Sets.newTreeSet();
//  private CellLineType cellLineType;
	private String name;
  private String cloId;  // TODO: from the Cell Line Ontology
	private String alternateName;
	private String alternateId;
	private String centerName;
	private String centerSpecificId;
	private String vendor;
  private String vendorCatalogId;
  private String batchId;
  private String organism;  // TODO: controlled vocabulary
  private String organ;
  private String tissue;
  private String cellType;  // TODO: controlled vocabulary
  private String disease;
  private SortedSet<String> growthProperties;
  private String geneticModification;
  private SortedSet<String> relatedProjects;
  private String verification;
  private String recommendedCultureConditions;
  private String organismGender;    // TODO: male/female/genderless

	private String facilityId;

	private String cellTypeDetail;
	private String diseaseDetail;
	private String mutationsReference;
	private String mutationsExplicit;

	private String verificationReferenceProfile;

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public Cell()
  {
  	super(null); // not supporting the parent AuditedAbstractEntity contract requiring a createdBy to be set.  this can be set later if needed. -sde4
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name="cell_id_seq",
                                              strategy="sequence",
                                              parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="cell_id_seq")})
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cell_id_seq")
  public Integer getCellId()
  {
    return getEntityId();
  }

  private void setCellId(Integer cellId)
  {
    setEntityId(cellId);
  }
  

  @Column(nullable=false, unique=true)
	public String getFacilityId() {
		return facilityId;
	}
	
	public void setFacilityId(String value) {
		this.facilityId = value;
	}
  
  /**
   */
  @OneToMany(mappedBy = "cell", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.ForeignKey(name = "fk_experimental_cell_information_link_to_cell")
  public SortedSet<ExperimentalCellInformation> getExperimentalCellInformationSet()
  {
    return experimentalCellInformationSet;
  }
  
  private void setExperimentalCellInformationSet(SortedSet<ExperimentalCellInformation> value)
  {
  	experimentalCellInformationSet = value;
  }

  public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

  @Column(nullable=true)
	public String getCloId() {
		return cloId;
	}

	public void setCloId(String cloId) {
		this.cloId = cloId;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVendorCatalogId() {
		return vendorCatalogId;
	}

	public void setVendorCatalogId(String vendorCatalogId) {
		this.vendorCatalogId = vendorCatalogId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getTissue() {
		return tissue;
	}

	public void setTissue(String tissue) {
		this.tissue = tissue;
	}

	public String getCellType() {
		return cellType;
	}

	public void setCellType(String cellType) {
		this.cellType = cellType;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}


  @ElementCollection(fetch=FetchType.EAGER)
  @Column(name="growthProperty", nullable=false)
  @JoinTable(
    name="cellGrowthProperties",
    joinColumns=@JoinColumn(name="cellId")
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_cell_growth_properties_to_cell")
  public SortedSet<String> getGrowthProperties() {
		return growthProperties;
	}

	public void setGrowthProperties(SortedSet<String> growthProperties) {
		this.growthProperties = growthProperties;
	}

	public String getGeneticModification() {
		return geneticModification;
	}

	public void setGeneticModification(String geneticModification) {
		this.geneticModification = geneticModification;
	}

  @ElementCollection(fetch=FetchType.EAGER)
  @Column(name="relatedProject", nullable=false)
  @JoinTable(
    name="cellRelatedProjects",
    joinColumns=@JoinColumn(name="cellId")
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_cell_related_projects_to_cell")
  public SortedSet<String> getRelatedProjects() {
		return relatedProjects;
	}
  

  @org.hibernate.annotations.Type(type="text")
	public String getCellTypeDetail() {
		return this.cellTypeDetail;
	}

  public void setCellTypeDetail(String value)
  {
  	this.cellTypeDetail = value;
  }
  
  @org.hibernate.annotations.Type(type="text")
	public String getDiseaseDetail() {
		return this.diseaseDetail;
	}

  public void setDiseaseDetail(String value) {
  	this.diseaseDetail = value;
  }
  
  @org.hibernate.annotations.Type(type="text")
	public String getMutationsReference() {
		return this.mutationsReference;
	}
  
  public void setMutationsReference(String value)
  {
  	this.mutationsReference = value;
  }

  @org.hibernate.annotations.Type(type="text")
	public String getMutationsExplicit() {
  	return mutationsExplicit;
	}
  
  public void setMutationsExplicit(String value) {
  	this.mutationsExplicit = value;
  }

	public void setRelatedProjects(SortedSet<String> relatedProjects) {
		this.relatedProjects = relatedProjects;
	}

  @org.hibernate.annotations.Type(type="text")
  public String getVerification() {
		return verification;
	}

	public void setVerification(String verification) {
		this.verification = verification;
	}

  @org.hibernate.annotations.Type(type="text")
  public String getRecommendedCultureConditions() {
		return recommendedCultureConditions;
	}

	public void setRecommendedCultureConditions(String recommendedCultureConditions) {
		this.recommendedCultureConditions = recommendedCultureConditions;
	}

	public String getOrganismGender() {
		return organismGender;
	}

	public void setOrganismGender(String organismGender) {
		this.organismGender = organismGender;
	}


	public String getAlternateName() {
		return alternateName;
	}

	public void setAlternateName(String alternateName) {
		this.alternateName = alternateName;
	}

	public String getAlternateId() {
		return alternateId;
	}

	public void setAlternateId(String alternateId) {
		this.alternateId = alternateId;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public String getCenterSpecificId() {
		return centerSpecificId;
	}

	public void setCenterSpecificId(String centerSpecificId) {
		this.centerSpecificId = centerSpecificId;
	}

	public String getOrgan() {
		return organ;
	}

	public void setOrgan(String organ) {
		this.organ = organ;
	}
	
	@org.hibernate.annotations.Type(type="text")
  public String getVerificationReferenceProfile() {
		return this.verificationReferenceProfile;
	}

	public void setVerificationReferenceProfile(String value) {
		this.verificationReferenceProfile = value;
	}
	
  @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "cellUpdateActivity",
             joinColumns = @JoinColumn(name = "cellId", nullable = false, updatable = false),
             inverseJoinColumns = @JoinColumn(name = "updateActivityId", nullable = false, updatable = false, unique = true))
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @Sort(type = SortType.NATURAL)
  @ToMany(singularPropertyName = "updateActivity", hasNonconventionalMutation = true /*
                                                                                      * model testing framework doesn't
                                                                                      * understand this is a containment
                                                                                      * relationship, and so requires
                                                                                      * addUpdateActivity() method
                                                                                      */)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }	
	
	public int compareTo(Cell o) {
    if (this.equals(o)) {
      return 0;
    }
    return this.getFacilityId().compareTo(o.getFacilityId());
    //return hashCode() > o.hashCode() ? 1 : -1;
	}
	
	@Override
	public String toString()
	{
		return getFacilityId() + ": " + getName();
	}



}
