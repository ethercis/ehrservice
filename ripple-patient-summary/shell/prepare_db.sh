#!/bin/bash
# description: Prepare the db for cache summary
#
# SCRIPT created 03-05-2017, CCH
#-----------------------------------------------------------------------------------
export DBHOST=localhost
export DBPORT=5432
export DBNAME=ethercis
export PG_HOME=/usr/pgsql-9.4
export PSQL=${PG_HOME}/bin/psql

echo "#########################################################"
echo "# PREPARING HEADING TABLES                              #"
echo "#########################################################"

${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/prepare_cache_summary_db_1.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
  echo "psql failed while trying to run this sql script" 1>&2
  exit $psql_exit_status
fi

# run template table populate script
echo "#########################################################"
echo "# POPULATING TEMPLATE TABLE FROM KNOWLEDGE BASE         #"
echo "#########################################################"

./set_template_table.sh

echo "#########################################################"
echo "# BUILDING UP CROSS REFERENCE HEADING - TEMPLATES       #"
echo "#########################################################"

${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/prepare_cache_summary_db_2.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
  echo "psql failed while trying to run this sql script" 1>&2
  exit $psql_exit_status
fi

echo "#########################################################"
echo "# CREATING SUMMARY FUNCTIONS       		      #"
echo "#########################################################"

${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/generate_summary_fields.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
  echo "psql failed while trying to run this sql script" 1>&2
  exit $psql_exit_status
fi

${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/cache_composition.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
  echo "psql failed while trying to run this sql script" 1>&2
  exit $psql_exit_status
fi

${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/cache_summary_triggers.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
  echo "psql failed while trying to run this sql script" 1>&2
  exit $psql_exit_status
fi

echo "#########################################################"
echo "# SET THE TRIGGERS		       		      #"
echo "#########################################################"

#${PSQL} -X -U postgres -h $DBHOST -p $DBPORT -f ../sql/set_cache_summary_db_triggers.sql  -d $DBNAME --echo-all --set AUTOCOMMIT=on --set ON_ERROR_STOP=on 

#psql_exit_status=$?

#if [ $psql_exit_status != 0 ]; then
#  echo "psql failed while trying to run this sql script" 1>&2
#  exit $psql_exit_status
#fi