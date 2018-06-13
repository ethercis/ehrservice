CREATE OR REPLACE FUNCTION ehr.aql_dv_text(json_value TEXT)
  RETURNS ANYELEMENT AS
  $$
  BEGIN
    RETURN json_value::TEXT;
  END
  $$
LANGUAGE plpgsql;