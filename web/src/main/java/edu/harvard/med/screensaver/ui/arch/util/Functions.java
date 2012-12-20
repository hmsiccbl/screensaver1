package edu.harvard.med.screensaver.ui.arch.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

public class Functions {
	  /**
	   * Perform a depth-first traversal of a collection that may contain mixed element types.
	   * Any nested Iterable instances are visited themselves and their contents added to the
	   * collection.
	   * 
	   * @param original an iterable object
	   * @return a collection containing the results of iterating the original object and any iterable descendents
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection flatten(Iterable original) {
		  Collection flattened = new ArrayList();
		  if(original == null)
			  return flattened;
		  
		  Deque stack = new LinkedList();
		  stack.push(original.iterator());
		  
		  while(stack.size() > 0) {
			  Iterator current = (Iterator)stack.pop();
			  
			  while(current.hasNext()) {
				  Object next = current.next();
				  
				  if(next instanceof Iterable) {
					  if(next != null) {
						  stack.push(current);
						  current = ((Iterable)next).iterator();
					  }
				  } else {
					  flattened.add(next);
				  }
			  }
		  }
		  
		  return flattened;
	  }
	  
	  /**
	   * Optimised version of distinct(coalesce(flatten(original))).
	   * 
	   * @param original an iterable object
	   * @return a collection containing the unique, non-empty results of iterating the original object and any iterable descendents
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection compact(Iterable original) {
		  Collection compacted = new LinkedHashSet();
		  if(original == null)
			  return compacted;
		  
		  Deque stack = new LinkedList();
		  stack.push(original.iterator());
		  
		  while(stack.size() > 0) {
			  Iterator current = (Iterator)stack.pop();
			  
			  while(current.hasNext()) {
				  Object next = current.next();
				  
				  if(next == null
				     || "".equals(next)
				     || (next instanceof Collection && ((Collection)next).size() == 0)
				     || (next instanceof Map && ((Map)next).size() == 0)) {
					  continue;
				  } else if(next instanceof Iterable) {
					  if(next != null) {
						  stack.push(current);
						  current = ((Iterable)next).iterator();
					  }
				  } else {
					  compacted.add(next);
				  }
			  }
		  }
		  
		  return compacted;
	  }
	  
	  /**
	   * Concatenate two iterables
	   * 
	   * @param first an iterable object
	   * @param second another iterable object
	   * 
	   * @return a collection containing the combined results of iterating the two objects
	   */
	  @SuppressWarnings({ "rawtypes" })
	  public static Iterable concat(final Iterable first, final Iterable second) {
		  return new Iterable() {
			
			@Override
			public Iterator iterator() {
				return new Iterator() {
					private Iterator firstIter = first.iterator();
					private Iterator secondIter = second.iterator();
					
					@Override
					public boolean hasNext() {
						return (firstIter.hasNext() || secondIter.hasNext());
					}
					
					@Override
					public Object next() {
						if(firstIter.hasNext())
							return firstIter.next();
						if(secondIter.hasNext())
							return secondIter.next();
						
						throw new NoSuchElementException("No items left in either iterator");
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException("Cannot remove from this iterator");
					}
				};
			}
		  };
	  }
	  
	  /**
	   * Retain the first occurrence of any repeated items in a collection.
	   * 
	   * @param original an iterable object
	   * @return a collection containing the unique items in the collection in the order they are returned by the iterator.
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection distinct(Iterable original) {
		  Collection flattened = new LinkedHashSet();
		  if(original == null)
			  return flattened;
		  
		  Iterator current = original.iterator();
			  
		  while(current.hasNext()) {
			  Object next = current.next();
			  
			  flattened.add(next);
		  }
		  
		  return flattened;
	  }
	  
	  /**
	   * Retain non-empty elements in a collection.
	   * 
	   * An element is empty if it is null, the empty string, a Collection instance with no elements or
	   * a Map instance with no mappings.
	   * 
	   * @param original an iterable object
	   * @return a collection containing the results of iterating the original object but skipping any empty elements.
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection coalesce(Iterable original) {
		  Collection coalesced = new ArrayList();
		  Iterator current = original.iterator();
				 
		  while(current.hasNext()) {
			  Object next = current.next();
				  
			  if(next == null
			     || "".equals(next)
			     || (next instanceof Collection && ((Collection)next).size() == 0)
			     || (next instanceof Map && ((Map)next).size() == 0)) {
				  continue;
			  }
			  coalesced.add(next);
		  }
		  
		  return coalesced;
	  }
	  
	  /**
	   * Evaluate an expression for each element in a collection and return a collection of the evaluation results.
	   * 
	   * @param original an iterable object
	   * @param variable a binding to which the element can be assigned
	   * @param expression a binding (perhaps involving the variable) to return for each element
	   * @return a collection containing the results
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection transform(Iterable original, String variable, String expression) {
		  FacesContext context = FacesContext.getCurrentInstance();
		  Application application = context.getApplication();
		  ValueBinding variableBinding = application.createValueBinding(variable);
		  ValueBinding expressionBinding = application.createValueBinding(expression);
		  
		  Collection transformed = new ArrayList();
		  Iterator current = original.iterator();
				 
		  while(current.hasNext()) {
			  Object next = current.next();
			  
			  variableBinding.setValue(context, next);
			  
			  transformed.add(expressionBinding.getValue(context));
		  }
		  
		  return transformed;
	  }
	  
	  /**
	   * Evaluate an expression for each element in a collection and return a collection containing
	   * those elements where the expression evaluates to Boolean.TRUE.
	   * 
	   * @param original an iterable object
	   * @param variable a binding to which the element can be assigned
	   * @param expression a binding (perhaps involving the variable) to return for each element
	   * 
	   * @return a collection containing the elements where the expression evaluates to true.
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	  public static Collection filter(Iterable original, String variable, String expression) {
		  FacesContext context = FacesContext.getCurrentInstance();
		  Application application = context.getApplication();
		  ValueBinding variableBinding = application.createValueBinding(variable);
		  ValueBinding expressionBinding = application.createValueBinding(expression);
		  
		  Collection filtered = new ArrayList();
		  Iterator current = original.iterator();
				 
		  while(current.hasNext()) {
			  Object next = current.next();
			  
			  variableBinding.setValue(context, next);
			  
			  if(Boolean.TRUE.equals(expressionBinding.getValue(context)))
				  filtered.add(next);
		  }
		  
		  return filtered;
	  }

}
