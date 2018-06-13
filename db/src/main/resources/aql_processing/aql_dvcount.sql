CREATE OR REPLACE FUNCTION ehr.aql_dv_count(json_value NUMERIC)
  RETURNS INTEGER AS
  $$
  BEGIN
    RETURN json_value::INTEGER;
  END
  $$
LANGUAGE plpgsql;