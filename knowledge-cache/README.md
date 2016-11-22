Ethercis knowledge-cache
========================

Knowledge Cache is used to parse and cache in memory openEhr templates, operational templates and archetypes. The Cache
uses several properties specifying the access paths:

- `knowledge.path.opt` path to operational templates directory
- `knowledge.path.template` path to openEhr templates directory
- `knowledge.path.archetype` path to archetypes directory

The following property specifies if the caching is performed at startup time

- `knowledge.forcecache` **Should be set to `true` in a production environment!**

Knowledge Cache maintains a number of indexes to retrieve quickly the cached object. Templates are referenced by a 
`template Id` which is used in an XML composition for example. For this reason it is recommended to set the `knowledge.forcecache` to true.

Since this module is intended to be wrapped in a service, a number of utility methods are provided to interact with
the instance at runtime with JMX or with a query:

- add a template
- reload the templates
- get the list of current cached objects


How To Compile The Module
-------------------------
REQUIREMENTS

- Java 1.8 or higher
- Maven 3.3 or higher

INSTALLATION

The compilation and artifact generation is performed by `mvn clean install`.

Dependencies
------------
Dependencies that are not resolved by Maven should be located in a local repository. An archive containing these local
dependencies is provided on this site:

- openEHR.v1.OperationalTemplate -- XmlBeans compilation of schema Template.xsd (see in main/resources/schemas)
- openEHR.v1.Template -- XmlBeans compilation of schema CompositionTemplate.xsd (see in main/resources/schemas)
- openehr-am-rm-term -- subset of openehr java ref library with AM, RM and TERMINOLOGY
- adl-parser -- openehr java ref library archetype parser

Tests
-----
Several tests are dependent on existing openehr compositions, archetypes and templates. They should be adapted to your
particular environment, in particular path to access the knowledge models.

Tests are disabled in POM.XML.

Known issues
============

2015/12/23

- at the moment only ADL 1.4 is supported