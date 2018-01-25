CREATE OR REPLACE FUNCTION ehr.object_version_id(UUID, TEXT, INT)
  RETURNS JSON AS
  $$
  DECLARE
    object_uuid ALIAS FOR $1;
    object_host ALIAS FOR $2;
    object_version ALIAS FOR $3;
  BEGIN
    RETURN
      json_build_object(
          '@class', 'OBJECT_VERSION_ID',
          'value', object_uuid::TEXT || '::' || object_host || '::' || object_version::TEXT
      );
  END
  $$
LANGUAGE plpgsql;