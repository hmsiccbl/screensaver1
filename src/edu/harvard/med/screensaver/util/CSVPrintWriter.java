// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.log4j.Logger;

// TODO: implement quoting of string values
public class CSVPrintWriter extends PrintWriter
{
  // static members

  private static Logger log = Logger.getLogger(CSVPrintWriter.class);
  private String _delimiter = ",";
  private boolean _newLine = true;


  // instance data members

  // public constructors and methods

  public CSVPrintWriter(Writer out)
  {
    super(out);
  }
  
  public CSVPrintWriter(Writer out,
                        String delimiter)
  {
    super(out);
    _delimiter = delimiter;
  }
  
  @Override
  public void print(boolean b)
  {
    print(Boolean.toString(b));
  }
  
  @Override
  public void print(char c)
  {
    print(Character.toString(c));
  }
  
  @Override
  public void print(char[] s)
  {
    print(new String(s));
  }
  
  @Override
  public void print(double d)
  {
    print(Double.toString(d));
  }
  
  @Override
  public void print(float f)
  {
    print(Float.toString(f));
  }
  
  @Override
  public void print(int i)
  {
    print(Integer.toString(i));
  }
  
  @Override
  public void print(long l)
  {
    print(Long.toString(l));
  }
  
  @Override
  public void print(Object o)
  {
    print(o.toString());
  }
  
  @Override
  public void print(String s)
  {
    printDelimiter();
    super.print(escape(s));
  }
  
  @Override
  public void println()
  {
    super.println();
    _newLine = true;
  }
  
  @Override
  public void println(boolean x)
  {
    print(x);
    println();
  }
  
  @Override
  public void println(char x)
  {
    print(x);
    println();
  }
  
  @Override
  public void println(char[] x)
  {
    print(x);
    println();
  }
  
  @Override
  public void println(double x)
  {
    print(x);
    println();
  }
  
  @Override
  public void println(float x)
  {
    print(x);
    println();
  }
  
  @Override
  public void println(int x)
  {
    print(x);
    println();
  }

  @Override
  public void println(long x)
  {
    print(x);
    println();
  }

  @Override
  public void println(Object x)
  {
    print(x);
    println();
  }

  @Override
  public void println(String x)
  {
    print(x);
    println();
  }

  @Override
  public PrintWriter append(char c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter append(CharSequence csq, int start, int end)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter append(CharSequence csq)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter format(Locale l, String format, Object... args)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter format(String format, Object... args)
  {
    throw new UnsupportedOperationException();
  }


  
  // private methods

  private String escape(String s)
  {
//    s = s.replaceAll("\\", "\\\\");
//    s = s.replaceAll(",", "\\,");
    return s;
  }

  private void printDelimiter()
  {
    if (!_newLine) {
      super.print(_delimiter);
    }
    else {
      _newLine = false;
    }
  }
}

