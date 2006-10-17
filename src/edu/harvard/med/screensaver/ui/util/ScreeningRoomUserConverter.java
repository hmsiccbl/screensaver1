// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Converts a ScreeningRoomUser between its entity object and its entity ID (as a String object).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreeningRoomUserConverter implements Converter
{
  
  private DAO _dao;

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public Object getAsObject(FacesContext arg0, UIComponent arg1, String entityId)
    throws ConverterException
  {
    if (_dao == null) {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      _dao = (DAO) facesCtx.getApplication()
                           .getVariableResolver()
                           .resolveVariable(facesCtx, "dao");
    }
    try {
      ScreeningRoomUser entity = _dao.findEntityById(ScreeningRoomUser.class,
                                                     Integer.parseInt(entityId));
      if (entity == null) {
        throw new ConverterException("cannot find ScreeningRoomUser for id="
                                     + entityId + " for component "
                                     + arg1.getId());
      }
      return entity;
    }
    catch (NumberFormatException e) {
      throw new ConverterException(e);
    }
  }

  public String getAsString(FacesContext arg0,
                            UIComponent arg1,
                            Object screeningRoomUser) throws ConverterException
  {
    if (!(screeningRoomUser instanceof ScreeningRoomUser)) {
      throw new ConverterException("ScreeningRoomUser object expected: cannot convert object of type "
                                   + screeningRoomUser.getClass()
                                   + " to string for component " + arg1.getId());
    }
    return ((ScreeningRoomUser) screeningRoomUser).getEntityId()
                                                  .toString();
  }

}
