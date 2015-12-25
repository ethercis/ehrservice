Ethercis EhrDAO
===============

EhrDAO implement the logic to persist openEhr entities in a DB using a mixed representation (SQL, NoSQL). This current
implementation relies on PostgreSQL 9.4 with jsonb data types.

The main components include:

- The classes used to persist openEhr objects. Use of these classes should be done with their corresponding interfaces.
- A utility class to perform various I/O's on the DB based on Path/Value pairs (PvCompoHandler)
- Support utility to deal with terminology: TerminologySetter (terminology concepts are actually stored in a table)
- Utility to dump paths from a composition: CompositionUtil (useful if the original template is not available)
- A utility to populate the Concept table from terminology.xml. The concept table is used to ease SQL queries on specif concept(s).

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

GENERATING THE DB STRUCTURE

Assuming a DB 'ethercis' is already created in the PostgreSQL running instance, DDL file `resources/ddls/pgsql-ehr.ddl`
can be used to generate the required tables, relations, constraints and triggers.

NB. Please make sure extension *uuid-ossp* is available on your system (this is required to generate default UUID with *uuid\_generate\_v4()*. More details on this at: `http://www.postgresql.org/docs/9.4/static/uuid-ossp.html`

COMPILING jOOQ DB Binding

Directory resources contains a bat script used to generate the binding classes. It is called as follows:

`jooq-codegen ecis-dbgen.xml`

Runtime Parameter
-----------------

**ServerNodeId**

The server node id is used to refer the *domain name* of a versioned object id:

UUID::*domain name*::version

It is set by a JVM parameter, for example:

`-Dserver.node.name=testsg.ethercis.org`



Dependencies
------------
The main dependency is jOOQ 3.5 which is extensively used to perform SQL queries in a neat way (jOOQ stands for *java object oriented query*). See `http://www.jooq.org/` for more details on this great library.

Dependencies that are not resolved by Maven should be located in a local repository. An archive containing these local
dependencies is provided on this site:

- openEHR.v1.OperationalTemplate -- XmlBeans compilation of schema Template.xsd (see in main/resources/schemas)
- openEHR.v1.Template -- XmlBeans compilation of schema CompositionTemplate.xsd (see in main/resources/schemas)
- openehr-am-rm-term -- subset of openehr java ref library with AM, RM and TERMINOLOGY
- oet-parser -- openehr java ref library openehr internal template parser

Tests
-----
Several tests are dependent on existing openehr compositions, archetypes and templates. They should be adapted to your
particular environment, in particular path to access the knowledge models.

Tests are disabled in POM.XML.

Known issues
============

2015/12/23

- Default System settings retrieval is not OS dependent (SystemAccess)
- Composition update using canonical XML is not supported (it is all or nothing)
- Jsonb data type is dealt with the SQL way... JSON type should be handled as described in
  `https://github.com/jOOQ/jOOQ/issues/2788`