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