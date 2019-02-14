CREATE OR REPLACE FUNCTION ehr.js_dv_date_time(TIMESTAMPTZ, TEXT)
  RETURNS JSON AS
  $$
  DECLARE
    date_time ALIAS FOR $1;
    time_zone ALIAS FOR $2;
  BEGIN

    IF (date_time IS NULL)
    THEN
      RETURN NULL;
    END IF;

    IF (time_zone IS NULL)
    THEN
      time_zone := 'UTC';
    END IF;

    RETURN
      json_build_object(
          '@class', 'DV_DATE_TIME',
          'value', ehr.iso_timestamp(date_time)||time_zone
      );
  END
  $$
LANGUAGE plpgsql;