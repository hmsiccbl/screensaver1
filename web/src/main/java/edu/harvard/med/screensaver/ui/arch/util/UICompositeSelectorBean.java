package edu.harvard.med.screensaver.ui.arch.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.harvard.med.screensaver.model.VocabularyTerm;

/**
 * Pairs an inputText field and selectOneMenu widget, providing the necessary bound values.
  **/
public class UICompositeSelectorBean<T extends VocabularyTerm>
{
  
  private UISelectOneBean<T> selectorOneBean;
  private String value;
  
  public UICompositeSelectorBean(String initialValue, T defaultSelection, List<T> displayValues)
   {
     this.selectorOneBean =
      new UISelectOneBean<T>(displayValues, defaultSelection)
      {
        @Override
        protected String makeLabel(T t) { return t.getValue(); }
      };
      this.setValue(initialValue);
   }
  
  public UISelectOneBean<T> getSelectorBean()
  {
    return this.selectorOneBean;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
  
  public boolean isEmpty()
  {
    return StringUtils.isEmpty(value);
  }
  
}
