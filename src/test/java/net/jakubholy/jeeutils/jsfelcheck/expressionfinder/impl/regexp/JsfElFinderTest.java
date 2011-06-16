package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.regexp;
import static org.junit.Assert.*;

import java.util.Iterator;

import net.jakubholy.jeeutils.jsfelcheck.ExpressionInfo;
import net.jakubholy.jeeutils.jsfelcheck.ExpressionInfo.ElType;

import org.junit.Test;



public class JsfElFinderTest {
	
	@Test
	public void should_find_all_expressions() throws Exception {
		
		String expressions = "action='#{bean.handler}'"
				+ "value = '#{bean.property}'";
		
		Iterator<ExpressionInfo> expressionIterator = new JsfElFinder(expressions).iterator();
		
		assertTrue(expressionIterator.hasNext());
		assertEquals(new ExpressionInfo("#{bean.handler}", ExpressionInfo.ElType.METHOD)
			, expressionIterator.next());
		
		assertTrue(expressionIterator.hasNext());
		assertEquals(new ExpressionInfo("#{bean.property}", ExpressionInfo.ElType.VALUE)
			, expressionIterator.next());
		
		assertFalse(expressionIterator.hasNext());
	}

}
