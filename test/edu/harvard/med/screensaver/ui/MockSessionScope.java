// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.Map;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.RequestAttributes;

public class MockSessionScope implements Scope
{
  private static final int SCOPE = 0;
  private RequestAttributes attrs = new RequestAttributes() {

    private Map<String,Object> attrs = Maps.newHashMap();

    @Override
    public Object getAttribute(String name, int scope)
    {
      return attrs.get(name);
    }

    @Override
    public String[] getAttributeNames(int scope)
    {
      return (String[]) attrs.keySet().toArray(new String[attrs.keySet().size()]);
    }

    @Override
    public String getSessionId()
    {
      return "mockSessionId";
    }

    @Override
    public Object getSessionMutex()
    {
      return attrs;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope)
    {
    }

    @Override
    public void removeAttribute(String name, int scope)
    {
      attrs.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value, int scope)
    {
      attrs.put(name, value);
    }
  };

  /**
   * Create a new SessionScope, storing attributes in a locally
   * isolated session (or default session, if there is no distinction
   * between a global session and a component-specific session).
   */
  public MockSessionScope()
  {
  }

  public String getConversationId()
  {
    return attrs.getSessionId();
  }

  public Object get(String name, ObjectFactory objectFactory)
  {
    Object mutex = attrs.getSessionMutex();
    synchronized (mutex) {
      Object attribute = attrs.getAttribute(name, SCOPE);
      if (attribute == null) {
        attribute = objectFactory.getObject();
        attrs.setAttribute(name, attribute, SCOPE);
      }
      return attribute;
    }
  }

  public Object remove(String name)
  {
    Object mutex = attrs.getSessionMutex();
    synchronized (mutex) {
      Object attribute = attrs.getAttribute(name, SCOPE);
      attrs.removeAttribute(name, SCOPE);
      return attribute;
    }
  }

  @Override
  public void registerDestructionCallback(String name, Runnable callback)
  {}
}
