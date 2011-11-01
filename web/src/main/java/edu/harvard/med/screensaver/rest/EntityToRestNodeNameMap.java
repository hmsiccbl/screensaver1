package edu.harvard.med.screensaver.rest;

/**
 * Maps Entity (or other) class names to REST-ful node names.
 * TODO: this may be replaced with a real schema definition at some point; this was created due to the lack of namespace support, and minimal mapping support in 
 * the XStreamMarshaller
 */
public interface EntityToRestNodeNameMap
{
  public String getNodeName(Class clazz);
}
