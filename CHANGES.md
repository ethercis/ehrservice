This is a fork of https://github.com/ethercis/ehrservice with the
following modifications:

* build using gradle
* do not depend on external openehr libraries but they are all included in
  the openehr directory
* all modifications to the openehr libraries are merged into the openehr
  directory
* disabled all code related to ripple-patient-summary
* various bug fixes also available as separate pull requests
* added bb-test and test-data which contains some black-box tests
* disabled quite a large number of tests due to missing resources
* database schema is managed by flyway

TODO
====

* get all tests working to check our changes did not mess something up
* see if VirtualEhr can use a library using the new build
* clean up redundant files (.svn, generated sources, openehr files ourside
  the openehr dir, etc)
* update dependencies to the latest version


Usage
=====

Building and testing:

* install all required extensions on the system:
  * uuid-ossp
    * https://www.postgresql.org/docs/current/static/uuid-ossp.html
    * install using `apt-get install postgresql-contrib`
  * temporal_tables
    * https://github.com/arkhipov/temporal_tables
  * jsquery
    * https://github.com/postgrespro/jsquery
  * ltree
    * https://www.postgresql.org/docs/current/static/ltree.html
    * install using `apt-get install postgresql-contrib`
* create a database (build assumes a postgreSQL database named `ethercis`
  with user and password `ethercis` but can be changed in `build.gradle`)
* build and run all tests:
  `./gradlew build`


Please note the build currently uses a bash shell script which uses sudo
to install the required extensions in the database. This is only a temporary
solution.


Generate project files for IntelliJ IDEA
========================================

run `./gradlew idea`

