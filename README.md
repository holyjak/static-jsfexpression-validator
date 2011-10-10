Static JSF EL Expression Validator
==================================

Perform analysis of (selected) JSF 1.1 JSP files and validate that
all EL expressions reference only existing managed beans and their
properties/action methods.

USAGE
-----
See the main class net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer
and perhaps try to run it from the command line (it prints usage info).

MORE INFO
---------

See detailed description of how to use the tool at [the blog post validating-jsf-el-expressions-in-jsf-pages-with-static-jsfexpression-validator]
(http://theholyjava.wordpress.com/2011/06/22/validating-jsf-el-expressions-in-jsf-pages-with-static-jsfexpression-validator/)

TODO
----

- introduce analyzer.withManagedBeansAndVariablesConfiguration

- run Sonar & Findbugs ???

- consider using more modern jasper for JSF2.x than 6.0.29 used in 1.2

- remove confusing $$EnhancerByMockitoWithCGLIB from var/property names in failure reports

- finishing touches:
    - add addFunctionReturnTypeOverride -> FakingMethodMapper

- report all functions found (and of course not validated)

- better support for view file filtrering (includes/excludes)
- JsfElFilter should take something more reasonable than ParsedElExpression - remove setters, ref.to Iterator<ElSegment>, incl.file name, tag, line
    - how does it work with "#{b\[nestedBean.itsProp].property + anotherBean}"?
- Modify JsfAnalyzer: setters to return this

- full JSF 1.2+ support:
    - Facelets (the Facelets page says it has compile-type EL validation?!)
    - detect annotated managed beans - use http://code.google.com/p/reflections/ (@ManagedBean(name="userBean") + configurable [Spring, EJB, CDI, ...])
    - support EL functions (not just 'function tolerance' as implemented now)

- add (more) example JSF page
- test with various JSF projects
- better error msgs - see below
 => make it clear the solution is st. like propertyTypeOverrides.put("bean.listProperty.*",  TheElementType.class);
- check JavaDoc, remove/correct references to refactored functionality

---
- don't mock implementations of Map/Collection, instantiate them instead (eg ArrayList)
- consider parallelization (take ~30s for our 200 pages on my PC)
- better handling of includes:
    1) possibility to exclude pages from processing (the non-standalone ones)
    2) whenever dynamic jsp include encountered, switch to processing of that page, passing local variables in

### TODO - improve error messages ###
#### Ex.1.: PropertyNotFoundException on class Error_YouMustDelcareTypeForThisVariable
- include advice how to solve it

#### Ex.2.: Forgotten .* in property override
> Invalid EL expression 'List item value: #{myCollectionBean.list[0].value}': PropertyNotFoundException - Property '0' not found
> on type net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.testbean.MyCollectionBean$ValueHolder$$EnhancerByMockitoWithCGLIB$$ec64146];
> expression=List item value: #{myCollectionBean.list[0].value}, file=/testLocalVariables.jsp, tagLine=32]

- add tip that the user might have forgotten the .* if we can guess that it would help

#### Ex.3.: Missing property override
> Invalid EL expression 'List item value: #{myCollectionBean.list[0].value}': PropertyNotFoundException - Property
> 'value' not found on type net.jakubholy.jeeutils.jsfelcheck.validator.MockObjectOfUnknownType$$EnhancerByMockitoWithCGLIB$$f3296ef8];
> expression=List item value: #{myCollectionBean.list[0].value}, file=/testLocalVariables.jsp, tagLine=32]

 - property not found & collection => advice to use `withPropertyOverride`

#### Ex.4.:
> Invalid EL expression 'Unknown managed bean - extra variable: #{iAmExtraVariable}': VariableNotFoundException -
> No variable 'iAmExtraVariable' among the predefined ones.];
> expression=Unknown managed bean - extra variable: #{iAmExtraVariable}, file=/testLocalVariables.jsp, tagLine=35]

 - advice to declare it either as a local variable or with `withExtraVariable`


DEVELOPEMET INTRO
-----------------

### Key classes
 - AbstractJsfStaticAnalyzer - the main method, wires all together
 - JsfElValidatingPageNodeListener - drives the validation based on JSP tags being processed
    and composes the failures/successes report
 - ValidatingJsfElResolver - actual checks of JSF EL expressions

---

 - net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeExpressionValidator.isMethodBinding(String) - distinguish value and method binding

### Note on JSF 1.2 and 2.0 ###

http://www.roseindia.net/jsf/jsf-versions.shtml

Expression Language, referred to as EL, was first introduced in JSTL 1.0, and later included in JSP 2.0.
  A variant of the EL was used in JSF 1.0. In JSP 2.1, JSF 1.2 and JSTL 1.2, an unified EL was defined.

 JSF 1.1 (27 may 2004) -   Bug fix release. No specification changes.
 
 JSF 1.2(11 may 2006)
  - Unified EL for JSP and JSF, introduced ELResolver => support for setters, calling methods

 JSF 2.0
  - The Java EE 6 EL: "The expression language has changed in minor ways. Perhaps the most useful enhancement for JSF programmers is the use of arguments in action methods."

#### IMPLEMENTATIONS
 
  - JSF 1.2: org.apache.tomcat:el-api:6.0.29 and org.apache.tomcat:jasper-el:6.0.29
  - JSF 2.0: likely org.apache.tomcat:tomcat-jasper-el:7.0.16 and org.apache.tomcat:tomcat-el-api:7.0.16

#### Note:
  - JSF 2.0 <=> JavaEE 6 => Servlet 3.0 and JSP/EL 2.2 => Tomcat 7.0.x
  - JSF 1.2 <=> JavaEE 5 => Servlet 2.5 and JSP 2.1 =>Tomcat 6.0.x

#### Sources:
 
  - http://en.wikipedia.org/wiki/JavaServer_Faces#JSF_versions
  - and http://en.wikipedia.org/wiki/Java_EE_version_history#Java_EE_5_.28May_11.2C_2006.29
  - and http://tomcat.apache.org/whichversion.html

Interesting Links
-----------------
- http://stackoverflow.com/questions/4441713/migrating-from-jsf-1-2-to-jsf-2-0

---------
