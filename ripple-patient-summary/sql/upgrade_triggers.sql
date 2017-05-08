-- alter the existing versioning triggers
--  C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------

DROP TRIGGER versioning_trigger ON ehr.event_context;
DROP TRIGGER versioning_delete_trigger ON ehr.event_context;

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE ON ehr.event_context
FOR EACH ROW
WHEN (NOT ehr.is_event_context_summary_cache(NEW.id))
EXECUTE PROCEDURE versioning('sys_period', 'ehr.event_context_history', true);

CREATE TRIGGER versioning_delete_trigger BEFORE DELETE ON ehr.event_context
FOR EACH ROW
WHEN (NOT ehr.is_event_context_summary_cache(OLD.id))
EXECUTE PROCEDURE versioning('sys_period', 'ehr.event_context_history', true);



