JOOQ Binder for Postgres 9.4
============================

This module uses jOOQ 3.7

How To Compile The Module
-------------------------
REQUIREMENTS

- Java 1.8 or higher
- Maven 3.3 or higher

INSTALLATION

The compilation and artifact generation is performed by `mvn clean install`.

GENERATING THE DB STRUCTURE

Assuming a DB 'ethercis' is already created in the PostgreSQL running instance, DDL file `resources/ddls/pgsql-ehr.ddl`
can be used to generate the required tables, relations, views, constraints and triggers.

##Required extensions
- *uuid-ossp* is available on your system (this is required to generate default UUID with *uuid\_generate\_v4()*. More details on this at: `http://www.postgresql.org/docs/9.4/static/uuid-ossp.html`
- *temporal_tables* http://pgxn.org/dist/temporal_tables/
- *jsquery* https://github.com/postgrespro/jsquery
- *ltree*

COMPILING jOOQ DB Binding

Directory config contains a bat script used to generate the binding classes. It is called as follows:

`jooq-codegen ecis-dbgen.xml`

Dependencies
------------
The main dependency is jOOQ 3.7 which is extensively used to perform SQL queries in a neat way (jOOQ stands for *java object oriented query*). See `http://www.jooq.org/` for more details on this great library.

Tests
-----

Known issues
============
