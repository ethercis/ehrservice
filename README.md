# ehrservice

This project contains the specialized components used to deal with Openehr RM
object and persist them with using a a mixed projection (SQL + NoSQL with
jsonb data type in PostgreSQL 9.4)

Please see the READMEs in each module for more details.

# Building using Gradle

To build the project, Gradle can be downloaded manually and then installed
system wide.
An other option is to use the provided gradle wrapper which does not install
anything on the system. The following examples use the Gradle wrapper
for *NIX. For windows use the `gradlew.bat` file.

To build and test the project using gradle run:

  `./gradlew build`

To install the JARs into the local maven repository so it can be used by
other projects run:

  `./gradlew install`

Some (older) versions of IntelliJ IDEA have difficulties importing Gradle
projects. If this is the case, Gradle can create IDEA project files by
running:

  `./gradlew idea`

To find out what other tasks are available, run:

  `./gradlew tasks`


# Setting up the database

## For testing

The gradle build uses flyway to manage the database schemas but before
it can manage the schemas, a database must be created.

First, the following Postgresql extentions need to be installed manually:

- *uuid-ossp* (on Debian this is in the package `postgresql-contrib`)
- *ltree* (on Debian this is in the package `postgresql-contrib`)
- *temporal_tables* ( http://pgxn.org/dist/temporal_tables/ )
- *jsquery* ( https://github.com/postgrespro/jsquery )

Then run the script to create a new database and configure the extensions.
See the db/createdb.sql for more details :

  `sudo -u postgresql psql < db/createdb.sql`

(only required once)

After this, the Gradle build will take care of creating the database schemas
as needed.

To empty the database, run

  `./gradlew flaywayClean`

this will remove all data from the database.

## For production

The database schemas and migration scripts can be archived into a single jar
and then deployed on the production server using a command line version
Flyway.
