# Patient Summary Cache #

The patient summary cache is an attempt to support "fixed" composition gathering summary results (f.e. number of transaction of a given type, date of last transaction etc.). In this implementation, results of aggregations are calculated at DB level. That is, EtherCIS (the middleware) does not play any role in the construction of the cache.

## Principle

We build and update ONE summary cache per EHR. In other terms, one fixed composition is allocated to one EHR. The fixed composition is a simple no-content composition, calculated data is contained in an archetyped other_context structure.

The calculations are performed using SQL triggers whenever a new composition is created, updated or deleted. Depending on the actual transaction type, the trigger is invoked at different level:

- ENTRY level for INSERT/UPDATE
- COMPOSITION level for DELETE: this is due to the sequence whenever invoking a DELETE CASCADE

		The summary composition is built at DB level. It is template dependent; 
		if the template is changed, the respective encoding must be adapted!

## Under The Hood

Few things to know to understand how this works.

This module is essentially based on two scripts generating the functions used to build and update the summaries:

- `cache_composition.sql` create the function used to maintain summaries for an EHR:
	- `set_cache_for_ehr()` insert the entries initially required for a summary:
		- `entry` is a pseudo content for the composition. It is used to deal with AQL queries (template id in particular)
		- `composition` this is the main entry point to retrieve the summary as a composition using REST GET
		- `event_context` this table holds the summaries as a JSONB structure
		- `containment` set the composition archetype id used to resolve AQL clause `CONTAINS`
	- `update_cache_for_ehr()` updates the results or create a new summary if the cache composition does not exist
	- `other_context_for_ehr()` this function performs the calculations for an EHR according to the defined headings.
- `generate_summary_fields.sql` creates the functions used to encode summary results into a valid JSONB data reflecting the archetyped structure
	- `other_context_summary_field()` generates the jsonb object
	- `create_items_dv_count()` creates a jsonb structure for a DvCount RM type
	- `create_items_dv_date_time()` creates a jsonb structure for a DvDateTime RM type

NB. NULL count is defaulted to `0`. NULL date/time is defaulted to `1970-01-01 00:00:00`  


## Configuration

Since the summary cache is maintained at DB level, the configuration is held in tables:

- `HEADING`: contains the list of defined headings with their name and description
- `TEMPLATE`: this table is built from the actual knowledge cache (compiled operational templates), it contains the template UID (generally created whenever the operational template is generated), the template id and the concept.
- `TEMPLATE_HEADING_XREF`: this table build the 1-m lookup between headings and templates.

Several scripts and utility are provided to build the configuration:

- `heading.sql`: SQL script to build the `HEADING` table
- `set_template_table.sh`: shell script used to build the `TEMPLATE` reference table. Several variable must be adjusted depending on the environment
	- `JOOQ_DB_HOST`: the DB host id to bind to (hostname, IP address, 'localhost'...)
	- `JOOQ_DB_PORT`: the DB port to bind to
	- `JOOQ_DB_DATABASE`:the database used by ethercis (NB. the default schema is 'ehr')
	- `JOOQ_DB_LOGIN`: the login name used to connect to the DB
	- `JOOQ_DB_PASSWORD`: the password used to connect to the DB
	- `OPTPATH`: the directory path to access the operational templates. This should be the same as used by EtherCIS.
- `template_heading.sql`: SQL script to build `TEMPLATE_HEADING_XREF` using template id (easier to read...)

## Installation

Shell script `prepare_db.sh` performs all required steps to enable a DB to support summary calculations:

- create the configuration tables (as described above) and set-up the headings.
- Populate `template` table with operational template meta-data
- Build the cross references
- Setup the required DB functions to perform the calculation and RM composition encoding
- Setup the table triggers

Shell script `set_template_table.sh` must be adapted to the runtime environment.

## Initialization

Script `init_summary.sql` can be used to create the summaries for an existing DB

## Operation

The triggers are set using script `set_cache_summary_db_triggers.sql`:

- table `COMPOSITION` trigger is invoked on DELETE. It updates the summary for a delete composition transaction.
- table `ENTRY` trigger is invoked on INSERT or UPDATE. It updates the summary for an insert/update composition transaction.
- table `EVENT_CONTEXT` uses two _conditional_ versioning triggers that are invoked whenever the transaction is done on a non summary object.

NB. table `EVENT_CONTEXT` column `sys_period` is modified to be NULLABLE. This is required since summaries are non versioned objects. 

## Querying

### AQL

	select e/ehr_id/value as ehrId, 
		a/context/other_context[at0001]/items[at0005]/value/magnitude as diagnosesCount,
		a/context/other_context[at0001]/items[at0002]/value/magnitude as ordersCount,
		a/context/other_context[at0001]/items[at0006]/value/value as ordersDate,
		a/context/other_context[at0001]/items[at0004]/value/magnitude as resultsCount,
		a/context/other_context[at0001]/items[at0009]/value/value as resultsDate,
		a/context/other_context[at0001]/items[at0003]/value/magnitude as vitalsCount,
		a/context/other_context[at0001]/items[at0007]/value/value as vitalsDate
		from EHR e contains COMPOSITION a[openEHR-EHR-COMPOSITION.ripple_cache.v1]

Returns a list of summaries:

	{
	  "executedAQL": "select e/ehr_id/value as ehrId, \r\n\t\ta/context/other_context[at0001]/items[at0005]/value/magnitude as diagnosesCount,\r\n\t\ta/context/other_context[at0001]/items[at0002]/value/magnitude as ordersCount,\r\n\t\ta/context/other_context[at0001]/items[at0006]/value/value as ordersDate,\r\n\t\ta/context/other_context[at0001]/items[at0004]/value/magnitude as resultsCount,\r\n\t\ta/context/other_context[at0001]/items[at0009]/value/value as resultsDate,\r\n\t\ta/context/other_context[at0001]/items[at0003]/value/magnitude as vitalsCount,\r\n\t\ta/context/other_context[at0001]/items[at0007]/value/value as vitalsDate\r\n\t\tfrom EHR e contains COMPOSITION a[openEHR-EHR-COMPOSITION.ripple_cache.v1]\r\n\t\t",
	  "resultSet": [
	    {
	      "ordersCount": "0",
	      "resultsDate": "2017-05-05T14:47:34.622+07:00",
	      "vitalsDate": "1970-01-01T00:00:00+07:00",
	      "diagnosesCount": "0",
	      "resultsCount": "8",
	      "vitalsCount": "0",
	      "ehrId": "00c7b748-0bca-4f41-892e-5c03ff673d7d",
	      "ordersDate": "1970-01-01T00:00:00+07:00"
	    },
	    {
	      "ordersCount": "0",
	      "resultsDate": "1970-01-01T00:00:00+07:00",
	      "vitalsDate": "1970-01-01T00:00:00+07:00",
	      "diagnosesCount": "0",
	      "resultsCount": "0",
	      "vitalsCount": "0",
	      "ehrId": "00fa6812-9434-4455-9c7b-a956e1a17317",
	      "ordersDate": "1970-01-01T00:00:00+07:00"
	    },
	    {....

### REST API

`GET <server_url>/rest/v1/composition?uid=<composition_id>&format=[FLAT|ECISFLAT]`

#### FLAT JSON
	{
	  "composition": {
	    "ripple_dashboard_cache/_uid": "49e0b063-3b58-4e9d-b421-14df5c14a506::vm01.ethercis.org::1",
	    "ripple_dashboard_cache/language|code": "en",
	    "ripple_dashboard_cache/language|terminology": "ISO_639-1",
	    "ripple_dashboard_cache/territory|code": "GB",
	    "ripple_dashboard_cache/territory|terminology": "ISO_3166-1",
	    "ripple_dashboard_cache/context/orders": 1,
	    "ripple_dashboard_cache/context/orders_date": "2016-12-27T14:24:50.614+07:00",
	    "ripple_dashboard_cache/context/vitals": 1,
	    "ripple_dashboard_cache/context/vitals_date": "2016-12-27T14:24:49.665+07:00",
	    "ripple_dashboard_cache/context/results": 1,
	    "ripple_dashboard_cache/context/results_date": "2016-12-27T14:24:51.590+07:00",
	    "ripple_dashboard_cache/context/diagnoses": 0,
	    "ripple_dashboard_cache/context/diagnoses_date": "1970-01-01T00:00:00.000+07:00",
	    "ripple_dashboard_cache/context/start_time": "2017-05-04T16:05:32.009+07:00",
	    "ripple_dashboard_cache/context/setting|code": "238",
	    "ripple_dashboard_cache/context/setting|value": "other care",
	    "ripple_dashboard_cache/context/setting|terminology": "openehr",
	    "ripple_dashboard_cache/composer|name": "$RIPPLE_SUMMARY_CACHE$"
	  },
	  "meta": {
	    "href": "rest/v1/composition?uid=49e0b063-3b58-4e9d-b421-14df5c14a506::vm01.ethercis.org::1"
	  },
	  "format": "FLAT",
	  "templateId": "Ripple Dashboard Cache.v1"
	}

#### ECIS FLAT

	{
	  "composition": {
	    "/composer|name": "$RIPPLE_SUMMARY_CACHE$",
	    "/context/other_context[at0001]/items[at0002]|value": "1",
	    "/context/other_context[at0001]/items[at0003]|value": "1",
	    "/context/other_context[at0001]/items[at0004]|value": "1",
	    "/context/other_context[at0001]/items[at0005]|value": "0",
	    "/context/other_context[at0001]/items[at0006]|value": "2016-12-27T14:24:50.614+07:00",
	    "/context/other_context[at0001]/items[at0007]|value": "2016-12-27T14:24:49.665+07:00",
	    "/context/other_context[at0001]/items[at0008]|value": "1970-01-01T00:00:00+07:00",
	    "/context/other_context[at0001]/items[at0009]|value": "2016-12-27T14:24:51.59+07:00",
	    "/context/setting": "openehr::238|other care|",
	    "/context/start_time": "2017-05-04T16:05:32.009+07:00",
	    "/language": "en",
	    "/territory": "GB"
	  },
	  "meta": {
	    "href": "rest/v1/composition?uid=49e0b063-3b58-4e9d-b421-14df5c14a506::vm01.ethercis.org::1"
	  },
	  "format": "ECISFLAT",
	  "templateId": "Ripple Dashboard Cache.v1"
	} 

### License

See LICENSE.txt in this directory.