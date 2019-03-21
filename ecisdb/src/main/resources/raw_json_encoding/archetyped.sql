CREATE OR REPLACE FUNCTION ehr.js_archetyped(TEXT, TEXT)
  RETURNS JSON AS
  $$
  DECLARE
    archetype_id ALIAS FOR $1;
    template_id ALIAS FOR $2;
  BEGIN
    RETURN
      json_build_object(
          '@class', 'ARCHETYPED',
          'archetype_id',
          json_build_object(
              '@class', 'ARCHETYPE_ID',
              'value', archetype_id
          ),
          template_id,
          json_build_object(
              '@class', 'TEMPLATE_ID',
              'value', template_id
          ),
          'rm_version', '1.0.1'
      );
  END
  $$
LANGUAGE plpgsql;