CREATE OR REPLACE FUNCTION ehr.js_context(UUID)
  RETURNS JSON AS
  $$
  DECLARE
    context_id ALIAS FOR $1;
    json_context_query TEXT;
    json_context       JSON;
    v_start_time  TIMESTAMP;
    v_start_time_tzid TEXT;
    v_end_time TIMESTAMP;
    v_end_time_tzid TEXT;
    v_facility UUID;
    v_location TEXT;
    v_other_context JSONB;
    v_setting UUID;
  BEGIN

    IF (context_id IS NULL) THEN
      RETURN NULL;
    END IF;


    -- build the query
    SELECT start_time, start_time_tzid, end_time, end_time_tzid, facility, location, other_context, setting
          FROM ehr.event_context
          WHERE id = context_id
          INTO v_start_time, v_start_time_tzid, v_end_time, v_end_time_tzid, v_facility, v_location, v_other_context,v_setting;

    json_context_query := ' SELECT json_build_object(
                                  ''@class'', ''EVENT_CONTEXT'',
                                  ''start_time'', ehr.js_dv_date_time('''||v_start_time||''','''|| v_start_time_tzid||'''),';

    IF (v_end_time IS NOT NULL)
    THEN
      json_context_query := json_context_query || '''end_date'', ehr.js_dv_date_time('''||v_end_time||''','''|| v_end_time_tzid||'''),';
    END IF;

    IF (SELECT v_location IS NOT NULL)
    THEN
      json_context_query := json_context_query || '''location'', '||v_location||',';
    END IF;

    IF (v_other_context IS NOT NULL)
    THEN
      json_context_query := json_context_query || '''other_context'', '||v_other_context::TEXT||',';
    END IF;

    IF (v_facility IS NOT NULL)
    THEN
      json_context_query := json_context_query || '''health_care_facility'', ehr.js_party('''||v_facility||'''),';
    END IF;

    json_context_query := json_context_query || '''setting'',ehr.js_context_setting('''||v_setting||'''))';

    --     IF (participation IS NOT NULL) THEN
    --       json_context_query := json_context_query || '''participation'', participation,';
    --     END IF;

    EXECUTE json_context_query
    INTO STRICT json_context;

    RETURN json_context;
  END
  $$
LANGUAGE plpgsql;