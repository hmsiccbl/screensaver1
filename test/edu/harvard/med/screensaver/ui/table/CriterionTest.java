// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

import org.apache.log4j.Logger;

// TODO: test other operators
public class CriterionTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(CriterionTest.class);


  @Override
  protected void setUp() throws Exception
  {
  }

  public void testLikeOperator()
  {
    String data = "Hello World.\nHow *are* you?";
    Operator operator = Operator.TEXT_LIKE;
    assertNotMatches(operator, "Hello", data);
    assertNotMatches(operator, "helLo", data);
    assertMatches(operator, "Hello*", data);
    assertMatches(operator, "hello*", data);
    assertNotMatches(operator, "Hello\\*", data);
    assertMatches(operator, "*Hello*", data);
    assertMatches(operator, "*World*", data);
    assertMatches(operator, "*World*How*", data);
    assertNotMatches(operator, "Hello World?", data);
    assertMatches(operator, "Hello World?*", data);
    assertMatches(operator, "hello world*", data);
    assertMatches(operator, "hello world.*How*", data);
    assertNotMatches(operator, "Hello World\\?", data);
    assertMatches(operator, "?ello World*", data);
    assertMatches(operator, "Hello World*you\\?", data);
    assertMatches(operator, "Hello World*you?", data);
    assertNotMatches(operator, "Hello World*you\\??", data);
    assertNotMatches(operator, "?", data);
    assertMatches(operator, "?*", data);
    assertNotMatches(operator, "\\?", data);
    assertMatches(operator, "*", data);
    assertNotMatches(operator, "\\*", data);
    assertMatches(operator, "*\\?", data);
    assertNotMatches(operator, "\\*???\\*", data);
    assertMatches(operator, "*\\*???\\**", data);
    assertNotMatches(operator, "\\**\\*", data);
    assertMatches(operator, "*\\**\\**", data);

    doTestEmptyAndNullCasesWithOperator(operator);
  }

  public void testStartsWithOperator()
  {
    String data = "Hello World.\nHow *are* you?";
    Operator operator = Operator.TEXT_STARTS_WITH;
    assertMatches(operator, "Hello", data);
    assertMatches(operator, "helLo", data);
    assertMatches(operator, "Hello*", data);
    assertMatches(operator, "*Hello", data);
    assertMatches(operator, "*ello", data);
    assertNotMatches(operator, "Jello", data);
    assertMatches(operator, "?", data);
    assertMatches(operator, "*", data);
    assertNotMatches(operator, "\\?", data);
    assertNotMatches(operator, "\\*", data);
    assertMatches(operator, "*\\?", data);
    assertMatches(operator, "*\\*", data);
    assertNotMatches(operator, "\\*???\\*", data);
    assertNotMatches(operator, "\\**\\*", data);
    assertMatches(operator, "*\\*???\\*", data);
    assertMatches(operator, "*\\**\\*", data);


    doTestEmptyAndNullCasesWithOperator(operator);
  }

  public void testContainsOperator()
  {
    String data = "Hello World.\nHow *are* you?";
    Operator operator = Operator.TEXT_CONTAINS;
    assertMatches(operator, "Hello", data);
    assertMatches(operator, "Hello*you?", data);
    assertMatches(operator, "helLo", data);
    assertMatches(operator, "World", data);
    assertMatches(operator, "*World*", data);
    assertMatches(operator, "World*How", data);
    assertMatches(operator, "World??How", data);
    assertMatches(operator, "?", data);
    assertMatches(operator, "*", data);
    assertMatches(operator, "\\?", data);
    assertMatches(operator, "\\*", data);
    assertMatches(operator, "\\*???\\*", data);
    assertMatches(operator, "\\**\\*", data);

    doTestEmptyAndNullCasesWithOperator(operator);
  }

  public void testRegexCharactersAreIgnoredInUserExpression()
  {
    assertNotMatches(Operator.TEXT_LIKE, ".", "x");
    assertMatches(Operator.TEXT_LIKE, ".", ".");
    assertMatches(Operator.TEXT_LIKE, "..", "..");
    assertNotMatches(Operator.TEXT_LIKE, "..", ".x");
    assertMatches(Operator.TEXT_LIKE, ".*", ".x");
    assertMatches(Operator.TEXT_LIKE, ".*", ".x");
    assertMatches(Operator.TEXT_LIKE, ".*", ".*");
    assertNotMatches(Operator.TEXT_LIKE, ".*", "x*");
    assertMatches(Operator.TEXT_LIKE, "*", "*");
    assertMatches(Operator.TEXT_LIKE, "\\\\x", "\\x");
    assertMatches(Operator.TEXT_LIKE, "\\\\x\\?", "\\x?");
  }

  public void testEmptyOperator()
  {
    Operator operator = Operator.EMPTY;
    assertMatches(operator, null, null);
    assertMatches(operator, null, "");
    assertNotMatches(operator, null, "x");
    assertNotMatches(operator, "", "x");
    assertMatches(operator, "x", null);
    assertMatches(operator, "x", "");
  }

  private void doTestEmptyAndNullCasesWithOperator(Operator operator)
  {
    assert operator != Operator.EMPTY : "cannot test the EMPTY operator with this test method; it is a special case!";
    assertMatches(operator, null, null);
    assertMatches(operator, null, "");
    assertMatches(operator, null, "x");
    assertMatches(operator, "", "x");
    assertNotMatches(operator, "x", null);
    assertNotMatches(operator, "x", "");
  }

  private void assertMatches(Operator op, Object value, Object data)
  {
    assertMatches(op, value, data, true);
  }

  private void assertNotMatches(Operator op, Object value, Object data)
  {
    assertMatches(op, value, data, false);
  }

  private void assertMatches(Operator op, Object value, Object data, boolean expectedResult)
  {
    Criterion<Object> criterion = new Criterion<Object>();
    criterion.setValue(value);
    criterion.setOperator(op);
    if (criterion.isUndefined()) {
      // undefined criterion does not filter out data
      assertTrue(criterion.matches(data));
    }
    else {
      assertEquals(expectedResult, criterion.matches(data));
    }
  }
}
