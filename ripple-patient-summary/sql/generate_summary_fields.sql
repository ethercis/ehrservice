-- encode a node name following ethercis json convention
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION ehr.encode_node_name(TEXT)
  RETURNS JSON AS
  $encode_node_name$
  DECLARE
    nodeName ALIAS FOR $1;
  BEGIN
    RETURN
      json_build_array(json_build_object('value', nodeName));
  END
  $encode_node_name$
LANGUAGE plpgsql;

-- build a DvCount named node
CREATE OR REPLACE FUNCTION ehr.create_items_dv_count(TEXT, BIGINT, TEXT)
  RETURNS JSON AS
  $dv_count$
  DECLARE
    nodeName ALIAS FOR $1;
    magnitude ALIAS FOR $2;
    nodePath ALIAS FOR $3;
    jsonValue JSON;
  BEGIN
    RETURN
      json_build_array(
          json_build_object(
              '/name', ehr.encode_node_name(nodeName),
              '/value',
              json_build_object(
                  'accuracy', 0.0,
                  'magnitude', COALESCE(magnitude, 0),
                  'accuracyPercent', FALSE
              ),
              '/$PATH$', nodePath,
              '/$CLASS$', 'DvCount'
          )
      );
  END
  $dv_count$
LANGUAGE plpgsql;

-- build a DvDateTime named node
CREATE OR REPLACE FUNCTION ehr.create_items_dv_date_time(TEXT, TIMESTAMPTZ, TEXT)
  RETURNS JSON AS
  $dv_date_time$
  DECLARE
    nodeName ALIAS FOR $1;
    dateTime ALIAS FOR $2;
    nodePath ALIAS FOR $3;
  BEGIN
    RETURN
      json_build_array(
          json_build_object(
              '/name', ehr.encode_node_name(nodeName),
              '/value', json_build_object(
                  'value', COALESCE(dateTime, '1970-01-01 00:00:00'::TIMESTAMP)
              ),
              '/$PATH$', nodePath,
              '/$CLASS$', 'DvDateTime'
          )
      );
  END
  $dv_date_time$
LANGUAGE plpgsql;

-- This function build a JSONB value based on the passed arguments
-- Arguments are date of the last composition (TIMESTAMP), count of compositions (INT) for the respective headings:
-- ORDERS
-- RESULTS
-- VITALS
-- DIAGNOSES
CREATE OR REPLACE FUNCTION ehr.other_context_summary_field(TIMESTAMPTZ, BIGINT, TIMESTAMPTZ, BIGINT, TIMESTAMPTZ, BIGINT,
                                                      TIMESTAMPTZ, BIGINT)
  RETURNS JSONB AS
  $summary$
  DECLARE
    ordersLastDate ALIAS FOR $1;
    ordersCount ALIAS FOR $2;
    resultsLastDate ALIAS FOR $3;
    resultsCount ALIAS FOR $4;
    vitalsLastDate ALIAS FOR $5;
    vitalsCount ALIAS FOR $6;
    diagnosesLastDate ALIAS FOR $7;
    diagnosesCount ALIAS FOR $8;

    ordersCountPath    TEXT;
    ordersLastPath     TEXT;
    resultsCountPath   TEXT;
    resultsLastPath    TEXT;
    vitalsCountPath    TEXT;
    vitalsLastPath     TEXT;
    diagnosesCountPath TEXT;
    diagnosesLastPath  TEXT;
    ordersCountName    TEXT;
    ordersLastName     TEXT;
    resultsCountName   TEXT;
    resultsLastName    TEXT;
    vitalsCountName    TEXT;
    vitalsLastName     TEXT;
    diagnosesCountName TEXT;
    diagnosesLastName  TEXT;
  BEGIN
    ordersCountName := 'Orders';
    ordersLastName := 'Orders date';
    resultsCountName := 'Results';
    resultsLastName := 'Results date';
    vitalsCountName := 'Vitals';
    vitalsLastName := 'Vitals date';
    diagnosesCountName := 'Diagnoses';
    diagnosesLastName := 'Diagnoses date';

    ordersCountPath := '/items[at0001 and name/value=''Tree'']/items[at0002 and name/value=''Orders'']';
    ordersLastPath := '/items[at0001 and name/value=''Tree'']/items[at0006 and name/value=''Orders date'']';
    resultsCountPath := '/items[at0001 and name/value=''Tree'']/items[at0004 and name/value=''Results'']';
    resultsLastPath := '/items[at0001 and name/value=''Tree'']/items[at0009 and name/value=''Results date'']';
    vitalsCountPath := '/items[at0001 and name/value=''Tree'']/items[at0003 and name/value=''Vitals'']';
    vitalsLastPath := '/items[at0001 and name/value=''Tree'']/items[at0007 and name/value=''Vitals date'']';
    diagnosesCountPath := '/items[at0001 and name/value=''Tree'']/items[at0005 and name/value=''Diagnoses'']';
    diagnosesLastPath := '/items[at0001 and name/value=''Tree'']/items[at0008 and name/value=''Diagnoses date'']';

    RETURN
      json_build_object(
          '/context/other_context[at0001]',
          json_build_object(
              '/items[at0002]', ehr.create_items_dv_count(ordersLastName, ordersCount, ordersCountPath),
              '/items[at0003]', ehr.create_items_dv_count(vitalsCountName, vitalsCount, vitalsCountPath),
              '/items[at0004]', ehr.create_items_dv_count(resultsCountName, resultsCount, resultsCountPath),
              '/items[at0005]', ehr.create_items_dv_count(diagnosesCountName, diagnosesCount, diagnosesCountPath),
              '/items[at0006]', ehr.create_items_dv_date_time(ordersLastName, ordersLastDate, ordersLastPath),
              '/items[at0007]', ehr.create_items_dv_date_time(vitalsLastName, vitalsLastDate, vitalsLastPath),
              '/items[at0008]', ehr.create_items_dv_date_time(diagnosesLastName, diagnosesLastDate, diagnosesLastPath),
              '/items[at0009]', ehr.create_items_dv_date_time(resultsLastName, resultsLastDate, resultsLastPath)
          )
      )::JSONB;

  END;
  $summary$

LANGUAGE plpgsql;
