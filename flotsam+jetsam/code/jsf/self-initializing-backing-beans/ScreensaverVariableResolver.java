// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.apache.log4j.Logger;
import org.springframework.web.jsf.DelegatingVariableResolver;

public class ScreensaverVariableResolver extends DelegatingVariableResolver
{

  private static Logger log = Logger.getLogger(ScreensaverVariableResolver.class);

  // instance data members
  

  // public constructors and methods

  public ScreensaverVariableResolver(VariableResolver variableResolver)
  {
    super(variableResolver);
  }

  @Override
  public Object resolveVariable(FacesContext facesContext, String variableName) throws EvaluationException
  {
    Object variable = super.resolveVariable(facesContext, variableName);
    if (variable instanceof AbstractSelfInitializingBackingBean) {
      AbstractSelfInitializingBackingBean bean = (AbstractSelfInitializingBackingBean) variable;
      if (!bean.isInitialized()) {
        bean.initialize();
      }
    }
    return variable;
  } 

  // private methods

}

