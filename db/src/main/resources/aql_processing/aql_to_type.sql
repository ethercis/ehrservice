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