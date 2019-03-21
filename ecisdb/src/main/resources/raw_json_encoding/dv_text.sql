CREATE OR REPLACE FUNCTION ehr.js_dv_text(TEXT)
  RETURNS JSON AS
  $$
  DECLARE
    value_string ALIAS FOR $1;
  BEGIN
    RETURN
      json_build_object(
          '@class', 'DV_TEXT',
          'value', value_string
      );
  END
  $$
LANGUAGE plpgsql;