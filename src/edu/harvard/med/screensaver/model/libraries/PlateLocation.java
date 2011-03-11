// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.Triple;

/**
 * A physical storage location for a {@link Library} Plate}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "room", "freezer", "shelf", "bin" }) })
public class PlateLocation extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 1L;

  public static final Function<PlateLocation,String> ToRoom = new Function<PlateLocation,String>() {
    public String apply(PlateLocation pl)
    {
      return pl.getRoom();
    }
  };
  public static final Function<PlateLocation,String> ToFreezer = new Function<PlateLocation,String>() {
    public String apply(PlateLocation pl)
    {
      return pl.getFreezer();
    }
  };
  public static final Function<PlateLocation,Pair<String,String>> ToRoomFreezer = new Function<PlateLocation,Pair<String,String>>() {
    public Pair<String,String> apply(PlateLocation pl)
    {
      return new Pair<String,String>(pl.getRoom(), pl.getFreezer());
    }
  };
  public static final Function<PlateLocation,String> ToShelf = new Function<PlateLocation,String>() {
    public String apply(PlateLocation pl)
    {
      return pl.getShelf();
    }
  };
  public static final Function<PlateLocation,Triple<String,String,String>> ToRoomFreezerShelf = new Function<PlateLocation,Triple<String,String,String>>() {
    public Triple<String,String,String> apply(PlateLocation pl)
    {
      return new Triple<String,String,String>(pl.getRoom(), pl.getFreezer(), pl.getShelf());
    }
  };
  public static final Function<PlateLocation,String> ToBin = new Function<PlateLocation,String>() {
    public String apply(PlateLocation pl)
    {
      return pl.getBin();
    }
  };


  private String _room;
  private String _freezer;
  private String _shelf;
  private String _bin;

  /**
   * @motivation for hibernate or DTO
   */
  public PlateLocation()
  {
  }

  /**
   * Constructs an <code>PlateLocation</code> vocabulary term.
   */
  public PlateLocation(String room,
                       String freezer,
                       String shelf,
                       String bin)
  {
    if (room == null) {
      throw new RequiredPropertyException(PlateLocation.class, "room");
    }
    if (freezer == null) {
      throw new RequiredPropertyException(PlateLocation.class, "freezer");
    }
    if (shelf == null) {
      throw new RequiredPropertyException(PlateLocation.class, "shelf");
    }
    if (bin == null) {
      throw new RequiredPropertyException(PlateLocation.class, "bin");
    }
    _room = room;
    _freezer = freezer;
    _shelf = shelf;
    _bin = bin;
  }

  public PlateLocation(PlateLocation locationDto)
  {
    this(locationDto.getRoom(),
         locationDto.getFreezer(),
         locationDto.getShelf(),
         locationDto.getBin());
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name = "plate_location_id_seq",
                                              strategy = "sequence",
                                              parameters = { @Parameter(name = "sequence", value = "plate_location_id_seq") })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plate_location_id_seq")
  public Integer getPlateLocationId()
  {
    return getEntityId();
  }
  
  private void setPlateLocationId(Integer plateLocationId)
  {
    setEntityId(plateLocationId);
  }

  @Column(nullable=false)
  @Type(type = "text")
  public String getRoom()
  {
    return _room;
  }

  private void setRoom(String room)
  {
    _room = room;
  }

  @Column(nullable=false)
  @Type(type = "text")
  public String getFreezer()
  {
    return _freezer;
  }

  private void setFreezer(String freezer)
  {
    _freezer = freezer;
  }

  @Column(nullable = false)
  @Type(type = "text")
  public String getShelf()
  {
    return _shelf;
  }

  private void setShelf(String shelf)
  {
    _shelf = shelf;
  }

  @Column(nullable=false)
  @Type(type = "text")
  public String getBin()
  {
    return _bin;
  }

  private void setBin(String bin)
  {
    _bin = bin;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public String toDisplayString()
  {
    return Joiner.on("-").join(ImmutableList.of(_room, _freezer, _shelf, _bin));
  }
}
