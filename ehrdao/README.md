Ethercis EhrDAO
===============

EhrDAO implement the logic to persist openEhr entities in a DB using a mixed representation (SQL, NoSQL). This current
implementation relies on PostgreSQL 9.4 with jsonb data types.

The main components include:

- The classes used to persist openEhr objects. Use of these classes should be done with their corresponding interfaces.
- A utility class to perform various I/O's on the DB based on Path/Value pairs (PvCompoHandler)
- Support utility to deal with terminology: TerminologySetter (terminology concepts are actually stored in a table)
- Utility to dump paths fromBinder a composition: CompositionUtil (useful if the original template is not available)
- A utility to populate the Concept table fromBinder terminology.xml. The concept table is used to ease SQL queries on specif concept(s).

On the data access specifically, the following classes are provided:

- CompositionAccess: operations on the *static* part of Compositions (eg. non archetyped attributes)
- ConceptAccess: operations on the Context part of a Composition
- EntryAccess: operations on the Entry part of a Composition (Entry is archetyped and uses json serialization)
- EhrAccess & StatusAccess: operations on a Ehr
- ContributionAccess: keeps track of changes of Ehrs and Compositions
- PartyIdentifedAccess: manages PartyIdentified objects


How To Compile The Module
-------------------------
REQUIREMENTS

- Java 1.8 or higher
- Maven 3.3 or higher

INSTALLATION

The compilation and artifact generation is performed by `mvn clean install`.

Temporal data extension must be installed on the DB. This extension can be found at:
`http://pgxn.org/dist/temporal_tables/`

Runtime Parameter
-----------------

**ServerNodeId**

The server node id is used to refer the *domain name* of a versioned object id:

UUID::*domain name*::version

It is set by a JVM parameter, for example:

`-Dserver.node.name=testsg.ethercis.org`



Dependencies
------------
Dependencies that are not resolved by Maven should be located in a local repository. An archive containing these local
dependencies is provided on this site:

- openEHR.v1.OperationalTemplate -- XmlBeans compilation of schema Template.xsd (see in main/resources/schemas)
- openEHR.v1.Template -- XmlBeans compilation of schema CompositionTemplate.xsd (see in main/resources/schemas)
- openehr-am-rm-term -- subset of openehr java ref library with AM, RM and TERMINOLOGY
- oet-parser -- openehr java ref library openehr internal template parser

Tests
-----

Tests are disabled in POM.XML.

Known issues
============

2015/12/23

- Default System settings retrieval is not OS dependent (SystemAccess)
- Composition update using canonical XML is not supported (it is all or nothing)
- Jsonb data type is dealt with the SQL way... JSON type should be handled as described in
  `https://github.com/jOOQ/jOOQ/issues/2788`