CREATE OR REPLACE FUNCTION ehr.opt_list(opt_list_filepath TEXT)
  RETURNS TABLE (
    path TEXT
  )
  AS
  $$
  BEGIN
   RETURN QUERY
     SELECT opt_list_filepath||'/'||pg_ls_dir as path
      from pg_ls_dir(opt_list_filepath)
      where pg_ls_dir like '%.opt';
  END
  $$
LANGUAGE plpgsql;