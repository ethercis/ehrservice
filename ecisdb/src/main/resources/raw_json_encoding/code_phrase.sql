CREATE OR REPLACE FUNCTION ehr.js_code_phrase(TEXT, TEXT)
  RETURNS JSON AS
  $$
  DECLARE
    code_string ALIAS FOR $1;
    terminology ALIAS FOR $2;
  BEGIN
    RETURN
      json_build_object(
          '@class', 'CODE_PHRASE',
          'terminology_id',
          json_build_object(
              '@class', 'TERMINOLOGY_ID',
              'value', terminology
          ),
          'code_string', code_string
      );
  END
  $$
LANGUAGE plpgsql;