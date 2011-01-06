// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import java.io.Serializable;

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
public abstract class AbstractEntityConverter<E extends AbstractEntity> implements Converter
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
      E entity = _dao.findEntityById(_entityType, parseEntityId(entityId));
      if (entity == null) {
        throw new ConverterException("no such " + _entityType.getSimpleName() + 
                                     " with id=" + entityId + 
                                     " for component " + uiComponent.getId());
      }
      return entity;
    }
    catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  abstract protected Serializable parseEntityId(String entityIdStr);

  @SuppressWarnings("unchecked")
  public String getAsString(FacesContext arg0, UIComponent arg1, Object entity)
    throws ConverterException
  {
    if (entity == null) {
      return "";
    }
    if (entity.equals("")) {
      return ""; // handle conversion of "empty" SelectItem.value, which is not allowed to be null
    }
    if (!!!_entityType.isInstance(entity)) {
      throw new ConverterException("expected type " + _entityType);
    }
    return ((E) entity).getEntityId().toString();
  }
}
