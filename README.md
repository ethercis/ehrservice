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