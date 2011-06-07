package edu.harvard.med.screensaver.io.screens;

import java.net.URL;

import edu.harvard.med.screensaver.model.Entity;

public class NoOpStudyImageProvider implements StudyImageProvider<Entity>
{
  @Override
  public URL getImageUrl(Entity entity)
  {
    return null;
  }

}
