Ethercis Aql Processor
======================

Processing of AQL queries. AQL is further define at: https://openehr.atlassian.net/wiki/display/spec/AQL-+Archetype+Query+Language

The AQL grammar is compliant with ANTLR4. It can be found in src/main/antlr4

This implementation is performing a pre-processing of an AQL query expression into SQL query parts:

- Part1: (compilation pass 1) resolves the CONTAINS clause. This resolution allows to 
 - identify the conditional part of the SQL construction (ex. composition composer) from the archetyped ones (ex. diastolic in blood pressure archetype)
 - build a symbol map in which actual AQL paths will be resolved depending on selected composition
- Part 2: (compilation pass 2) encode all other clauses: SELECT, FROM, WHERE, TOP, ORDER BY

Once, the AQL expression is translated into its respective parts, the query is processed:

If the SELECT/WHERE expression contains AQL path based field, the processing is iterative on selected compositions; for
each retrieved composition, paths resolution take place (from table CONTAINMENT) and the field in the SELECT/WHERE 
can be completed. The assembled SQL query is performed.
If the SELECT/WHERE contains only non archetyped field expression, the SQL is directly assembled and the query is performed.

NB. SQL query is performed against view COMP_EXPAND

TODO:
- Further optimization and query planning

Function Support
----------------
A number of postgresql functions are now supported:

### Aggregate Functions

COUNT, AVG, BOOL_AND, BOOL_OR, EVERY, MAX, MIN, SUM

### Statistics Functions

#### correlation
CORR

#### covariance
COVAR_POP, COVAR_SAMP

#### regression 
REGR_AVGX, REGR_AVGY, REGR_COUNT, REGR_INTERCEPT, REGR_R2, REGR_SLOPE, REGR_SXX,
REGR_SXY, REGR_SYY

#### standard deviation
STDDEV, STDDEV_POP, STDDEV_SAMP, VARIANCE, VAR_POP, VAR_SAM

See [https://www.postgresql.org/docs/9.4/static/functions-aggregate.html](https://www.postgresql.org/docs/9.4/static/functions-aggregate.html "Postgresql aggregate functions") for more details

### String Functions
SUBSTR, STRPOS, SPLIT_PART, BTRIM, CONCAT, CONCAT_WS, DECODE, ENCODE, FORMAT, INITCAP, LEFT, LENGTH, LPAD
LTRIM, REGEXP_MATCH, REGEXP_REPLACE, REGEXP_SPLIT_TO_ARRAY, REGEXP_SPLIT_TO_TABLE, REPEAT, REPLACE, REVERSE
RIGHT, RPAD, RTRIM, TRANSLATE

See [https://www.postgresql.org/docs/9.4/static/functions-string.html](https://www.postgresql.org/docs/9.4/static/functions-string.html) for more details.

### Limitations
Embedded expressions or functions are not yet supported.


How To Compile The Module
-------------------------
REQUIREMENTS

- Java 1.8 or higher
- Maven 3.3 or higher

INSTALLATION

The compilation and artifact generation is performed by `mvn clean install`.

Dependencies
------------
ANTLR4

Tests
-----

Known issues
============
This currently supports a limited set of AQL operators