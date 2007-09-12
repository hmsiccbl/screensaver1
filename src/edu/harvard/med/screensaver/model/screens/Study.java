// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public abstract class Study extends AbstractEntity
{
  abstract public Set<ScreeningRoomUser> getCollaborators();

  abstract public ScreeningRoomUser getLabHead();

  abstract public ScreeningRoomUser getLeadScreener();

  abstract public Integer getStudyNumber();

  abstract public StudyType getStudyType();

  abstract public String getTitle();

  abstract public SortedSet<AnnotationType> getAnnotationTypes();

  abstract public ScreenType getScreenType();

  public boolean isStudyOnly()
  {
    // TODO: this is a total hack; proper solution is waiting on having the Study->Screen->IccbScreen hierarchy in place
    return getStudyType().equals(StudyType.IN_SILICO);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
}

