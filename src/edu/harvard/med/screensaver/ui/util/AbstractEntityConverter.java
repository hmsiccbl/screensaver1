// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Base class for Converters that convert between an AbstractEntity and its entity ID.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
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
    if (StringUtils.isEmpty(entityId)) {
      return null;
    }
    try {
      E entity = getDao().findEntityById(_entityType,
                                         Integer.parseInt(entityId));
      if (entity == null) {
        throw new ConverterException("no such " + _entityType.getSimpleName() + 
                                     " with id=" + entityId + 
                                     " for component " + uiComponent.getId());
      }
      return entity;
    }
    catch (NumberFormatException e) {
      throw new ConverterException(e);
    }
  }

  private GenericEntityDAO getDao()
  {
    if (_dao == null) {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      _dao = (GenericEntityDAO) facesCtx.getApplication().getVariableResolver().resolveVariable(facesCtx, "genericEntityDao");
    }
    return _dao;
  }

  @SuppressWarnings("unchecked")
  public String getAsString(FacesContext arg0, UIComponent arg1, Object value)
    throws ConverterException
  {
    if (value == null) {
      return "";
    }
    if (value.equals("")) {
      return ""; // handle conversion of "empty" SelectItem.value, which is not allowed to be null
    }
    if (!(_entityType.isInstance(value))) {
      throw new ConverterException("expected type " + _entityType);
    }
    return ((E) value).getEntityId().toString();
  }
}
