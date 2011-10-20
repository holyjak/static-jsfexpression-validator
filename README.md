Static JSF EL Expression Validator
==================================

Perform analysis of JSF 1.1/1.2/2.x JSP files and validate that
all EL expressions reference only existing managed beans and their
properties/action methods.

USAGE
-----

See the main class [net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer](https://github.com/jakubholynet/static-jsfexpression-validator/blob/master/static-jsfexpression-validator-core/src/main/java/net/jakubholy/jeeutils/jsfelcheck/AbstractJsfStaticAnalyzer.java#L140)
and perhaps try to run it from the command line (it prints usage info).

DOWNLOADS
---------

You can get the binaries via Maven or manually from the Maven Central Repository. Select version compatible with your JSF:

1. JSF 1.1: [static-jsfexpression-validator-jsf11](http://search.maven.org/#browse%7C1186899637)
1. JSF 1.2: [static-jsfexpression-validator-jsf12](http://search.maven.org/#browse%7C1186899668)
1. JSF 2.x: [static-jsfexpression-validator-jsf20](http://search.maven.org/#browse%7C1186900567)

MORE INFO
---------

See detailed description of how to use the tool at [the blog post validating-jsf-el-expressions-in-jsf-pages-with-static-jsfexpression-validator]
(http://theholyjava.wordpress.com/2011/06/22/validating-jsf-el-expressions-in-jsf-pages-with-static-jsfexpression-validator/)

---

TODO - FURTHER DEVELOPMENT
--------------------------

ADD TEST FOR v0.9.6 FIXES
- support imported Spring subconfigx and multiple/... faces configs via a Resource abstraction over files/streams
-  static-jsfexpression-validator-jsf*.jar: Add integration test verifying JSP parsing -> ... => no missing dependencies etc.
- make sure all -jsf* modules have correct dependencies on jasper and the *jasper-el it needs to be able to parse JSPs
- how is it possible we now can get ReferenceSyntaxException: ${notesParsedXml} (not #{}) which we hadn't before?
 Should we always ignore ${}, even for JSF1.2/2.0? (i.e. check deferenced eval. expr. only)
- why there is mockito-core in  taget/test-webapp-jsf11/WEB-INF/lib ?

- net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeExpressionValidator.isMethodBinding - make names configurable?

- finishing touches:
    - add addFunctionReturnTypeOverride -> MethodFakingFunctionMapper

- report all functions found (and of course not validated (SUPPORT PARTLY IMPLEMENTED))

- better support for view file filtrering (includes/excludes)

- JsfElFilter should take something more reasonable than ParsedElExpression - remove setters, ref.to Iterator<ElSegment>, incl.file name, tag, line
    - how does it work with "#{b\[nestedBean.itsProp].property + anotherBean}"?

---

- full JSF 1.2+ support:
    - Facelets (the Facelets page says it has compile-type EL validation?!)
    - detect annotated managed beans - use http://code.google.com/p/reflections/ (@ManagedBean(name="userBean") + configurable [Spring, EJB, CDI, ...])
    - support EL functions (not just 'function tolerance' as implemented now)

- run Sonar & Findbugs ???

- FIX: MethodFakingFunctionMapper currently allows only 1 arity for a function, i.e. not having the same fun name with different number of arguments

- add (more) example JSF page
- test with various JSF projects ?
- better error msgs - see below
 => make it clear the solution is st. like propertyTypeOverrides.put("bean.listProperty.*",  TheElementType.class);
- check JavaDoc, remove/correct references to refactored functionality

---
- don't mock implementations of Map/Collection, instantiate them instead (eg ArrayList)
- consider using more modern jasper for JSF2.x than 6.0.29 used in 1.2
- consider parallelization (take ~30s for our 200 pages on my PC)
- better handling of includes:
    1) possibility to exclude pages from processing (the non-standalone ones)
    2) whenever dynamic jsp include encountered, switch to processing of that page, passing local variables in

---
Architecture refactoring: Stateless objects and message passing through a central delegator with support
for plugging-in filters. Key components:
- Message: Contains Context, input, output (may be of the same type as the input). Context holds the
    static global state (e.g. user-registered local vars), public state from upstream workers
    (e.g. current EL), and private state of each worker that needs it (e.g. per-page cache of functions),
    and indicator whether processing of the current resource (page/tag/EL) should continue or not
    (for filtering, ...)
- WorkHub:
    - has delegateWork (exp. sb. to transform the input into output) and notifyOf (no recipient required)
    - creates instances of work processors un demand, initializes them with itself/other stuff
- Worker: Has an instance of WorkHub
=> advantages:1) Easy to do multithreaded processing; 2) Powerful & flexible arch. for users: the can plug in
    filters that reject resources or modify messages (inputs/outputs) or just collect info for reporting/stats
- Global shared state objects: E.g. cache shared across all threads (or force them into the ctx?)
- Issues
    - complicates interfaces and thus reuse outside of JsfElV, e,g, Result validate(String EL) => Event val.(Event)
    - storing private state in the ctx makes it more expensive to access (logN, negligible?), sb.
        else can possibly modify it
    - how to pass state needed in collbacks? (easy with instance var., not so with ctx as method param.)
    - system initialization (Compiler is created by Jasper via new C.() => can't get objects from outside)
    - how to handle exceptions in listeners/workers?
    - ...

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

NOTES
-----

### Version 0.9.6

Quick bugfix release:
- support imported Spring subconfigx and multiple/... faces configs
- static-jsfexpression-validator-jsf11.jar: Add missing dependencies on jasper-el to avoid ClassNotFound ex. when parsing
- fixed EL expression recognizer to ignore immediate evaluation expr. (${..}) and only accept ordinary JSF (deffered eval.) ones
- added File... version of from* methods also for the non-static ones in Man.BeansAndVarConfig
