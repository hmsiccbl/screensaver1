// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.TimeStampedAbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public abstract class Study extends TimeStampedAbstractEntity
{
  public static final Integer MIN_STUDY_NUMBER = 100000;

  abstract public Set<ScreeningRoomUser> getCollaborators();

  abstract public ScreeningRoomUser getLabHead();

  abstract public ScreeningRoomUser getLeadScreener();

  abstract public Integer getStudyNumber();

  abstract public StudyType getStudyType();

  abstract public String getTitle();

  abstract public SortedSet<AnnotationType> getAnnotationTypes();

  abstract public Collection<Reagent> getReagents();

  abstract public ScreenType getScreenType();

  abstract public boolean isShareable();

  @Transient
  public boolean isStudyOnly()
  {
    // TODO: this is a total hack; proper solution is waiting on having the Study->Screen->IccbScreen hierarchy in place
    return getStudyNumber() == null || getStudyNumber() >= MIN_STUDY_NUMBER;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
}

