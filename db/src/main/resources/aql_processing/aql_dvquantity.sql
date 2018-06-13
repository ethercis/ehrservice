CREATE OR REPLACE FUNCTION ehr.aql_dv_quantity(json_value NUMERIC)
  RETURNS REAL AS
  $$
  BEGIN
    RETURN json_value::REAL;
  END
  $$
LANGUAGE plpgsql;