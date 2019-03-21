CREATE OR REPLACE FUNCTION ehr.js_dv_coded_text(TEXT, JSON)
  RETURNS JSON AS
  $$
  DECLARE
    value_string ALIAS FOR $1;
    code_phrase ALIAS FOR $2;
  BEGIN
    RETURN
      json_build_object(
          '@class', 'DV_CODED_TEXT',
          'value', value_string,
          'defining_code', code_phrase
      );
  END
  $$
LANGUAGE plpgsql;