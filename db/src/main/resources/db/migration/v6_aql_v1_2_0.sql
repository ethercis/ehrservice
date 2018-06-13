CREATE OR REPLACE FUNCTION ehr.aql_dv_count(json_value NUMERIC)
  RETURNS INTEGER AS
  $$
  BEGIN
    RETURN json_value::INTEGER;
  END
  $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.aql_dv_quantity(json_value NUMERIC)
  RETURNS REAL AS
  $$
  BEGIN
    RETURN json_value::REAL;
  END
  $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.aql_dv_text(json_value TEXT)
  RETURNS ANYELEMENT AS
  $$
  BEGIN
    RETURN json_value::TEXT;
  END
  $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.aql_to_type(json_value ANYELEMENT, rm_type TEXT)
  RETURNS ANYELEMENT AS
  $$
  BEGIN
    CASE rm_type
      WHEN 'DvQuantity'
      THEN
        RETURN json_value::REAL;
      WHEN 'DvCount'
      THEN
        RETURN json_value::INTEGER;
      WHEN 'DvText'
      THEN
        RETURN json_value::TEXT;
    ELSE
      RETURN json_value;
    END CASE;
  END
  $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.aql_node_name_predicate(entry JSONB, name_value_predicate TEXT, jsonb_path TEXT)
  RETURNS JSONB AS
  $$
  DECLARE
    entry_segment JSONB;
    jsquery_node_expression TEXT;
    subnode JSONB;
  BEGIN

    -- get the segment for the predicate

    SELECT jsonb_extract_path(entry, VARIADIC string_to_array(jsonb_path, ',')) INTO STRICT entry_segment;

    IF (entry_segment IS NULL) THEN
      RETURN NULL ;
    END IF ;

    -- identify structure with name/value matching argument
    IF (jsonb_typeof(entry_segment) <> 'array') THEN
      RETURN NULL;
    END IF;

    FOR subnode IN SELECT jsonb_array_elements(entry_segment)
      LOOP
        IF ((subnode #>> '{/name,0,value}') = name_value_predicate) THEN
          RETURN subnode;
        END IF;
      END LOOP;

    RETURN NULL;

  END
  $$
LANGUAGE plpgsql;