// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/libraries/Gene.java $
// $Id: Gene.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.Screen;


/**
 * Information about a Cell Lines or Cells used in Screens.
 * TODO: make this sortable/comparable
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
public class ExperimentalCellInformation extends AbstractEntity<Integer>  implements Comparable<ExperimentalCellInformation>
{
  private static final long serialVersionUID = 0L;

	public static final RelationshipPath<ExperimentalCellInformation> cellProperty = RelationshipPath.from(ExperimentalCellInformation.class).to("cell", Cardinality.TO_ONE);
	public static final RelationshipPath<ExperimentalCellInformation> screenProperty = RelationshipPath.from(ExperimentalCellInformation.class).to("screen", Cardinality.TO_ONE);
	//public static final RelationshipPath<ExperimentalCellInformation> dataColumnProperty = RelationshipPath.from(ExperimentalCellInformation.class).to("dataColumn", Cardinality.TO_ONE);

  private Cell cell;
  private Screen screen;

  private String cultureConditions;
  private String transientModification;
  
  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  private ExperimentalCellInformation() {}

  public ExperimentalCellInformation(Cell cell2, Screen s) {
  	setCell(cell2);
  	setScreen(s);
	}

	@Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name="exp_cell_information_id_seq",
                                              strategy="sequence",
                                              parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="exp_cell_information_id_seq")})
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="exp_cell_information_id_seq")
  public Integer getExperimentalCellInformationId()
  {
    return getEntityId();
  }

  private void setExperimentalCellInformationId(Integer id)
  {
    setEntityId(id);
  }

  @ManyToOne
  @JoinColumn(name="cellId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_exp_cell_information_to_cell")
	public Cell getCell() {
		return cell;
	}

	private void setCell(Cell cell) {
		this.cell = cell;
	}

  @ManyToOne
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_exp_cell_information_to_screen")
	public Screen getScreen() {
		return screen;
	}

	private void setScreen(Screen screen) {
		this.screen = screen;
	}
	
	@Override
	public int compareTo(ExperimentalCellInformation o) 
	{
		if(this==o) return 0;
		if(getCell() != null) {
			return o.getCell() == null? 1: o.getCell().compareTo(getCell());
		}
		return getEntityId().compareTo(o.getEntityId());	
	}

	public String toString()
	{
		String s = "[";
		s += "screen: " + getScreen().getFacilityId();
		s += ", cell: " + getCell().getFacilityId() + "]";
		return s;
	}

	public ExperimentalCellInformation forScreen(Screen s)
	{
		setScreen(s);
		return this;
	}

}
