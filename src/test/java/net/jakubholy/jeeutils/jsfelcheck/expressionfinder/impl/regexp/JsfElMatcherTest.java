package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.regexp;


import static org.junit.Assert.*;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.regexp.ExpressionInfo.ElType;

import org.junit.Before;
import org.junit.Test;

public class JsfElMatcherTest {
	
	private static final ExpressionInfo.ElType EXP_VALUE = ExpressionInfo.ElType.VALUE;
	private static final ExpressionInfo.ElType EXP_METHOD = ExpressionInfo.ElType.METHOD;

	//private JsfElMatcher matcher;
	
	

	@Test
	public void should_find_all_value_bindings() {
		String expressions = " space #{simpleBean}"
			+ "#{bean.property}	"
			+ "#{ spacedBean } "
			+ "#{bean['key'].property} EOF";
		
		JsfElMatcher matcher = JsfElMatcher.forText(expressions);
		
		assertEquals(new ExpressionInfo("#{simpleBean}", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{bean.property}", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{ spacedBean }", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{bean['key'].property}", EXP_VALUE), matcher.findNext());
		assertEquals(null, matcher.findNext());
	}
	
	@Test
	public void should_find_simple_method_binding() {
		String expressions = "action='#{bean.method}'"
			+ "action = \" #{ bean2.method2 } \""
			+ "actionListener	=	\"	#{	b3.m3	}	\""
			+ "actionListener= '#{objekt.action}'";
		
		JsfElMatcher matcher = JsfElMatcher.forText(expressions);
		
		assertEquals(new ExpressionInfo("#{bean.method}", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{ bean2.method2 }", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{	b3.m3	}", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{objekt.action}", EXP_METHOD), matcher.findNext());
		assertEquals(null, matcher.findNext());
	}

}
