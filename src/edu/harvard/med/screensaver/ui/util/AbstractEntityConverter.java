// $HeadURL$
// $Id$
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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Base class for Converters that convert between an AbstractEntity and its entity ID.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AbstractEntityConverter<E extends AbstractEntity> implements Converter
{
  private Class<E> _entityType;
  private GenericEntityDAO _dao;

  public AbstractEntityConverter(Class<E> entityType, GenericEntityDAO dao)
  {
    _entityType = entityType;
    _dao = dao;
  }

  public Object getAsObject(FacesContext arg0,
                            UIComponent uiComponent,
                            String entityId) throws ConverterException
  {
    if (_dao == null) {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      _dao = (GenericEntityDAO) facesCtx.getApplication()
                           .getVariableResolver()
                           .resolveVariable(facesCtx, "genericEntityDao");
    }
    if (entityId == null) {
      return null;
    }
    try {
      E entity = _dao.findEntityById(_entityType,
                                     Integer.parseInt(entityId));
      if (entity == null) {
        throw new ConverterException("cannot find ScreeningRoomUser for id="
                                     + entityId + " for component "
                                     + uiComponent.getId());
      }
      return entity;
    }
    catch (NumberFormatException e) {
      throw new ConverterException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public String getAsString(FacesContext arg0, UIComponent arg1, Object entity)
    throws ConverterException
  {
    if (entity == null) {
      return null;
    }
    if (!(_entityType.isInstance(entity))) {
      throw new ConverterException(_entityType.getSimpleName()
                                   + " object expected: cannot convert object of type "
                                   + entity.getClass()
                                   + " to ID string for component "
                                   + arg1.getId());
    }
    return ((E) entity).getEntityId().toString();
  }
}
