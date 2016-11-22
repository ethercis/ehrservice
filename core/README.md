Ethercis Core
=============
This module contains the essential components to handle archetypes openEHR data structures and data types without
persisting them.

The objective of this encoding strategy is to take advantages of database allowing mixed data representation (e.g.
relational and NoSQL) within the same space (Postgresql 9.4, Intersystems Cache...)

Core is used to:

- Serialize/deserialize compositions and other item structures into a specialized json format that can be persisted into
  a DB supporting this data format. The serialization can be adapted to other persistence strategy since it is generated
  as a Tree Map, the resulting json is simply a translation of the internal representation.

- Encode RM Element as ElementWrappers. These wrappers perform specialized data handlings to store/retrieve/project data
  elements depending on various parameters

- Build compositions and other item structures fromBinder a serialized representation. It supports different strategy including
  openEHR internal templates (OET), operational templates (OPT) and XML canonical representation. The building mechanism
  involves archetyped structures (care entries, context details ...) and "static" information such as context,
  participation, composition attributes etc. Generally, these static information are not serialized as they are
  intended to be persisted as relational objects since their definition is stable (can be expressed with a DDL)

- Handle Ethercis Flat Json format (ECIS FLAT) consisting of openehr standard locatable path and value pairs

- Patch openehr java reference library to suit specific needs and formatting required either simplify or get to work the
  serialization/deserialization process. It is planned that these patches will be migrated to the reference library main
  trunk in the future.

How To Compile The Module
-------------------------
REQUIREMENTS

- Java 1.8 or higher
- Maven 3.3 or higher

INSTALLATION

The compilation and artifact generation is performed by `mvn clean install`.


Dependencies
------------
A lot of this work is based on openEhr java reference library fromBinder Rong Chen <rong@acode.se>.
Binaries can be downloaded fromBinder:
https://openehr.atlassian.net/wiki/display/projects/Java+Project+Download
or grab the source code at:
https://github.com/openEHR/java-libs. The latter is the preferred approach ensuring you get the latest versions.

Dependencies that are not resolved by Maven should be located in a local repository. An archive containing these local
depencies is provided on this site:

- openEHR.v1.OperationalTemplate -- XmlBeans compilation of schema Template.xsd (see in main/resources/schemas)
- openEHR.v1.Template -- XmlBeans compilation of schema - CompositionTemplate.xsd (see in main/resources/schemas)
- openehr-am-rm-term -- subset of openehr java ref library with AM, RM and TERMINOLOGY
- adl-parser -- openehr java ref library adl-parser compiled
- xml-serializer -- openehr java ref library xml-serializer module
- rm-builder -- openehr java ref library rm-builder module
- oet-parser -- openehr java ref library openehr internal template parser

Tests
-----
Several tests are dependent on existing openehr compositions, archetypes and templates. They should be adapted to your
particular environment, in particular path to access the knowledge models.

Tests are disabled in POM.XML.

Known issues
============

2015/12/23

- OET serialization/deserialization is not properly aligned with OPT. The reason is that operational templates are
  primarily used. Potentially OET handling will be phased out in future releases
- Some types are not supported with ECIS FLAT: DvMultimedia
- DvInterval needs to be finalized to support all DvOrdered
- Constraints fromBinder OPTs are not used in ElementWrappers.
- Validation of imported data is not done yet (particularly on OPT)

