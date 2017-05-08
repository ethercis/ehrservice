#!/bin/bash
# description: Populate the template table from cached templates
#
# SCRIPT created 03-05-2017, CCH
#-----------------------------------------------------------------------------------
UNAME=`uname`
HOSTNAME=178.62.71.220
export ECIS_DEPLOY_BASE=/opt/ecis
export SYSLIB=${ECIS_DEPLOY_BASE}/lib/system
export COMMONLIB=${ECIS_DEPLOY_BASE}/lib/common
export APPLIB=${ECIS_DEPLOY_BASE}/lib/application
export LIB=${ECIS_DEPLOY_BASE}/lib/deploy


# Mailer configuration
ECIS_MAILER=echo

# use the right jvm library depending on the OS
# NB: EtherCIS requires java 8
if [ :${UNAME}: = :Linux: ];
then
  JAVA_HOME=/opt/jdk1.8.0_60/jre
fi
if [ :${UNAME}: = :SunOS: ];
then
  JAVA_HOME=/jdk1.8.0_60/jre
fi

#force to use IPv4 so Jetty can bind to it instead of IPv6...
# export _JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"

# runtime parameters
export JVM=${JAVA_HOME}/bin/java
export RUNTIME_HOME=/opt/ecis
export RUNTIME_ETC=/etc/opt/ecis
export RUNTIME_LOG=/var/opt/ecis

export JOOQ_DIALECT=POSTGRES
JOOQ_DB_PORT=5432
JOOQ_DB_HOST=localhost
JOOQ_DB_DATABASE=ethercis
export JOOQ_URL=jdbc:postgresql://${JOOQ_DB_HOST}:${JOOQ_DB_PORT}/${JOOQ_DB_DATABASE}
export JOOQ_DB_LOGIN=postgres
export JOOQ_DB_PASSWORD=postgres

CLASSPATH=./:\
${JAVA_HOME}/lib:\
${LIB}/ecis-core-1.1.1-SNAPSHOT.jar:\
${LIB}/ecis-knowledge-cache-1.1.0-SNAPSHOT.jar:\
${LIB}/ecis-ehrdao-1.1.0-SNAPSHOT.jar:\
${LIB}/jooq-pg-1.1.0-SNAPSHOT.jar:\
${LIB}/ehrxml.jar:\
${LIB}/oet-parser.jar:\
${LIB}/ecis-openehr.jar:\
${LIB}/types.jar:\
${LIB}/adl-parser-1.0.9.jar:\
${SYSLIB}/log4j-1.2.17.jar

# path massaging if running under cygwin
if [ "$(expr substr $(uname -s) 1 6)" == "CYGWIN" ];
then
	echo "CYGWIN path convention is use"
	export CLASSPATH="$(cygpath -pw "$CLASSPATH")"
	export LOGGING_CONFIG="$(cygpath -aw "${RUNTIME_ETC}/logging.properties")"
	export LOG4J_CONFIG="$(cygpath -aw "${RUNTIME_ETC}/log4j.xml")"
	export RUNTIME_ETC="$(cygpath -aw "${RUNTIME_ETC}")"
	export OPTPATH=/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates/
else
	export LOGGING_CONFIG=${RUNTIME_ETC}/logging.properties
	export LOG4J_CONFIG=${RUNTIME_ETC}/log4j.xml
	export OPTPATH=${RUNTIME_ETC}/knowledge/operational_templates/		
fi

# launch server
# ecis server${ECIS_DEPLOY_BASE} is run as user ethercis
# please note usage of cygpath for Windows... :P
echo "populating template table"
echo ${CLASSPATH}
echo "operational_template path:" ${OPTPATH}
${JVM} \
	-Xmx256M \
	-Xms256M \
	-cp ${CLASSPATH} \
	-Xdebug \
	-Djava.util.logging.config.file=${LOGGING_CONFIG} \
	-Dlog4j.configuration=file:${LOG4J_CONFIG} \
	-Djdbc.drivers=org.postgresql.Driver \
	-Dfile.encoding=UTF-8 \
	-Djooq.url=${JOOQ_URL} \
	-Djooq.login=${JOOQ_DB_LOGIN} \
	-Djooq.password=${JOOQ_DB_PASSWORD} \
	-Druntime.etc=${RUNTIME_ETC} \
	com.ethercis.dao.access.support.TemplateRefSetter \
	-url ${JOOQ_URL} \
	-login ${JOOQ_DB_LOGIN} \
	-password ${JOOQ_DB_PASSWORD} \
	-opt_path ${OPTPATH}
exit 0
