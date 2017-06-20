#!/bin/sh

# This script runs before the database migrations are applied and creates the required
# extensions.
# These extension can not be created by Flyway as they require super user privileged and/or
# can not be installed inside a transaction.
#
# This script is also run on the server during deployment.
#
# Extentions are installed in a separate schema called 'ext'
#
# This needs to be converted into something plaform independend

# Set variables are:
# DBNAME, DBUSER, DBHOST, DBPASS

# normal (local development, server deployment)
CMD="sudo -u postgres psql"

$CMD $DBNAME << EOF
CREATE SCHEMA IF NOT EXISTS ext AUTHORIZATION $DBUSER;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA ext;
CREATE EXTENSION IF NOT EXISTS "temporal_tables" SCHEMA ext;
CREATE EXTENSION IF NOT EXISTS "jsquery" SCHEMA ext;
CREATE EXTENSION IF NOT EXISTS "ltree" SCHEMA ext;
ALTER DATABASE $DBNAME SET search_path TO "\$user",public,ext;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA ext TO $DBUSER;
EOF
