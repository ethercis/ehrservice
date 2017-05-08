-- set trigger to recalculate summary on composition DELETE
--  C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
DROP TRIGGER IF EXISTS old_summary_trigger ON ehr.composition;

CREATE TRIGGER old_summary_trigger BEFORE DELETE ON ehr.composition
FOR EACH ROW EXECUTE PROCEDURE ehr.cache_summary_delete_trigger();

-- set trigger to recalculate summary on entry INSERT/UPDATE
DROP TRIGGER IF EXISTS new_summary_trigger ON ehr.entry;

CREATE TRIGGER new_summary_trigger AFTER INSERT OR UPDATE ON ehr.entry
FOR EACH ROW EXECUTE PROCEDURE ehr.cache_summary_update_insert_trigger();


-- alter the current triggers
ALTER TABLE ehr.event_context ALTER COLUMN sys_period DROP NOT NULL;

DROP TRIGGER IF EXISTS versioning_trigger ON ehr.event_context;
DROP TRIGGER IF EXISTS versioning_delete_trigger ON ehr.event_context;

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE ON ehr.event_context
FOR EACH ROW
WHEN (NOT ehr.is_event_context_summary_cache(NEW.id))
EXECUTE PROCEDURE ehr.versioning('sys_period', 'ehr.event_context_history', true);

CREATE TRIGGER versioning_delete_trigger BEFORE DELETE ON ehr.event_context
FOR EACH ROW
WHEN (NOT ehr.is_event_context_summary_cache(OLD.id))
EXECUTE PROCEDURE ehr.versioning('sys_period', 'ehr.event_context_history', true);





