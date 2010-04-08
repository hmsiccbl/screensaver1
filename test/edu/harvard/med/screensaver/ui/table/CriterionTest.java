// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

// TODO: test other operators
public class CriterionTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(CriterionTest.class);


  @Override
  protected void setUp() throws Exception
  {
  }
  
  public void testEqualityOperators()
  {
    for (Operator operator : Arrays.asList(Operator.EQUAL, Operator.NOT_EQUAL)) {
      boolean expectedMatch = operator == Operator.EQUAL;
      assertMatches(operator, "text", "text", expectedMatch); 
      assertMatches(operator, "text", "txet", !expectedMatch);
      assertMatches(operator, new Integer(1), new Integer(1), expectedMatch); 
      assertMatches(operator, new Integer(0), new Integer(1), !expectedMatch); 
      assertMatches(operator, new BigDecimal("1.0"), new BigDecimal("1.0"), expectedMatch); 
      assertMatches(operator, new BigDecimal("0.0"), new BigDecimal("1.0"), !expectedMatch);
      assertMatches(operator, new Double("1.0"), new Double("1.0"), expectedMatch);
      assertMatches(operator, new Double("0.0"), new Double("1.0"), !expectedMatch);
      assertMatches(operator, Boolean.TRUE, Boolean.TRUE, expectedMatch);
      assertMatches(operator, Boolean.TRUE, Boolean.FALSE, !expectedMatch);
      assertMatches(operator, new LocalDate(2000, 1, 1), new LocalDate(2000, 1, 1), expectedMatch);
      assertMatches(operator, new LocalDate(2000, 1, 1), new LocalDate(2000, 1, 2), !expectedMatch);
    }
    assertMatches(Operator.EQUAL, new Integer(1), Arrays.asList(new Integer(1), new Integer(2)), true); 
    assertMatches(Operator.EQUAL, new Integer(1), Arrays.asList(new Integer(0), new Integer(2)), false);
  }

  public void testRankingOperators()
  {
    for (Operator operator : Arrays.asList(Operator.GREATER_THAN, Operator.GREATER_THAN_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_EQUAL)) {
      boolean expectedEqualityMatches = operator.name().contains("EQUAL");
      boolean expectedGreaterThanMatches = operator.name().contains("GREATER");
      boolean expectedLessThanMatches = operator.name().contains("LESS");
      assertMatches(operator, "text1", "text1", expectedEqualityMatches); 
      assertMatches(operator, "text2", "text1", expectedLessThanMatches);
      assertMatches(operator, "text1", "text2", expectedGreaterThanMatches);
      assertMatches(operator, new Integer(1), new Integer(1), expectedEqualityMatches); 
      assertMatches(operator, new Integer(1), new Integer(0), expectedLessThanMatches); 
      assertMatches(operator, new Integer(0), new Integer(1), expectedGreaterThanMatches); 
      assertMatches(operator, new BigDecimal("1.0"), new BigDecimal("1.0"), expectedEqualityMatches); 
      assertMatches(operator, new BigDecimal("1.0"), new BigDecimal("0.0"), expectedLessThanMatches);
      assertMatches(operator, new BigDecimal("0.0"), new BigDecimal("1.0"), expectedGreaterThanMatches);
      assertMatches(operator, new Double("1.0"), new Double("1.0"), expectedEqualityMatches);
      assertMatches(operator, new Double("1.0"), new Double("0.0"), expectedLessThanMatches);
      assertMatches(operator, new Double("0.0"), new Double("1.0"), expectedGreaterThanMatches);
      assertMatches(operator, Boolean.TRUE, Boolean.TRUE, expectedEqualityMatches);
      assertMatches(operator, Boolean.TRUE, Boolean.FALSE, expectedLessThanMatches);
      assertMatches(operator, Boolean.FALSE, Boolean.TRUE, expectedGreaterThanMatches);
      assertMatches(operator, new LocalDate(2000, 1, 1), new LocalDate(2000, 1, 1), expectedEqualityMatches);
      assertMatches(operator, new LocalDate(2000, 1, 2), new LocalDate(2000, 1, 1), expectedLessThanMatches);
      assertMatches(operator, new LocalDate(2000, 1, 1), new LocalDate(2000, 1, 2), expectedGreaterThanMatches);
      assertMatches(operator, new Integer(1), Arrays.asList(new Integer(1), new Integer(1)), expectedEqualityMatches); 
      assertMatches(operator, new Integer(2), Arrays.asList(new Integer(0), new Integer(1)), expectedLessThanMatches);
      assertMatches(operator, new Integer(0), Arrays.asList(new Integer(1), new Integer(2)), expectedGreaterThanMatches);
    }
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

  public void testAnyOperator()
  {
    Operator operator = Operator.ANY;
    assertMatches(operator, null, null);
    assertMatches(operator, null, "");
    assertMatches(operator, null, "x");
    assertMatches(operator, "", "x");
    assertMatches(operator, "x", null);
    assertMatches(operator, "x", "");
  }

  public void testEmptyOperators()
  {
    Operator operator = Operator.EMPTY;
    assertMatches(operator, null, null);
    assertMatches(operator, null, "");
    assertNotMatches(operator, null, "x");
    assertNotMatches(operator, "", "x");
    assertMatches(operator, "x", null);
    assertMatches(operator, "x", "");
    
    operator = Operator.NOT_EMPTY;
    assertNotMatches(operator, null, null);
    assertNotMatches(operator, null, "");
    assertMatches(operator, null, "x");
    assertMatches(operator, "", "x");
    assertNotMatches(operator, "x", null);
    assertNotMatches(operator, "x", "");
  }

  public void testListValue()
  {
    Operator operator = Operator.TEXT_CONTAINS;
    assertMatches(operator, null, Arrays.asList("x", "y", "z"));
    assertMatches(operator, "x", Arrays.asList("x", "y", "z"));
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
