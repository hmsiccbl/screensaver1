// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.text.DateFormat;
import java.util.EnumSet;

import org.joda.time.DateTime;

import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

public enum AnnotationHeaderColumn
{
  STUDY_NAME("Study Name", "Name of the Study"/*, "annotationValues[annotationType].annotationType.study", "title"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      return r.getAnnotationValues().get(type).getAnnotationType().getStudy().getTitle();
    }
  },
  
  DATE("Date", "Date this study was entered into the system"/*, "annotationValues[annotationType].annotationType.study", "dateCreated"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      DateTime d =  r.getAnnotationValues().get(type).getAnnotationType().getStudy().getDateCreated();
      return d == null? "" : DateFormat.getDateInstance(DateFormat.SHORT).format(d.toDate());
    }
  },
  
  STUDY_LEAD("Study Lead", "Lead Screener for the Study"/*, "annotationValues[annotationType].annotationType.study.leadScreener", "name"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      ScreeningRoomUser s = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getLeadScreener();
      return s==null ? "" : s.getName();
    }
    
  },
  
  LAB_HEAD("Lab Head", "Lab Head"/*, "annotationValues[annotationType].annotationType.study.labHead", "name"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      ScreeningRoomUser s = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getLabHead();
      return s==null ? "" : s.getName();
    }
    
  },
  
  SUMMARY("Summary", "Summary description of the study"/*, "annotationValues[annotationType].annotationType.study", "summary"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      return r.getAnnotationValues().get(type).getAnnotationType().getStudy().getSummary();
    }
  },

  STUDY_TYPE("Type", "Study type"/*, "annotationValues[annotationType].annotationType.study", "studyType"*/)
  {
    public String getValueInternal(Reagent r, AnnotationType type)
    {
      StudyType t = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getStudyType();
      return t == null ? "" : t.getValue();
    }
  };
  
  public final String _basename;
  public final String _description;
  
  private AnnotationHeaderColumn(String basename, String description)
  {
    _basename = basename;
    _description = description;
  }
  
  public String getSummaryColName(Integer studyNumber)
  {
    return String.format("%s [%d]", _basename, studyNumber);
  }
  
  public String getColName()
  {
    return _basename;
  }
  
  public String getDescription() { return _description; }
  
  public boolean matchesOnSummaryColName(TableColumn<Reagent,?> col, Integer studyNumber)
  {
    return col.getName().indexOf(_basename) == 0 &&
      col.getName().indexOf(studyNumber.toString()) > 0;
  }
  
  public String getValue(Reagent r, AnnotationType type)
  {
    if(r.getAnnotationValues().get(type) != null)
    {
      return getValueInternal(r,type);
    }
    return "";
  }
  
  public abstract String getValueInternal(Reagent r, AnnotationType type);
  
  public static AnnotationHeaderColumn fromSummaryColName(String colName)
  {
    for(AnnotationHeaderColumn roc: EnumSet.allOf(AnnotationHeaderColumn.class) )
    {
      if(colName.matches(roc._basename  + "\\ \\[\\d+\\]")  )
      {
        return roc;
      }
    }
    return null;
  }
  
}