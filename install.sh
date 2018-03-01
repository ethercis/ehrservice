#!/usr/bin/env bash

echo 'installing third party jars to maven repository'

./install_3rd_party_jars.sh | tee install_jars.output

if grep -Fq "FAIL" install_jars.output ; then
    echo "installation of external jars failed"
    exit 1
else
    echo "Installation of jars succeeded"
fi
echo 'finished installing third party jars'

echo 'Running flyway via Gradle to generate database tables'
./gradlew db:flywayMigrate | tee gradle.output
if grep -Fq "BUILD SUCCESSFUL" gradle.output ; then
    echo "DB tables created"
else
    echo "Could not create DB tables using gradle"
    exit 1
fi