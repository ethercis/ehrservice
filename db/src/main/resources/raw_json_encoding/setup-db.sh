#!/bin/bash
# description: Prepare the db for in db json encoding of composition (Phase I)
#
# SCRIPT created 24-01-2018, CCH
#-----------------------------------------------------------------------------------
export DBHOST=localhost
export DBPORT=5433
export DBNAME=ethercis
export PG_HOME=/usr/pgsql-10
export PSQL=${PG_HOME}/bin/psql

JSENCODE_ARRAY=(\
                "archetyped.sql" "code_phrase.sql" "context.sql" "context_setting.sql" "dv_coded_text.sql" "dv_date_time.sql" "dv_text.sql"\
                "iso_timestamp.sql" "json_composition_pg10.sql" "object_version_id.sql" "party.sql" "party_identified.sql" "party_ref.sql"
                )

echo "#########################################################"
echo "# PREPARING HEADING TABLES                              #"
echo "#########################################################"

for jscript in ${JSENCODE_ARRAY[@]}; do
${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ${jscript}  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on
done

exit 0