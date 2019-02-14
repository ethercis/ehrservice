CREATE OR REPLACE FUNCTION ehr.js_party_identified(TEXT, JSON)
  RETURNS JSON AS
  $$
  DECLARE
    name_value ALIAS FOR $1;
    external_ref ALIAS FOR $2;
  BEGIN
      IF (external_ref IS NOT NULL) THEN
        RETURN
          json_build_object(
              '@class', 'PARTY_IDENTIFIED',
              'name', name_value,
              'external_ref', external_ref
          );
      ELSE
        RETURN
          json_build_object(
              '@class', 'PARTY_IDENTIFIED',
              'name', name_value
          );
      END IF;
  END
  $$
LANGUAGE plpgsql;