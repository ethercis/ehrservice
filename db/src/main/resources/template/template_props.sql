-- use f.e.
-- select uid, tid FROM ehr.ecis_template_props(<path to template>) AS (uid UUID, tid TEXT);
-- NB. the template directory must be located into ${PG_DATA}
-- WARNING: the file load will fail if it contains BOM (Byte Order Mask) http://en.wikipedia.org/wiki/Byte_order_mark
-- THIS IS WINDOWS STUFF SO use dos2unix to remove it!!!
-- https://waterlan.home.xs4all.nl/dos2unix.html#DOS2UNIX
CREATE OR REPLACE FUNCTION ehr.ecis_template_props(TEXT)
  RETURNS TABLE (
    template_uuid UUID,
    template_id TEXT
  )
  AS
  $$
  DECLARE
    opt_filepath ALIAS FOR $1;
    optxml xml;
  BEGIN
    RAISE NOTICE 'processing template %', opt_filepath;
    optxml := pg_read_file(opt_filepath, 0, 100000000);
    RETURN QUERY
      SELECT (xpath('//n:uid/n:value/text()', def, ARRAY[ARRAY['n','http://schemas.openehr.org/v1']]))[1]::TEXT::UUID as template_uuid,
             (xpath('//n:template_id/n:value/text()', def, ARRAY[ARRAY['n','http://schemas.openehr.org/v1']]))[1]::TEXT as template_id
      FROM
        unnest(
            xpath('/n:template',
                  (pg_read_file(opt_filepath, 0, 100000000))::xml,
                  ARRAY[ARRAY['n','http://schemas.openehr.org/v1']])
        ) def;
  END
  $$
LANGUAGE plpgsql;