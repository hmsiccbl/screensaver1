package edu.harvard.med.screensaver.rest;

import java.util.Map;

import edu.harvard.med.screensaver.util.StringUtils;

import com.google.common.collect.Maps;
/**
 * TODO: this may be replaced with a real schema definition at some point
 */
public class LincsEntityToNodeNameMap implements EntityToRestNodeNameMap
{
  
  private Map<String,String> map;

  public LincsEntityToNodeNameMap() 
  {
    this.map = Maps.newHashMap();
  }
  
  public void setMapping(Map<String,String> mapping)
  {
    this.map = Maps.newHashMap(mapping);
  }
  
  @Override
  public String getNodeName(Class clazz)
  {
    if (this.map.containsKey(clazz.getName())) {
      return this.map.get(clazz.getName());
    }
    else {
      // the default naming for LINCS
      return clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
    }
  }

}
