CREATE OR REPLACE FUNCTION ehr.ecis_template_list(TEXT)
  RETURNS SETOF text
  AS
  $$
  DECLARE
    opt_list_filepath ALIAS FOR $1;
  BEGIN
    SET client_min_messages TO WARNING;
    DROP TABLE IF EXISTS files;
    CREATE TEMP TABLE files(filename text);
    EXECUTE format ('COPY files FROM ''%s''', opt_list_filepath);
    RETURN QUERY SELECT * FROM files WHERE filename like '%.opt' ORDER BY filename ASC;
  END
  $$
LANGUAGE plpgsql;