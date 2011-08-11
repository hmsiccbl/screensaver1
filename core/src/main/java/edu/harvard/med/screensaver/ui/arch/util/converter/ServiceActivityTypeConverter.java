package edu.harvard.med.screensaver.ui.arch.util.converter;

import edu.harvard.med.screensaver.model.activities.ServiceActivityType;

public class ServiceActivityTypeConverter extends VocabularyConverter<ServiceActivityType>
{
  public ServiceActivityTypeConverter()
  {
    super(ServiceActivityType.values());
  }
}

