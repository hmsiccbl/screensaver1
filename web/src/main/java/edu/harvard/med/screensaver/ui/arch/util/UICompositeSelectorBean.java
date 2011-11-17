package edu.harvard.med.screensaver.ui.arch.util;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.harvard.med.screensaver.model.VocabularyTerm;

/**
 * Pairs an inputText field and selectOneMenu widget, providing the necessary bound values.
 * Note: must be deployed with the proper converter for &lt;K&gt;  in order to set/get values from the textInput field.
  **/
public class UICompositeSelectorBean<K extends Serializable, T extends VocabularyTerm>
{
  
  private UISelectOneBean<T> selectorOneBean;
  private K value;
  
  public UICompositeSelectorBean(K initialValue, T defaultSelection, List<T> displayValues)
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

  public void setValue(K value)
  {
    this.value = value;
  }

  public K getValue()
  {
    return value;
  }
  
  public boolean isEmpty()
  {
    return value == null; // StringUtils.isEmpty(value);
  }
  
}
