//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class CustomNewlinePrintWriter extends PrintWriter
{
  private String _newline;

  public CustomNewlinePrintWriter(Writer out, String newline)
  {
    super(out);
    _newline = newline;
  }

  public CustomNewlinePrintWriter(OutputStream out, String newline)
  {
    super(out);
    _newline = newline;
  }

  protected void doPrintln()
  {
    super.print(_newline);
    flush(); // respect PrinterWriter API contract
  }

  @Override
  public void println()
  {
    doPrintln();
  }

  @Override
  public void println(boolean x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(char x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(char[] x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(double x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(float x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(int x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(long x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(Object x)
  {
    print(x);
    doPrintln();
  }

  @Override
  public void println(String x)
  {
    print(x);
    doPrintln();
  }
}
